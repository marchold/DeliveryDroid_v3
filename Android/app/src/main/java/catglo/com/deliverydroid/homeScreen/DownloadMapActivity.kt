package catglo.com.deliverydroid.homeScreen

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.google.android.gms.location.LocationServices
import kotlinx.android.synthetic.main.download_map_activity.*
import org.mapsforge.core.model.LatLong
import org.mapsforge.map.reader.MapFile
import java.io.*
import android.content.Intent
import android.net.Uri
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import catglo.com.deliverydroid.*
import catglo.com.deliverydroid.MapDownloadOption.*
import com.frostwire.jlibtorrent.Priority
import com.frostwire.jlibtorrent.TorrentHandle
import com.frostwire.jlibtorrent.TorrentInfo
import com.google.android.material.tabs.TabLayout
import com.masterwok.simpletorrentandroid.TorrentSession
import com.masterwok.simpletorrentandroid.TorrentSessionOptions
import com.masterwok.simpletorrentandroid.contracts.TorrentSessionListener
import com.masterwok.simpletorrentandroid.models.TorrentSessionStatus


import kotlinx.coroutines.*
import java.net.URL


fun Location.latLong() : LatLong{
    return LatLong(this.latitude,this.longitude)
}

data class DownloadableMap(val isFolder:Boolean,val title:String, val path: String = "", val list:ArrayList<DownloadableMap>? = null)

