package catglo.com

import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.Binder
import android.os.Environment
import android.os.IBinder
import android.util.Log
import catglo.com.deliverydroid.Settings
import com.frostwire.jlibtorrent.Priority
import com.frostwire.jlibtorrent.TorrentHandle
import com.masterwok.simpletorrentandroid.TorrentSession
import com.masterwok.simpletorrentandroid.TorrentSessionOptions
import com.masterwok.simpletorrentandroid.contracts.TorrentSessionListener
import com.masterwok.simpletorrentandroid.models.TorrentSessionStatus
import java.net.URI

class MapFileSharingService : Service(),TorrentSessionListener {



    override fun onTorrentRemoved(torrentHandle: TorrentHandle, torrentSessionStatus: TorrentSessionStatus) { }
    override fun onTorrentDeleteFailed(torrentHandle: TorrentHandle, torrentSessionStatus: TorrentSessionStatus) { }
    override fun onTorrentDeleted(torrentHandle: TorrentHandle, torrentSessionStatus: TorrentSessionStatus) { }
    override fun onTorrentError(torrentHandle: TorrentHandle, torrentSessionStatus: TorrentSessionStatus) { }
    override fun onTorrentFinished(torrentHandle: TorrentHandle, torrentSessionStatus: TorrentSessionStatus) { }
    override fun onTorrentPaused(torrentHandle: TorrentHandle, torrentSessionStatus: TorrentSessionStatus) { }
    override fun onBlockUploaded(torrentHandle: TorrentHandle, torrentSessionStatus: TorrentSessionStatus) { }
    override fun onMetadataFailed(torrentHandle: TorrentHandle, torrentSessionStatus: TorrentSessionStatus) { }
    override fun onMetadataReceived(torrentHandle: TorrentHandle, torrentSessionStatus: TorrentSessionStatus) {
        setTorrent(torrentHandle, torrentSessionStatus)
    }
    override fun onPieceFinished(torrentHandle: TorrentHandle, torrentSessionStatus: TorrentSessionStatus) {
        //TODO: Rescan files
    }
    override fun onTorrentResumed(torrentHandle: TorrentHandle, torrentSessionStatus: TorrentSessionStatus) {
        setTorrent(torrentHandle, torrentSessionStatus)
    }
    override fun onAddTorrent(torrentHandle: TorrentHandle, torrentSessionStatus: TorrentSessionStatus) {
        setTorrent(torrentHandle, torrentSessionStatus)
    }

    fun setTorrent( torrentHandle: TorrentHandle,torrentSessionStatus: TorrentSessionStatus){
        val torrentInfo = torrentHandle.torrentFile()
        if (torrentInfo==null) return

        val selectedDownloads = Settings(this).mapDownloads()
        val totalFiles = torrentInfo.numFiles()
        if (totalFiles>0) {
            var priorities = torrentHandle.filePriorities()
            for (i in 0..(totalFiles - 1)) {
                val fileName = torrentInfo.files().fileName(i)
              //  Log.i("Tor", "File Name $i: $fileName ${priorities[i]}")
                if (selectedDownloads.contains(fileName)) {
                    priorities[i] = Priority.SEVEN
                } else {
                    priorities[i] = Priority.IGNORE
                }
            }
            torrentHandle.prioritizeFiles(priorities)
            priorities = torrentHandle.filePriorities()

            //Log priorities
            for (i in 0..(totalFiles - 1)) {
                val fileName = torrentInfo.files().fileName(i)
                Log.i("Tor", "File Name $i: $fileName ${priorities[i]}")
            }
        }
    }

    var isDownloading = false
    fun initiateDownload(){
        if (isDownloading)
        {

        }
        else {
            isDownloading = true
            val torrentSessionOptions = TorrentSessionOptions(
                downloadLocation = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            )
            val torrentSession = TorrentSession(torrentSessionOptions)
            torrentSession.listener = this
//            torrentSession.start(applicationContext, Uri.parse("http://zan.ooguy.com/Maps.torrent"))
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
        if (Settings(this).mapDownloads().isNotEmpty())
        {
            initiateDownload()
        }

        return Service.START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onDestroy() {
        super.onDestroy()
    }


}
