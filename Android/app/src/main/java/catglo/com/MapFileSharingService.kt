package catglo.com

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.media.RingtoneManager
import android.net.Uri
import android.os.*
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import catglo.com.MapFileSharingStatus.*
import catglo.com.deliverydroid.R
import catglo.com.deliverydroid.Settings
import catglo.com.deliverydroid.homeScreen.DownloadMapActivity
import catglo.com.deliverydroid.homeScreen.HomeScreenActivity
import com.frostwire.jlibtorrent.Priority
import com.frostwire.jlibtorrent.TorrentHandle
import com.masterwok.simpletorrentandroid.TorrentSession
import com.masterwok.simpletorrentandroid.TorrentSessionOptions
import com.masterwok.simpletorrentandroid.contracts.TorrentSessionListener
import com.masterwok.simpletorrentandroid.models.TorrentSessionStatus
import kotlinx.android.synthetic.main.kind_of_note.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.IOException
import java.io.Serializable
import java.net.NoRouteToHostException
import java.net.URI

enum class MapFileSharingStatus : Serializable { None, Initializing, Downloading, Uploading, Error }

abstract class MapSharingServiceStatusListener : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent==null) return

        intent.getSerializableExtra("status")?.let { status ->
            if (status is MapFileSharingStatus) {
                onMapStatus(intent.getFloatExtra("progress",0f),status)
            }
        }
    }

    abstract fun onMapStatus(progress: Float, status: MapFileSharingStatus)

    companion object {
        fun registerReceiver(context:Context,receiver: MapSharingServiceStatusListener) {
            context.registerReceiver(receiver, IntentFilter("MapSharingServiceStatusListener"))
        }
    }
}

class MapFileSharingService : Service(),TorrentSessionListener {

    private fun brodcastStatus(){
        sendBroadcast(Intent("MapSharingServiceStatusListener").apply {
            putExtra("progress",progress)
            putExtra("status",status)
        })
    }

    var status : MapFileSharingStatus = None
        set(value) {
            field = value
            brodcastStatus()
        }
    var progress : Float = 0f
        set(value) {
            field = value
            brodcastStatus()
        }

    override fun onTorrentRemoved(torrentHandle: TorrentHandle, torrentSessionStatus: TorrentSessionStatus) { }
    override fun onTorrentDeleteFailed(torrentHandle: TorrentHandle, torrentSessionStatus: TorrentSessionStatus) { }
    override fun onTorrentDeleted(torrentHandle: TorrentHandle, torrentSessionStatus: TorrentSessionStatus) { }
    override fun onTorrentError(torrentHandle: TorrentHandle, torrentSessionStatus: TorrentSessionStatus) {
        Log.e("Torrent","onTorrentError")
    }
    override fun onTorrentFinished(torrentHandle: TorrentHandle, torrentSessionStatus: TorrentSessionStatus) {
        Log.i("Torrent","On Torrent Finished")

    }
    override fun onTorrentPaused(torrentHandle: TorrentHandle, torrentSessionStatus: TorrentSessionStatus) {
        Log.e("Torrent","onTorrentPaused")
    }
    override fun onBlockUploaded(torrentHandle: TorrentHandle, torrentSessionStatus: TorrentSessionStatus) {
        Log.e("Torrent","onBlockUploaded")
    }
    override fun onMetadataFailed(torrentHandle: TorrentHandle, torrentSessionStatus: TorrentSessionStatus) {
        Log.e("Torrent","onMetadataFailed")
    }
    override fun onMetadataReceived(torrentHandle: TorrentHandle, torrentSessionStatus: TorrentSessionStatus) {
        Log.i("Torrent","Metadata Received downloaded = ${torrentSessionStatus.bytesDownloaded} wanted = ${torrentSessionStatus.bytesWanted}")
        setTorrent(torrentHandle, torrentSessionStatus)
    }
    override fun onPieceFinished(torrentHandle: TorrentHandle, torrentSessionStatus: TorrentSessionStatus) {
        //TODO: Rescan files
        progress = torrentSessionStatus.progress
        if (progress>=1) status = Uploading
        Log.i("Torrent","On Piece Finished progress=${torrentSessionStatus.progress} downloaded = ${torrentSessionStatus.bytesDownloaded} wanted = ${torrentSessionStatus.bytesWanted}")
    }
    override fun onTorrentResumed(torrentHandle: TorrentHandle, torrentSessionStatus: TorrentSessionStatus) {
        Log.i("Torrent","On PTorrent Resumed downloaded = ${torrentSessionStatus.bytesDownloaded} wanted = ${torrentSessionStatus.bytesWanted}")

        setTorrent(torrentHandle, torrentSessionStatus)
    }
    override fun onAddTorrent(torrentHandle: TorrentHandle, torrentSessionStatus: TorrentSessionStatus) {
        Log.i("Torrent","Add Torrent downloaded = ${torrentSessionStatus.bytesDownloaded} wanted = ${torrentSessionStatus.bytesWanted}")

        setTorrent(torrentHandle, torrentSessionStatus)
    }

