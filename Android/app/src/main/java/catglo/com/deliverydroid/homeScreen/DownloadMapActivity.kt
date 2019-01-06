package catglo.com.deliverydroid.homeScreen


import android.Manifest.permission.*
import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.IBinder
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat.checkSelfPermission
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.recyclerview.widget.RecyclerView
import catglo.com.MapFileSharingStatus
import catglo.com.MapSharingServiceStatusListener
import catglo.com.deliverydroid.DeliveryDroidBaseActivity
import catglo.com.deliverydroid.DownloadedMap
import catglo.com.deliverydroid.MapDownloadOption.*
import catglo.com.deliverydroid.R
import catglo.com.deliverydroid.Settings
import com.google.android.gms.location.LocationServices
import kotlinx.android.synthetic.main.download_map_activity.*
import org.mapsforge.core.model.LatLong
import org.mapsforge.map.reader.MapFile
import org.mapsforge.map.reader.header.MapFileException
import java.io.File
import java.io.Serializable


fun Location.latLong(): LatLong {
    return LatLong(this.latitude, this.longitude)
}


class DownloadMapCell(view: View, map: DownloadedMap) : RecyclerView.ViewHolder(view) {
    val title = view.findViewById<TextView>(R.id.mapTitle)!!
    val hasMap = view.findViewById<ImageView>(R.id.hasMap)!!
    val hasPoi = view.findViewById<ImageView>(R.id.hasPoi)!!
    val warningIcon = view.findViewById<ImageView>(R.id.warningIcon)!!
    val warningText = view.findViewById<TextView>(R.id.warningText)!!
    val checkmark = view.findViewById<ImageView>(R.id.checkmark)!!
}

class DownloadMapActivity : DeliveryDroidBaseActivity() {

