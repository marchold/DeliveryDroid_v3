package catglo.com

import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.Binder
import android.os.Environment
import android.os.IBinder
import android.util.Log
import com.frostwire.jlibtorrent.Priority
import com.frostwire.jlibtorrent.TorrentHandle
import com.frostwire.jlibtorrent.TorrentInfo
import com.masterwok.simpletorrentandroid.TorrentSession
import com.masterwok.simpletorrentandroid.TorrentSessionOptions
import com.masterwok.simpletorrentandroid.contracts.TorrentSessionListener
import com.masterwok.simpletorrentandroid.models.TorrentSessionStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.net.URL

class MapFileSharingService : Service(),TorrentSessionListener {

    override fun onAddTorrent(torrentHandle: TorrentHandle, torrentSessionStatus: TorrentSessionStatus) {
        setTorrent(torrentHandle, torrentSessionStatus)
    }

    override fun onBlockUploaded(torrentHandle: TorrentHandle, torrentSessionStatus: TorrentSessionStatus) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onMetadataFailed(torrentHandle: TorrentHandle, torrentSessionStatus: TorrentSessionStatus) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onMetadataReceived(torrentHandle: TorrentHandle, torrentSessionStatus: TorrentSessionStatus) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onPieceFinished(torrentHandle: TorrentHandle, torrentSessionStatus: TorrentSessionStatus) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.

    }

    override fun onTorrentDeleteFailed(torrentHandle: TorrentHandle, torrentSessionStatus: TorrentSessionStatus) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onTorrentDeleted(torrentHandle: TorrentHandle, torrentSessionStatus: TorrentSessionStatus) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onTorrentError(torrentHandle: TorrentHandle, torrentSessionStatus: TorrentSessionStatus) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onTorrentFinished(torrentHandle: TorrentHandle, torrentSessionStatus: TorrentSessionStatus) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onTorrentPaused(torrentHandle: TorrentHandle, torrentSessionStatus: TorrentSessionStatus) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onTorrentRemoved(torrentHandle: TorrentHandle, torrentSessionStatus: TorrentSessionStatus) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onTorrentResumed(torrentHandle: TorrentHandle, torrentSessionStatus: TorrentSessionStatus) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun setTorrent( torrentHandle: TorrentHandle,torrentSessionStatus: TorrentSessionStatus){
        val torrentInfo = torrentHandle.torrentFile()
        if (torrentInfo==null) return

        val totalFiles = torrentInfo.numFiles()
        if (totalFiles>0) {
            var priorities = torrentHandle.filePriorities()
            for (i in 0..(totalFiles - 1)) {
                val fileName = torrentInfo.files().fileName(i)
                Log.i("Tor", "File Name $i: $fileName ${priorities[i]}")
                if (i == 5) {
                    priorities[i] = Priority.SEVEN
                } else {
                    priorities[i] = Priority.IGNORE
                }
            }
            torrentHandle.prioritizeFiles(priorities)

            priorities = torrentHandle.filePriorities()
            for (i in 0..(totalFiles - 1)) {
                val fileName = torrentInfo.files().fileName(i)
                Log.i("Tor", "File Name $i: $fileName ${priorities[i]}")

            }
        }
    }

    fun initiateDownload(){

        val torrentSessionOptions = TorrentSessionOptions(
            downloadLocation = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        )
        val torrentSession = TorrentSession(torrentSessionOptions)
        torrentSession.listener = this

    }


    private val myBinder = MyLocalBinder()
    override fun onBind(intent: Intent): IBinder? {
        return myBinder
    }
    inner class MyLocalBinder : Binder() {
        fun getService() : MapFileSharingService {
            return this@MapFileSharingService
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onDestroy() {
        super.onDestroy()
    }


}
