package com.officinasocialeproarpaia.officina_android.features.dashboard

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.location.LocationManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterManager
import com.officinasocialeproarpaia.officina_android.R
import com.officinasocialeproarpaia.officina_android.databinding.FragmentDashboardBinding
import com.officinasocialeproarpaia.officina_android.features.dashboard.adapters.MarkerClusterRenderer
import com.officinasocialeproarpaia.officina_android.features.dashboard.domain.MonumentClusterItem
import com.officinasocialeproarpaia.officina_android.features.main.domain.MonumentConfig
import com.officinasocialeproarpaia.officina_android.utils.exhaustive
import com.officinasocialeproarpaia.officina_android.utils.showEnableLocationSettingDialog
import java.io.IOException
import java.util.Locale
import org.altbeacon.beacon.Beacon
import org.altbeacon.beacon.BeaconConsumer
import org.altbeacon.beacon.BeaconManager
import org.altbeacon.beacon.BeaconParser
import org.altbeacon.beacon.Region
import org.koin.android.ext.android.inject
import timber.log.Timber


private const val MAP_ZOOM = 12.0f
const val PERMISSION_REQUEST_COARSE_LOCATION = 23

class DashboardFragment : Fragment(), BeaconConsumer, OnMapReadyCallback, GoogleMap.OnMapClickListener {
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private lateinit var beaconManager: BeaconManager
    private val dashboardViewModel: DashboardViewModel by inject()
    private val mediaPlayer: MediaPlayer = MediaPlayer()
    private var timeElapsed: Double = 0.0
    private var finalTime: Double = 0.0
    private val durationHandler: Handler = Handler()
    private lateinit var updateSeekBarTime: Runnable
    private lateinit var monumentConfig: MonumentConfig
    private var currentTrack: MonumentConfig.Monument.MonumentAudioUrl? = null
    private lateinit var map: GoogleMap
    private lateinit var mapView: SupportMapFragment
    private lateinit var myLocation: LatLng
    private lateinit var clusterManager: ClusterManager<MonumentClusterItem>
    private lateinit var clusterRenderer: MarkerClusterRenderer

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root
        setupDashboardViewModel()

        mapView = childFragmentManager.findFragmentById(R.id.map_view) as SupportMapFragment
        mapView.getMapAsync(this)
        setMonumentsMap()