    fun setTorrent( torrentHandle: TorrentHandle,torrentSessionStatus: TorrentSessionStatus){
        val torrentInfo = torrentHandle.torrentFile() ?: return

        //The way we select individual files from the torrent is by setting their priority
        val selectedDownloads = Settings(this).mapDownloads()
        val totalFiles = torrentInfo.numFiles()
        if (totalFiles>0) {
            status = Downloading
            var priorities = torrentHandle.filePriorities()
            for (i in 0..(totalFiles - 1)) {
                val fileName = torrentInfo.files().filePath(i)
              //  Log.i("Tor", "File Name $i: $fileName ${priorities[i]}")
                if (selectedDownloads.contains(fileName)) {
                    priorities[i] = Priority.SEVEN
                } else {
                    priorities[i] = Priority.IGNORE
                }
            }
            torrentHandle.prioritizeFiles(priorities)
            priorities = torrentHandle.filePriorities()

        }
    }

    var isDownloading = false
    private lateinit var torrentSession: TorrentSession

    fun initiateDownload(){
        if (isDownloading)
        {

        }
        else {
            isDownloading = true
            val torrentSessionOptions = TorrentSessionOptions(
                downloadLocation = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            )
            torrentSession = TorrentSession(torrentSessionOptions)
            torrentSession.listener = this
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    Log.i("Torrent","Starting torrent session")
                    status = MapFileSharingStatus.Initializing
                    torrentSession.start(applicationContext, Uri.parse("http://zan.ooguy.com/Maps.torrent"))

                } catch (e: IOException) {
                    //TODO: Deal with the server being down somehow
                    status = MapFileSharingStatus.Error
                    Log.e("Torrent","Failed to create torrent session "+e.localizedMessage)
                }
            }
        }

    }


    private val myBinder = LocalBinder()
    override fun onBind(intent: Intent): IBinder? {
        return myBinder
    }
    inner class LocalBinder : Binder() {
        fun getService() : MapFileSharingService {
            return this@MapFileSharingService
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i("Torrent","Service start command ")
        if (Settings(this).mapDownloads().isNotEmpty())
        {
            initiateDownload()
        }
        return Service.START_STICKY
    }

    override fun onCreate() {
        Log.i("Torrent","Service created")
        super.onCreate()
        val intent = Intent(this, DownloadMapActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0 /* request code */, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT)

            val builder = NotificationCompat.Builder(this, getString(R.string.app_name))
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentText("Map Sharing Service")
                .setSmallIcon(R.drawable.icon)
                .setContentIntent(pendingIntent)
                .setOnlyAlertOnce(true)
                .setChannelId(CHANNEL_ID)

            val mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            mNotificationManager.createNotificationChannel(channel)
            val notification = builder.build()
            startForeground(1,notification)
        }


    }

    override fun onDestroy() {
        Log.i("Torrent","Service started")
        super.onDestroy()
    }

    companion object {
        private val CHANNEL_ID = "com.catglo.mapdownloadchannel"
        private val CHANNEL_NAME = "Delivery Droid Map Sharing service"
        private val CHANNEL_DESCRIPTION = "For accessing map files for offline use in delivery droid"
    }



}
