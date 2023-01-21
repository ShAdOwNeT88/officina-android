package com.officinasocialeproarpaia.officina_android.features.dashboard

import com.google.gson.Gson
import com.officinasocialeproarpaia.officina_android.base.BaseViewModel
import com.officinasocialeproarpaia.officina_android.features.main.domain.MonumentConfig
import io.reactivex.rxjava3.core.Scheduler
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader


sealed class DashboardEvent {
    data class RetrieveMonumentsConfig(val jsonFromRaw: InputStream) : DashboardEvent()
}

sealed class DashboardState {
    data class RetrievedMonumentsConfig(val monumentsConfig: MonumentConfig) : DashboardState()
    data class Error(val error: Throwable) : DashboardState()
    object InProgress : DashboardState()
}

class DashboardViewModel(private val scheduler: Scheduler) : BaseViewModel<DashboardState, DashboardEvent>() {

    override fun send(event: DashboardEvent) {
        when (event) {
            is DashboardEvent.RetrieveMonumentsConfig -> getMonumentsConfig(event.jsonFromRaw)
        }
    }

    private fun getMonumentsConfig(jsonFromRaw: InputStream) {
        post(DashboardState.InProgress)
        var monConfig: MonumentConfig? = null

        val streamReader = BufferedReader(InputStreamReader(jsonFromRaw, "UTF-8"))
        if (streamReader.ready()) {
            monConfig = Gson().fromJson(streamReader, MonumentConfig::class.java)
        }

        if (monConfig != null) {
            streamReader.close()
            post(DashboardState.RetrievedMonumentsConfig(monConfig))
        } else {
            streamReader.close()
            post(DashboardState.Error(Throwable("No monuments config detected or error during deserialization")))
        }
    }
}