        return root
    }

    private fun setupDashboardViewModel() {
        dashboardViewModel.observe(lifecycleScope) {
            when (it) {
                is DashboardState.InProgress -> Timber.e("In Progress...need to be implemented")
                is DashboardState.Error -> Timber.e("Error ${it.error.message}")
                is DashboardState.RetrievedMonumentsConfig -> {
                    monumentConfig = it.monumentsConfig
                    setMarkers(it.monumentsConfig.monuments)
                    setBeaconManager()

                    binding.mediaPlay.setOnClickListener { play() }
                    binding.mediaPause.setOnClickListener { pause() }
                }
            }.exhaustive
        }
    }

    private fun setMonumentsMap() {
        val coarseLocationPermission = ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
        val fineLocationPermission = ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
        if (coarseLocationPermission != PackageManager.PERMISSION_GRANTED && fineLocationPermission != PackageManager.PERMISSION_GRANTED) {
            this.requestPermissions(
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSION_REQUEST_COARSE_LOCATION
            )
        }

        LocationServices.getFusedLocationProviderClient(requireActivity()).lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val latLngLocation = LatLng(location.latitude, location.longitude)
                myLocation = latLngLocation
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation, MAP_ZOOM))
            }
        }
    }

    private fun updateTrackInfo() {
        binding.trackName.text = currentTrack?.audioUrl?.split("/")?.last()

        updateSeekBarTime = object : Runnable {
            override fun run() {
                timeElapsed = mediaPlayer.currentPosition.toDouble()
                finalTime = mediaPlayer.duration.toDouble()
                val timeElapsedMin = (timeElapsed / 1000 / 60).toInt()
                val timeElapsedSec = (timeElapsed / 1000 % 60).toInt()
                val finalTimeMin = (finalTime / 1000 / 60).toInt()
                val finalTimeSec = (finalTime / 1000 % 60).toInt()

                binding.seekBar.max = finalTime.toInt()
                binding.seekBar.progress = timeElapsed.toInt()

                binding.trackDuration.text = String.format(
                    Locale.getDefault(), resources.getString(R.string.audio_track_time), timeElapsedMin, timeElapsedSec, finalTimeMin, finalTimeSec
                )

                durationHandler.postDelayed(this, 100)
            }
        }
    }

    private fun addTrackToAudioPlayer(audioTrack: MonumentConfig.Monument.MonumentAudioUrl) {
        try {
            if (currentTrack != audioTrack) {
                currentTrack = audioTrack
                mediaPlayer.setDataSource(audioTrack.audioUrl)
                mediaPlayer.prepare()
                timeElapsed = mediaPlayer.currentPosition.toDouble()
                finalTime = mediaPlayer.duration.toDouble()
                play()
            }
        } catch (e: IOException) {
            Timber.e("Exception during media player init $e")
        }
    }

    private fun play() {
        if (mediaPlayer.isPlaying.not()) {
            mediaPlayer.start()
        }
        binding.seekBar.max = finalTime.toInt()
        binding.seekBar.progress = timeElapsed.toInt()
        durationHandler.postDelayed(updateSeekBarTime, 100)
    }

    private fun pause() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.pause()
        }
    }

    private fun stop() {
        mediaPlayer.stop()
        mediaPlayer.reset()
        currentTrack = null
        binding.trackName.text = ""
        binding.seekBar.progress = 0
        binding.trackDuration.text = String.format(Locale.getDefault(), resources.getString(R.string.audio_track_time), 0, 0, 0, 0)
    }

    private fun setBeaconManager() {
        beaconManager = BeaconManager.getInstanceForApplication(this.requireContext())
        //BeaconManager.setDebug(true)

        //Eddystone Beacons
        beaconManager.beaconParsers.add(BeaconParser().setBeaconLayout(BeaconParser.EDDYSTONE_UID_LAYOUT))
        beaconManager.beaconParsers.add(BeaconParser().setBeaconLayout(BeaconParser.EDDYSTONE_TLM_LAYOUT))
        beaconManager.beaconParsers.add(BeaconParser().setBeaconLayout(BeaconParser.EDDYSTONE_URL_LAYOUT))
        //AltBeacon
        beaconManager.beaconParsers.add(BeaconParser().setBeaconLayout(BeaconParser.ALTBEACON_LAYOUT))
        //URIBeacon
        beaconManager.beaconParsers.add(BeaconParser().setBeaconLayout(BeaconParser.URI_BEACON_LAYOUT))
        // The example shows how to find iBeacon.
        beaconManager.beaconParsers.add(BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"))

        //id1: 0x00626c75657570626561636f6e7307 about 1.281532517328134 meters away
        //Beacon data identifiers [0x00626c75657570626561636f6e7307] rssi -75 txPower -69 bluetoothAddress CD:6F:88:9C:1C:44 beaconTypeCode 16 serviceUuid 65194 serviceUuid128Bit [B@4a08072
        //Beacon data manufacturer 65194 bluetoothName BlueUp-01-011671 parserIdentifier null isMultiFrameBeacon false runningAverageRssi -74.71428571428571
        //Beacon data measurementCount 9 packetCount 1 firstCycleDetectionTimestamp 1674385070391 lastCycleDetectionTimestamp 1674385070391
        val rangingObserver = Observer<Collection<Beacon>> { beacons ->
            Timber.w("Ranged: ${beacons.count()} beacons")
            beacons.forEach { beacon ->
                val monument = monumentConfig.monuments.first { it.beaconId == beacon.id1.toString() }
                if (beacon.distance < monument.trackStartRange) {
                    setMonumentAudioTrack(monument)
                } else if (beacon.distance > monument.trackStopRange) {
                    stop()
                } else {
                    Timber.w("The monument is too far away so please get closer BeaconName: ${beacon.bluetoothName} BeaconId: ${beacon.id1}")
                }
            }

        }

        val region = Region("all-beacons-region", null, null, null)
        beaconManager.getRegionViewModel(region).rangedBeacons.observe(this.requireActivity(), rangingObserver)
        beaconManager.startRangingBeacons(region)
    }

    private fun setMonumentAudioTrack(monument: MonumentConfig.Monument) {
        //get correct url of the audio track based on Device locale language
        val audioTrack = monument.monumentAudioUrls.first { audioTrack -> audioTrack.language.equals(other = Locale.getDefault().language, ignoreCase = true) }
        updateTrackInfo()
        addTrackToAudioPlayer(audioTrack)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        mediaPlayer.release()
        beaconManager.unbind(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (this::mapView.isInitialized) {
            mapView.onDestroy()
        }
    }

    override fun onPause() {
        super.onPause()
        beaconManager.unbind(this)
        if (this::mapView.isInitialized) {
            mapView.onPause()
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        if (this::mapView.isInitialized) {
            mapView.onLowMemory()
        }
    }

    override fun onBeaconServiceConnect() {}

    override fun getApplicationContext(): Context {
        return requireContext()
    }

    override fun unbindService(connection: ServiceConnection?) {}

    override fun bindService(intent: Intent?, connection: ServiceConnection?, mode: Int): Boolean {
        return false
    }

    private fun setMarkers(monuments: List<MonumentConfig.Monument>) {
        clusterManager.clearItems()
        monuments.forEach { monument -> clusterManager.addItem(MonumentClusterItem(monument)) }
        clusterManager.cluster()
    }

    override fun onMapReady(p0: GoogleMap) {
        map = p0
        map.mapType = GoogleMap.MAP_TYPE_NORMAL
        //applyMapStyle(map)

        map.uiSettings.isMyLocationButtonEnabled = false
        map.moveCamera(CameraUpdateFactory.zoomTo(MAP_ZOOM))
        map.setOnMapClickListener(this)

        clusterManager = ClusterManager<MonumentClusterItem>(requireContext(), map)
        clusterRenderer = MarkerClusterRenderer(requireContext(), map, clusterManager)
        clusterManager.renderer = clusterRenderer
        map.setOnCameraIdleListener(clusterManager)

        dashboardViewModel.send(DashboardEvent.RetrieveMonumentsConfig(resources.openRawResource(R.raw.officine_monuments_config)))

        setClusterClickListener()
        checkPermissions()
    }

    private fun setClusterClickListener() {
        clusterManager.setOnClusterItemClickListener { selectedMonument ->
            Timber.e("Clicked on element with title ${selectedMonument.title}, ${selectedMonument.position}")
            //setSpaceDetail(selectedMonument)
            true
        }
    }

    /* private fun applyMapStyle(map: GoogleMap) {
         try {
             // Customise the styling of the base map using a JSON object defined
             // in a raw resource file.
             val success: Boolean = map.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map_style_json))
             if (!success) {
                 Timber.e("Style parsing failed.")
             }
         } catch (e: Resources.NotFoundException) {
             Timber.e("Can't find style. Error: $e")
         }
     }
 */
    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            map.isMyLocationEnabled = true
            LocationServices.getFusedLocationProviderClient(requireActivity()).lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    myLocation = LatLng(location.latitude, location.longitude)
                }
                checkGpsStatus()
            }
        } else {
            this.requestPermissions(
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSION_REQUEST_COARSE_LOCATION
            )
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSION_REQUEST_COARSE_LOCATION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    val coarseLocationPermission = ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                    val fineLocationPermission = ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                    if (coarseLocationPermission == PackageManager.PERMISSION_GRANTED && fineLocationPermission == PackageManager.PERMISSION_GRANTED) {
                        map.isMyLocationEnabled = true
                        checkGpsStatus()
                    }
                }
                return
            }
        }
    }

    private fun checkGpsStatus() {
        val locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val gpsStatus = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if (!gpsStatus) {
            requireActivity().showEnableLocationSettingDialog()
        }
    }

    override fun onMapClick(p0: LatLng) {
        TODO("Not yet implemented")
    }
}
