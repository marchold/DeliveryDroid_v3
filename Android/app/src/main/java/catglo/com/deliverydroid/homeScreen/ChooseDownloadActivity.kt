package catglo.com.deliverydroid.homeScreen

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import catglo.com.deliverydroid.DeliveryDroidBaseActivity
import catglo.com.deliverydroid.R
import catglo.com.deliverydroid.Settings
import com.frostwire.jlibtorrent.TorrentInfo
import kotlinx.android.synthetic.main.choose_download_activity.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URL

data class DownloadableMap(val isFolder:Boolean,val title:String, val path: String = "", val list:ArrayList<DownloadableMap>? = null)

class DownloadableMapCell(view: View, map: DownloadableMap) : RecyclerView.ViewHolder(view){
    val title = view.findViewById<TextView>(android.R.id.text1)!!
    val icon = view.findViewById<ImageView>(R.id.icon)!!
}

class ChooseDownloadActivity : DeliveryDroidBaseActivity() {

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.choose_download_activity)

        setSupportActionBar(toolbar)
        supportActionBar?.title = "Choose Download Area"
        supportActionBar?.setDefaultDisplayHomeAsUpEnabled(true)

        GlobalScope.launch(Dispatchers.Default){
            //Download torrent file
            val torrentUri = Uri.parse("http://zan.ooguy.com/Maps.torrent")
            val localTorrentFile = File( Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),"Maps.torrent")
            try {
                URL("http://zan.ooguy.com/Maps.torrent").openStream()?.use { input ->
                    FileOutputStream(localTorrentFile).use { output ->
                        input.copyTo(output)
                    }
                }
            } catch ( e : IOException)
            {
                //Servers down use the local copy
                assets.open("Maps.torrent").copyTo(FileOutputStream(localTorrentFile))
            }
            val filesList = ArrayList<DownloadableMap>()
            val torrentInfo = TorrentInfo(localTorrentFile)
            val totalFiles = torrentInfo.numFiles()
            var lastFolder = ""
            val hashMap = HashMap<String,ArrayList<String>>()
            if (totalFiles>0) {
                for (i in 0..(totalFiles - 1)) {
                    val fileName = torrentInfo.files().fileName(i)
                    Log.i("Torrent","File Name $fileName")
                    val path = torrentInfo.files().filePath(i)
                    val parts = path.split("/")
                    if (parts.size>1)
                    {
                        val folder = parts[parts.lastIndex-1]
                        val name = parts[parts.lastIndex]
                        if (lastFolder!=folder)
                        {
                            hashMap[folder] = ArrayList()
                        }
                        hashMap[folder]?.add(path)
                        lastFolder = folder
                    }
                }
            }
            hashMap.keys.forEach { key ->
                if (key=="MapsforgeMaps")
                {
                    hashMap[key]?.forEach { path ->
                        val parts = path.split("/")
                        val title = parts[parts.lastIndex].split(".")[0]
                        filesList.add(DownloadableMap(false,title.capitalize(),path))
                    }
                }
                else {
                    val subfolder = ArrayList<DownloadableMap>()
                    hashMap[key]?.forEach { path ->
                        val parts = path.split("/")
                        val title = parts[parts.lastIndex].split(".")[0]
                        subfolder.add(DownloadableMap(false,title.capitalize(),path))
                    }
                    filesList.add(DownloadableMap(true, key.capitalize(), "", subfolder))
                }
            }

            GlobalScope.launch(Dispatchers.Main) {
                listView.adapter = DownloadableAdapter(this@ChooseDownloadActivity,filesList)
            }
        }
    }

    override fun onBackPressed() {
        listView.adapter?.let {
            if (it is DownloadableAdapter && it.level>0) {
                it.reset()
                return
            }
        }
        super.onBackPressed()
    }
}


class DownloadableAdapter(val context: Context, val maps : ArrayList<DownloadableMap>):RecyclerView.Adapter<DownloadableMapCell>()
{
    var mapList = maps
    var level = 0
    fun reset()
    {
        mapList = maps
        notifyDataSetChanged()
        level=0
    }
    override fun onCreateViewHolder(view: ViewGroup, position: Int): DownloadableMapCell {
        return DownloadableMapCell(View.inflate(context,R.layout.map_download_list_option,null),mapList[position])
    }

    override fun getItemCount(): Int {
        return mapList.size
    }

    @SuppressLint("MissingPermission")
    override fun onBindViewHolder(cell: DownloadableMapCell, position: Int) {
        val map = mapList[position]
        cell.title.text = map.title
        if (map.isFolder)
        {
            cell.icon.setImageDrawable(context.getDrawable(R.drawable.folder))
            cell.itemView.setOnClickListener {
                mapList = map.list!!
                notifyDataSetChanged()
                level++
            }
        }
        else
        {
            if (Settings(context).mapDownloads().contains(map.path)) {
                cell.icon.setImageDrawable(context.getDrawable(R.drawable.map_minus))
            } else {
                cell.icon.setImageDrawable(context.getDrawable(R.drawable.map_add))
            }


            cell.itemView.setOnClickListener {
                Settings(context).run {
                    if (mapDownloads().contains(map.path)) {
                        removeMapDownload(map)
                    } else {
                        addMapDownload(map)
                    }
                }
                //TODO: Send a message to the service about the change
            }
        }
    }
}