class DownloadableMapCell(view:View, map: DownloadableMap) : RecyclerView.ViewHolder(view){
    val title = view.findViewById<TextView>(android.R.id.text1)!!
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

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.download_map_activity)

        helpLink.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(helpLink.text.toString())))
        }
        mapDownloadRadioGroup.setOnCheckedChangeListener { _, checkedId ->
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
        when {
            Settings(this).mapDownloadOption == torrent -> automaticDownloadRadio.isChecked = true
            Settings(this).mapDownloadOption == download -> manualDownloadRadio.isChecked = true
            Settings(this).mapDownloadOption == create -> generateMapRadio.isChecked = true
        }
    }

    val WRITE_EXTERNAL_STORAGE_REQUEST_CODE: Int = 10001

    var listOfDownloadableMaps = ArrayList<DownloadableMap>()

    override fun onResume() {
        super.onResume()

       tabLayout.setOnTabSelectedListener(object:TabLayout.OnTabSelectedListener{
           override fun onTabReselected(p0: TabLayout.Tab?) {
               //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
           }

           override fun onTabUnselected(p0: TabLayout.Tab?) {
               //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
           }

           override fun onTabSelected(tab: TabLayout.Tab?) {
               if (tab?.position==0)
               {
                   downloadedMapList.visibility = View.VISIBLE
                   downloadableMapList.visibility = View.GONE
               }
               else {
                   downloadedMapList.visibility = View.GONE
                   downloadableMapList.visibility = View.VISIBLE
               }
           }
       })
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), WRITE_EXTERNAL_STORAGE_REQUEST_CODE)
        }
        else {
            val permission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            if (permission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),WRITE_EXTERNAL_STORAGE_REQUEST_CODE)
            }
            else {
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
                }

                if (Settings(this).mapDownloadOption == torrent) {

                    GlobalScope.launch(Dispatchers.Default){
                        //Download file


                        val torrentUri = Uri.parse("http://zan.ooguy.com/Maps.torrent")
                        val localTorrentFile = File( Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),"Maps.torrent")

                        URL("http://zan.ooguy.com/Maps.torrent").openStream()?.use { input ->
                            FileOutputStream(localTorrentFile)?.use { output ->
                                input.copyTo(output)
                            }
                        }

                        val torrentInfo = TorrentInfo(localTorrentFile)

                        val totalFiles = torrentInfo.numFiles()
                        if (totalFiles>0) {
                            for (i in 0..(totalFiles - 1)) {
                                val fileName = torrentInfo.files().fileName(i)
                                Log.i("Torrent","File Name $fileName")
                                val path = torrentInfo.files().filePath(i)
                            }
                        }

                        val torrentSessionOptions = TorrentSessionOptions(
                            downloadLocation = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                          //  ,enableLogging = true
                        )

                        val torrentSession = TorrentSession(torrentSessionOptions)



                        torrentSession.listener = object : TorrentSessionListener {
                            override fun onAddTorrent(
                                torrentHandle: TorrentHandle,
                                torrentSessionStatus: TorrentSessionStatus
                            ) {

                                Log.i("Torrent","onAddTorrent");
                                setTorrent(torrentHandle,torrentSessionStatus)
                                //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                            }

                            override fun onBlockUploaded(
                                torrentHandle: TorrentHandle,
                                torrentSessionStatus: TorrentSessionStatus
                            ) {
                                Log.i("Torrent","onBlockUploaded");
                                //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                            }

                            override fun onMetadataFailed(
                                torrentHandle: TorrentHandle,
                                torrentSessionStatus: TorrentSessionStatus
                            ) {
                                Log.i("Torrent","onMetadataFailed")
                               // TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                            }

                            override fun onMetadataReceived(
                                torrentHandle: TorrentHandle,
                                torrentSessionStatus: TorrentSessionStatus
                            ) {

                                setTorrent(torrentHandle,torrentSessionStatus)

                                Log.i("Torrent","onMetadataReceived")
                              //  TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                            }

                            override fun onTorrentDeleteFailed(
                                torrentHandle: TorrentHandle,
                                torrentSessionStatus: TorrentSessionStatus
                            ) {
                                Log.i("Torrent","onTorrentDeleteFailed")
                             //   TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                            }

                            override fun onTorrentDeleted(
                                torrentHandle: TorrentHandle,
                                torrentSessionStatus: TorrentSessionStatus
                            ) {
                                Log.i("Torrent","onTorrentDeleted")
                             //   TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                            }

                            override fun onTorrentError(
                                torrentHandle: TorrentHandle,
                                torrentSessionStatus: TorrentSessionStatus
                            ) {
                                Log.e("Torrent","onTorrentError")
                              //  TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                            }

                            override fun onTorrentFinished(
                                torrentHandle: TorrentHandle,
                                torrentSessionStatus: TorrentSessionStatus
                            ) {
                                Log.i("Torrent","onTorrentFinished")
                             //   TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                            }

                            override fun onTorrentPaused(
                                torrentHandle: TorrentHandle,
                                torrentSessionStatus: TorrentSessionStatus
                            ) {
                                Log.i("Torrent","onTorrentPaused")
                                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                            }

                            override fun onTorrentRemoved(
                                torrentHandle: TorrentHandle,
                                torrentSessionStatus: TorrentSessionStatus
                            ) {
                                Log.i("Torrent","onTorrentRemoved")
                              //  TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                            }

                            override fun onTorrentResumed(
                                torrentHandle: TorrentHandle,
                                torrentSessionStatus: TorrentSessionStatus
                            ) {
                                Log.i("Torrent","onTorrentResumed")
                             //   TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                                setTorrent(torrentHandle,torrentSessionStatus)
                            }

                            // Omitted for brevity

                            override fun onPieceFinished(
                                torrentHandle: TorrentHandle
                                , torrentSessionStatus: TorrentSessionStatus
                            ) {
                                Log.i("Torrent","onPieceFinished "+torrentSessionStatus.state)

                            }
                        }

                        torrentSession.start(this@DownloadMapActivity, torrentUri) // Invoke on background thread


                       /* val torrent = SharedTorrent.fromFile(
                            File(filesDir,"Maps.torrent"),
                            getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS))



                        torrentClient = Client(InetAddress.getLocalHost(),torrent)


                        torrentClient?.download()

                        torrentClient?.share()

                        torrentClient?.addObserver { observable, _ ->
                            val client = observable as Client
                            val progress = client.torrent.completion
                            Log.i("Torrent","Progress $progress ")
                        }

                        listOfDownloadableMaps.clear()
                        var lastFolder = ""
                        var hashMap = HashMap<String,ArrayList<String>>()
                        torrentClient?.torrent?.filenames?.forEach {
                            val parts = it.split("/")
                            if (parts.size>1) {
                                val title = parts[parts.lastIndex]
                                val folder = parts[parts.lastIndex - 1]
                                if (folder == "MapsforgeMaps")
                                {
                                    listOfDownloadableMaps.add(DownloadableMap(false,title,it))
                                }
                                else
                                if (lastFolder != folder)
                                {
                                    hashMap[folder] = ArrayList()
                                }
                            }
                        }
                        torrentClient?.torrent?.filenames?.forEach {
                            val parts = it.split("/")
                            if (parts.size>1) {
                                val folder = parts[parts.lastIndex - 1]
                                hashMap[folder]?.add(it)
                            }
                        }
                        hashMap.keys.forEach { key ->
                            val children = ArrayList<DownloadableMap>()
                            hashMap[key]?.forEach {
                                val parts = it.split("/")
                                val title = parts[parts.lastIndex].split(".")[0]
                                children.add(DownloadableMap(false,title,it))
                            }
                            listOfDownloadableMaps.add(DownloadableMap(true,key,"",children))
                        }

                        launch(Dispatchers.Main) {
                            val adapter = DownloadableAdapter(this@DownloadMapActivity,listOfDownloadableMaps)
                            downloadableMapList.adapter = adapter

                        }

                        torrentClient?.waitForCompletion()*/

                    }
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

class DownloadableAdapter(val context:Context, val maps : ArrayList<DownloadableMap>):RecyclerView.Adapter<DownloadableMapCell>()
{
    var mapList = maps
    override fun onCreateViewHolder(view: ViewGroup, position: Int): DownloadableMapCell {
        return DownloadableMapCell(View.inflate(context,android.R.layout.simple_list_item_1,null),mapList[position])
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
            cell.itemView.setOnClickListener {
                mapList = map.list!!
                notifyDataSetChanged()
            }
        }
        else
        {
            cell.itemView.setOnClickListener {

            }
        }
    }
}