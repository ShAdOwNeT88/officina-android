package com.officinasocialeproarpaia.officina_android.features.dashboard.domain

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem
import com.officinasocialeproarpaia.officina_android.features.main.domain.MonumentConfig
import java.util.Locale

class MonumentClusterItem(private val monument: MonumentConfig.Monument) : ClusterItem {

    //Need to check if latitude and longitude are correct, in case of null we could set 0.0 for both parameters
    private val position: LatLng = if (monument.location != null) {
        LatLng(monument.location.latitude, monument.location.longitude)
    } else {
        LatLng(0.0, 0.0)
    }

    override fun getPosition(): LatLng = position

    override fun getTitle(): String = monument.monumentName

    override fun getSnippet(): String =
        monument.monumentDescriptions.first { it.language.equals(other = Locale.getDefault().language, ignoreCase = true) }.description
}
