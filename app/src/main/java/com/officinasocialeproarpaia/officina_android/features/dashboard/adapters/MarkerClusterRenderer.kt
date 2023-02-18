package com.officinasocialeproarpaia.officina_android.features.dashboard.adapters

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Typeface
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.google.maps.android.ui.IconGenerator
import com.google.maps.android.ui.SquareTextView
import com.officinasocialeproarpaia.officina_android.R
import com.officinasocialeproarpaia.officina_android.features.dashboard.domain.MonumentClusterItem

private const val DENSITY_MULTIPLIER = 6
private const val LEFT_MULTIPLIER = 3
private const val BOTTOM_MULTIPLIER = 2

class MarkerClusterRenderer(private val context: Context, map: GoogleMap?, clusterManager: ClusterManager<MonumentClusterItem>) :
    DefaultClusterRenderer<MonumentClusterItem>(context, map, clusterManager) {

    private val clusterIconGenerator = IconGenerator(context)
    private val mDensity = context.resources.displayMetrics.density

    override fun onBeforeClusterItemRendered(item: MonumentClusterItem, markerOptions: MarkerOptions) {
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_monument_marker))
        markerOptions.title(item.title)
    }

    override fun getDescriptorForCluster(cluster: Cluster<MonumentClusterItem>): BitmapDescriptor {
        clusterIconGenerator.setBackground(ContextCompat.getDrawable(context, R.drawable.ic_cluster))
        clusterIconGenerator.setContentView(makeSquareTextView(context))

        val icon: Bitmap = clusterIconGenerator.makeIcon(cluster.size.toString())
        return BitmapDescriptorFactory.fromBitmap(icon)
    }

    private fun makeSquareTextView(context: Context): SquareTextView {

        val squareTextView = SquareTextView(context)
        squareTextView.setTextColor(ContextCompat.getColor(context, R.color.cluster_icon_text))
        squareTextView.setTypeface(null, Typeface.BOLD)
        val layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        squareTextView.layoutParams = layoutParams
        squareTextView.id = R.id.amu_text
        val twelveDpi = (DENSITY_MULTIPLIER * mDensity).toInt()
        squareTextView.setPadding(twelveDpi * LEFT_MULTIPLIER, twelveDpi, twelveDpi, twelveDpi * BOTTOM_MULTIPLIER)

        return squareTextView
    }
}
