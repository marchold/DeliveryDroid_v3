package catglo.com.deliverydroid.homeScreen

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import catglo.com.deliverydroid.R
import com.google.android.gms.maps.model.LatLngBounds
import kotlinx.android.synthetic.main.download_map_activity.*
import java.io.File

data class DownloadedMap(var title: String, var mapFile:File? = null, var poiFile: File? = null, var bounds: LatLngBounds?=null)

class DownloadMapCell(view:View, map: DownloadedMap) : RecyclerView.ViewHolder(view){
    val title = view.findViewById<TextView>(R.id.mapTitle)!!
    val hasMap = view.findViewById<TextView>(R.id.hasMap)!!
    val hasPoi = view.findViewById<TextView>(R.id.hasPoi)!!
}

class DownloadMapActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.download_map_activity)
    }

    override fun onResume() {
        super.onResume();

        //Scan the sd card folder for .map and .poi files
        val mapFilesList = ArrayList<DownloadedMap>()
        mapFilesList.run {
            addAll(searchForMapFiles(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)))
            addAll(searchForMapFiles(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)))
            addAll(searchForMapFiles(Environment.getExternalStorageDirectory()))

        }
        if (mapFilesList.size>0) {
            downloadedMapList.adapter = DownloadMapAdapter(this, mapFilesList)
            downloadedMapList.visibility = View.VISIBLE
            noDownloadsHelpView.visibility = View.GONE
        }
    }

    fun searchForMapFiles(pathToSearch: File) : ArrayList<DownloadedMap>
    {
        var result =  ArrayList<DownloadedMap>()
        var mapFile = HashMap<String,DownloadedMap>()
        if (pathToSearch.exists() && pathToSearch.isDirectory)
        {
            val fileList = pathToSearch.listFiles()
            fileList?.forEach {
             Log.i("DD",it.absolutePath)
                if (it.extension == "map")
                {
                    var map = mapFile[it.nameWithoutExtension]
                    if (map==null) map=DownloadedMap(it.nameWithoutExtension)
                    map.mapFile = it
                    mapFile[it.nameWithoutExtension] = map

                }
                if (it.extension == "poi")
                {
                    var map = mapFile[it.nameWithoutExtension]
                    if (map==null) map=DownloadedMap(it.nameWithoutExtension)
                    map.poiFile = it
                    mapFile[it.nameWithoutExtension] = map
                }
            }
            result.addAll(mapFile.values)
        }
        return result
    }
}

class DownloadMapAdapter(val context:Context, val maps : ArrayList<DownloadedMap>):RecyclerView.Adapter<DownloadMapCell>()
{
    override fun onCreateViewHolder(view: ViewGroup, position: Int): DownloadMapCell {
        return DownloadMapCell(View.inflate(context,R.layout.download_map_cell,null),maps[position])
    }

    override fun getItemCount(): Int {
        return maps.size
    }

    override fun onBindViewHolder(cell: DownloadMapCell, position: Int) {
        val map = maps[position]
        cell.title.text = map.title
        if (map.poiFile != null)
        {
            cell.hasPoi.visibility = View.VISIBLE
        }
        else
        {
            cell.hasPoi.visibility = View.GONE
        }
        if (map.mapFile != null)
        {
            cell.hasMap.visibility = View.VISIBLE
        }
        else
        {
            cell.hasMap.visibility = View.GONE
        }
    }

}