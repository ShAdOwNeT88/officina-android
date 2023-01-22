package com.officinasocialeproarpaia.officina_android.features.dashboard

import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.officinasocialeproarpaia.officina_android.R
import com.officinasocialeproarpaia.officina_android.databinding.FragmentDashboardBinding
import com.officinasocialeproarpaia.officina_android.features.main.domain.MonumentConfig
import com.officinasocialeproarpaia.officina_android.utils.exhaustive
import java.io.IOException
import java.util.Locale
import org.altbeacon.beacon.Beacon
import org.altbeacon.beacon.BeaconConsumer
import org.altbeacon.beacon.BeaconManager
import org.altbeacon.beacon.BeaconParser
import org.altbeacon.beacon.Region
import org.koin.android.ext.android.inject
import timber.log.Timber

class DashboardFragment : Fragment(), BeaconConsumer {
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root
        setupDashboardViewModel()
        setBeaconManager()

        dashboardViewModel.send(DashboardEvent.RetrieveMonumentsConfig(resources.openRawResource(R.raw.officine_monuments_config)))
        binding.mediaPlay.setOnClickListener { play() }
        binding.mediaPause.setOnClickListener { pause() }

        return root
    }

    private fun setupDashboardViewModel() {
        dashboardViewModel.observe(lifecycleScope) {
            when (it) {
                is DashboardState.InProgress -> Timber.e("In Progress...need to be implemented")
                is DashboardState.Error -> Timber.e("Error ${it.error.message}")
                is DashboardState.RetrievedMonumentsConfig -> monumentConfig = it.monumentsConfig
            }.exhaustive
        }
    }

    private fun updateTrackTimerAndBar() {
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
//        durationHandler.postDelayed(updateSeekBarTime, 100)
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
                    //get correct url of the audio track based on Device locale language
                    val audioTrack = monument.monumentAudioUrls.first { audioTrack ->
                        audioTrack.language.equals(other = Locale.getDefault().language, ignoreCase = true)
                    }
                    addTrackToAudioPlayer(audioTrack)
                    updateTrackTimerAndBar()
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        mediaPlayer.release()
        beaconManager.unbind(this)
    }

    override fun onPause() {
        super.onPause()
        beaconManager.unbind(this)
    }

    override fun onBeaconServiceConnect() {}

    override fun getApplicationContext(): Context {
        return requireContext()
    }

    override fun unbindService(connection: ServiceConnection?) {}

    override fun bindService(intent: Intent?, connection: ServiceConnection?, mode: Int): Boolean {
        return false
    }
}
