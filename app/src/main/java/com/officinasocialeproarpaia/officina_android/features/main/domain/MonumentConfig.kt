package com.officinasocialeproarpaia.officina_android.features.main.domain

import com.google.gson.annotations.SerializedName

data class MonumentConfig(
    @SerializedName("monuments") val monuments: List<Monument>,
) {
    data class Monument(
        @SerializedName("beaconName") val beaconName: String,
        @SerializedName("beaconId") val beaconId: String,
        @SerializedName("trackStartRange") val trackStartRange: Double,
        @SerializedName("trackStopRange") val trackStopRange: Double,
        @SerializedName("monumentName") val monumentName: String,
        @SerializedName("monumentSubtitles") val monumentSubtitles: List<MonumentSubtitle>,
        @SerializedName("monumentAudioUrls") val monumentAudioUrls: List<MonumentAudioUrl>,
        @SerializedName("monumentDescriptions") val monumentDescriptions: List<MonumentDescription>,
    ) {
        data class MonumentSubtitle(
            @SerializedName("language") val language: String,
            @SerializedName("subtitle") val subtitle: String,
        )

        data class MonumentAudioUrl(
            @SerializedName("language") val language: String,
            @SerializedName("audioUrl") val audioUrl: String,
        )

        data class MonumentDescription(
            @SerializedName("language") val language: String,
            @SerializedName("description") val description: String,
        )
    }
}
