package com.officinasocialeproarpaia.officina_android.features.dashboard

import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.officinasocialeproarpaia.officina_android.R
import com.officinasocialeproarpaia.officina_android.databinding.FragmentDashboardBinding
import com.officinasocialeproarpaia.officina_android.utils.exhaustive
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root
        setupDashboardViewModel()
        setBeaconManager()

        dashboardViewModel.send(DashboardEvent.RetrieveMonumentsConfig(resources.openRawResource(R.raw.officine_monuments_config)))

        val textView: TextView = binding.textDashboard

        return root
    }

    private fun setupDashboardViewModel() {
        dashboardViewModel.observe(lifecycleScope) {
            when (it) {
                is DashboardState.InProgress -> Timber.e("In Progress...need to be implemented")
                is DashboardState.Error -> Timber.e("Error ${it.error.message}")
                is DashboardState.RetrievedMonumentsConfig -> Timber.e("Retrieved monument config ${it.monumentsConfig.monuments}")
            }.exhaustive
        }
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

        val rangingObserver = Observer<Collection<Beacon>> { beacons ->
            Timber.e("Ranged: ${beacons.count()} beacons")
            for (beacon: Beacon in beacons) {
                Timber.e("$beacon about ${beacon.distance} meters away")
                Timber.w("Beacon data ${beacon.identifiers} ${beacon.rssi} ${beacon.txPower} ${beacon.bluetoothAddress} ${beacon.beaconTypeCode}${beacon.serviceUuid} ${beacon.serviceUuid128Bit} ${beacon.manufacturer} ${beacon.bluetoothName} ${beacon.parserIdentifier} ${beacon.isMultiFrameBeacon} ${beacon.runningAverageRssi} ${beacon.measurementCount} ${beacon.packetCount} ${beacon.firstCycleDetectionTimestamp}${beacon.lastCycleDetectionTimestamp}")
            }
        }

        val region = Region("all-beacons-region", null, null, null)
        beaconManager.getRegionViewModel(region).rangedBeacons.observe(this.requireActivity(), rangingObserver)
        beaconManager.startRangingBeacons(region)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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