    @SuppressLint("SetTextI18n")
    fun mapOptionsDialog() {
        val optionsView = View.inflate(this, R.layout.download_map_options, null)
        val helpText = optionsView.findViewById<TextView>(R.id.helpText)
        val helpLink = optionsView.findViewById<TextView>(R.id.helpLink)
        when {
            Settings(this).mapDownloadOption == torrent -> optionsView.findViewById<RadioButton>(R.id.automaticDownloadRadio)
                .isChecked = true
            Settings(this).mapDownloadOption == download -> optionsView.findViewById<RadioButton>(R.id.manualDownloadRadio)
                .isChecked = true
            Settings(this).mapDownloadOption == create -> optionsView.findViewById<RadioButton>(R.id.generateMapRadio)
                .isChecked = true
        }
        optionsView.findViewById<RadioGroup>(R.id.mapDownloadRadioGroup)
            .setOnCheckedChangeListener { group, checkedId ->
                when (checkedId) {
                    R.id.automaticDownloadRadio -> {
                        helpText.setText(R.string.map_bittorrent_desc)
                        helpLink.visibility = View.GONE
                        Settings(this).mapDownloadOption = torrent
                    }
                    R.id.manualDownloadRadio -> {
                        helpText.text = getString(R.string.map_download_desc)
                        helpLink.text = "http://download.mapsforge.org/"
                        helpLink.visibility = View.VISIBLE
                        Settings(this).mapDownloadOption = download
                    }
                    R.id.generateMapRadio -> {
                        helpText.text = getString(R.string.map_create_desc)
                        helpLink.text = "https://github.com/mapsforge/mapsforge"
                        helpLink.visibility = View.VISIBLE
                        Settings(this).mapDownloadOption = create
                    }
                }
            }
        helpLink.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(helpLink.text.toString())))
        }
        AlertDialog.Builder(this)
            .setView(optionsView)
            .setPositiveButton(android.R.string.ok, null)
            .setOnDismissListener {
                if (Settings(this).mapDownloadOption == none) {
                    finish()
                }
                else {
                    scanFileSystem()
                }
            }
            .setTitle("Choose map source")
            .show()
    }

    private var progress: Float = 0f
    private var status: MapFileSharingStatus = MapFileSharingStatus.None
    inner class MapDownloadStatusListener : MapSharingServiceStatusListener() {
        override fun onMapStatus(progress: Float, status: MapFileSharingStatus) {
            this@DownloadMapActivity.progress = progress
            this@DownloadMapActivity.status = status
            updateUi()
        }
    }
    val broadcastReceiver = MapDownloadStatusListener()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.download_map_activity)
        setSupportActionBar(toolbar)
    }

    private var downloadMapButton: MenuItem? = null

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.download_map_activity, menu)
        downloadMapButton = menu?.findItem(R.id.download);
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.settings -> mapOptionsDialog()
            R.id.download -> startActivity(Intent(this,ChooseDownloadActivity::class.java))
        }
        return super.onOptionsItemSelected(item)
    }



    override fun onPause() {
        super.onPause()
        unregisterReceiver(broadcastReceiver)
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        super.onServiceConnected(name, service)
        mapSharingServce?.status?.let { status = it }
        mapSharingServce?.progress?.let { progress = it }
        updateUi()
    }

    override fun onResume() {
        super.onResume()
        MapSharingServiceStatusListener.registerReceiver(this,broadcastReceiver)

        //First we need to check for system permissions
        if (checkSelfPermission(this, ACCESS_FINE_LOCATION) != PERMISSION_GRANTED
            && checkSelfPermission(this, ACCESS_COARSE_LOCATION) != PERMISSION_GRANTED
        ) {
            requestPermissions(this, arrayOf(ACCESS_FINE_LOCATION), 0)
            return
        }
        if (checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) != PERMISSION_GRANTED) {
            requestPermissions(this, arrayOf(WRITE_EXTERNAL_STORAGE), 0)
            return
        }
        updateUi()
    }
    private fun updateUi(){

        //Make sure the user has chosen his preferred way of getting maps
        when (Settings(this).mapDownloadOption) {
            none -> {
                downloadMapButton?.isVisible = false
                mapOptionsDialog()
                return
            }

            torrent -> {
                val hasFiles = scanFileSystem()
                downloadMapButton?.isVisible = true

                when (status){
                    MapFileSharingStatus.None -> {
                        downloadHelpArea.visibility = if (hasFiles) View.GONE else View.VISIBLE
                    }
                    MapFileSharingStatus.Initializing -> {
                        downloadHelpArea.visibility = View.GONE
                        downloadProgressArea.visibility = View.VISIBLE
                        downloadProgressMessage.text = "Initializing"
                    }
                    MapFileSharingStatus.Downloading -> {
                        downloadHelpArea.visibility = View.GONE
                        downloadProgressArea.visibility = View.VISIBLE
                        if (progress==0f)
                        {
                            downloadProgressMessage.text = "Downloading"
                        } else {
                            downloadProgressMessage.text = String.format("Downloading %d%%", (progress * 100f).toInt())
                        }
                    }
                    MapFileSharingStatus.Uploading -> {
                        downloadHelpArea.visibility = View.GONE
                        downloadProgressArea.visibility = View.GONE
                    }
                    MapFileSharingStatus.Error -> {
                        downloadHelpArea.visibility = View.GONE
                        downloadProgressArea.visibility = View.VISIBLE
                        downloadProgressMessage.text = "Network Error Retrying"
                    }
                }

            }

            //If we have all the permissions we need the next thing is to scan for files on the file system
            else -> {
                downloadMapButton?.isVisible = false
                scanFileSystem()
            }
        }

    }

    private fun scanFileSystem() : Boolean {
        //Scan the sd card folder for .map and .poi files
        var mapFilesList = ArrayList<DownloadedMap>()
        mapFilesList.run {
            addAll(searchForMapFiles(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)))
            addAll(searchForMapFiles(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)))
            addAll(searchForMapFiles(Environment.getExternalStorageDirectory()))

        }
        val oldList = DownloadedMap.loadMapsList(this)
        val map = HashMap<String, DownloadedMap>()
        val mapOld = HashMap<String, DownloadedMap>()
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
                    try {
                        val mapFile = MapFile(downloadedMap.mapFile)
                        downloadedMap.bounds = mapFile.boundingBox()
                    } catch (e: MapFileException) {e.printStackTrace()}
                }
            }
            DownloadedMap.saveMapsList(this, mapFilesList)
            downloadedMapList.adapter = DownloadMapAdapter(this, mapFilesList)
            downloadedMapList.visibility = View.VISIBLE
        }
        else {
            downloadedMapList.visibility = View.GONE
        }
        setHelpText()
        return mapFilesList.size>0
    }

    private fun setHelpText() {
        when (Settings(this).mapDownloadOption) {
            none -> {
            }
            torrent -> {
                downloadHelpArea.visibility = View.VISIBLE
                helpView.visibility = View.GONE
                downloadHelpArea.setOnClickListener {
                    startActivity(Intent(this, ChooseDownloadActivity::class.java))
                }
            }
            download -> {
                downloadHelpArea.visibility = View.GONE
                helpView.visibility = View.VISIBLE
                helpText.setText(R.string.map_download_desc)
                helpLink.setText("")
            }
            create -> {
                downloadHelpArea.visibility = View.GONE
                helpView.visibility = View.VISIBLE
                helpText.setText(R.string.map_create_desc)
                helpLink.setText("")
            }
        }
    }

    fun searchForMapFiles(pathToSearch: File): ArrayList<DownloadedMap> {
        val result = ArrayList<DownloadedMap>()
        val mapFile = HashMap<String, DownloadedMap>()
        if (pathToSearch.exists() && pathToSearch.isDirectory) {
            val fileList = pathToSearch.listFiles()
            fileList?.forEach { file ->
                if (file.isDirectory && file.name=="MapsforgeMaps") {
                    //This is the torrent download folder
                    //we should search this folder for map files
                    result.addAll(searchForMapFiles((file)))
                    //and each of its subdirectories for map files
                    file.listFiles().forEach { if (it.isDirectory) result.addAll(searchForMapFiles(it)) }
                }
                else {
                    Log.i("DD", file.absolutePath)
                    if (file.extension == "map") {
                        val map = mapFile[file.nameWithoutExtension] ?: DownloadedMap(file.nameWithoutExtension)
                        map.mapFile = file
                        mapFile[file.nameWithoutExtension] = map

                    }
                    if (file.extension == "poi") {
                        val map = mapFile[file.nameWithoutExtension] ?: DownloadedMap(file.nameWithoutExtension)
                        map.poiFile = file
                        mapFile[file.nameWithoutExtension] = map
                    }
                }
            }
            result.addAll(mapFile.values)
        }
        return result
    }

}


class DownloadMapAdapter(val context: Context, val maps: ArrayList<DownloadedMap>) :
    RecyclerView.Adapter<DownloadMapCell>() {
    override fun onCreateViewHolder(view: ViewGroup, position: Int): DownloadMapCell {
        return DownloadMapCell(View.inflate(context, R.layout.download_map_cell, null), maps[position])
    }

    override fun getItemCount(): Int {
        return maps.size
    }


    @SuppressLint("MissingPermission")
    override fun onBindViewHolder(cell: DownloadMapCell, position: Int) {
        val map = maps[position]
        cell.title.text = map.title
        if (map.poiFile == null && map.mapFile == null) {
            cell.hasPoi.visibility = View.GONE
            cell.hasMap.visibility = View.GONE
            cell.warningText.text = if (map.poiFile == null) {
                "Missing POI file"
            } else {
                "Missing MAP file"
            }
            cell.warningText.visibility = View.VISIBLE
            cell.warningIcon.visibility = View.VISIBLE
        } else {
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
                if (map.bounds!!.contains(location.latLong())) {
                    cell.checkmark.visibility = View.VISIBLE
                }
            }
        }
    }
}
