package catglo.com.deliverydroid.homeScreen

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.ActivityCompat

import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import catglo.com.deliverydroid.DeliveryDroidBaseActivity
import catglo.com.deliverydroid.DownloadedMap
import catglo.com.deliverydroid.R
import com.google.android.gms.location.LocationServices
import kotlinx.android.synthetic.main.download_map_activity.*
import org.mapsforge.core.model.LatLong
import org.mapsforge.map.reader.MapFile
import java.io.*

fun Location.latLong() : LatLong{
    return LatLong(this.latitude,this.longitude)
}


class DownloadMapCell(view:View, map: DownloadedMap) : RecyclerView.ViewHolder(view){
    val title = view.findViewById<TextView>(R.id.mapTitle)!!
    val hasMap = view.findViewById<ImageView>(R.id.hasMap)!!
    val hasPoi = view.findViewById<ImageView>(R.id.hasPoi)!!
    val warningIcon = view.findViewById<ImageView>(R.id.warningIcon)!!
    val warningText = view.findViewById<TextView>(R.id.warningText)!!
    val checkmark = view.findViewById<ImageView>(R.id.checkmark)!!
}

class DownloadMapActivity : DeliveryDroidBaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.download_map_activity)
    }

    val WRITE_EXTERNAL_STORAGE_REQUEST_CODE: Int = 10001

    override fun onResume() {
        super.onResume()

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), WRITE_EXTERNAL_STORAGE_REQUEST_CODE)
        }
        else {

            val permission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            if (permission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    WRITE_EXTERNAL_STORAGE_REQUEST_CODE
                )
            } else {
                //Scan the sd card folder for .map and .poi files
                var mapFilesList = ArrayList<DownloadedMap>()
                mapFilesList.run {
                    addAll(searchForMapFiles(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)))
                    addAll(searchForMapFiles(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)))
                    addAll(searchForMapFiles(Environment.getExternalStorageDirectory()))

                }
                var oldList = DownloadedMap.loadMapsList(this)
                var map = HashMap<String, DownloadedMap>()
                var mapOld = HashMap<String, DownloadedMap>()
                oldList.forEach {
                    if (it.mapFile != null) {
                        mapOld[it.mapFile!!.nameWithoutExtension] = it
                    } else if (it.poiFile != null) {
                        mapOld[it.poiFile!!.nameWithoutExtension] = it
                    }
                }
                mapFilesList.forEach {
                    var key: String? = null
                    if (it.mapFile != null) {
                        key = it.mapFile!!.nameWithoutExtension
                    } else if (it.poiFile != null) {
                        key = it.poiFile!!.nameWithoutExtension
                    }
                    if (key != null) {
                        if (mapOld[key] != null) {
                            mapOld[key]?.title?.let { oldTitle ->
                                it.title = oldTitle
                            }
                        }
                        map[key] = it
                    }
                }

                mapFilesList = ArrayList(map.values)

                if (mapFilesList.size > 0) {
                    mapFilesList.forEach { downloadedMap ->
                        //Load the map files and verify the bounds
                        if (downloadedMap.mapFile != null) {
                            val mapFile = MapFile(downloadedMap.mapFile)
                            downloadedMap.bounds = mapFile.boundingBox()
                        }
                    }
                    DownloadedMap.saveMapsList(this, mapFilesList)
                    downloadedMapList.adapter = DownloadMapAdapter(this, mapFilesList)
                    downloadedMapList.visibility = View.VISIBLE
                    noDownloadsHelpView.visibility = View.GONE
                }
            }
        }
    }

    fun searchForMapFiles(pathToSearch: File) : ArrayList<DownloadedMap>
    {
        var result =  ArrayList<DownloadedMap>()
        var mapFile = HashMap<String, DownloadedMap>()
        if (pathToSearch.exists() && pathToSearch.isDirectory)
        {
            val fileList = pathToSearch.listFiles()
            fileList?.forEach {
             Log.i("DD",it.absolutePath)
                if (it.extension == "map")
                {
                    var map = mapFile[it.nameWithoutExtension]
                    if (map==null) map= DownloadedMap(it.nameWithoutExtension)
                    map.mapFile = it
                    mapFile[it.nameWithoutExtension] = map

                }
                if (it.extension == "poi")
                {
                    var map = mapFile[it.nameWithoutExtension]
                    if (map==null) map= DownloadedMap(it.nameWithoutExtension)
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



    @SuppressLint("MissingPermission")
    override fun onBindViewHolder(cell: DownloadMapCell, position: Int) {
        val map = maps[position]
        cell.title.text = map.title
        if (map.poiFile==null && map.mapFile==null)
        {
            cell.hasPoi.visibility = View.GONE
            cell.hasMap.visibility = View.GONE
            cell.warningText.text = if (map.poiFile == null) { "Missing POI file" } else { "Missing MAP file" }
            cell.warningText.visibility = View.VISIBLE
            cell.warningIcon.visibility = View.VISIBLE
        }
        else
        {
            cell.warningText.visibility = View.GONE
            cell.warningIcon.visibility = View.GONE
            if (map.poiFile != null) {
                cell.hasPoi.visibility = View.VISIBLE
            } else {
                cell.hasPoi.visibility = View.GONE
            }
            if (map.mapFile != null) {
                cell.hasMap.visibility = View.VISIBLE
            } else {
                cell.hasMap.visibility = View.GONE
            }
        }
        cell.checkmark.visibility = View.INVISIBLE
        val locationProvider = LocationServices.getFusedLocationProviderClient(context)
        locationProvider.lastLocation.addOnSuccessListener { location ->
            if (map.bounds != null) {
                if (map.bounds!!.contains(location.latLong()))
                {
                    cell.checkmark.visibility = View.VISIBLE
                }
            }
        }
    }
}