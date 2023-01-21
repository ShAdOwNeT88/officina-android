package com.officinasocialeproarpaia.officina_android.features.dashboard

import com.google.gson.Gson
import com.officinasocialeproarpaia.officina_android.base.BaseViewModel
import com.officinasocialeproarpaia.officina_android.features.main.domain.MonumentConfig
import io.reactivex.rxjava3.core.Scheduler
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import timber.log.Timber


sealed class DashboardEvent {
    data class RetrieveMonumentsConfig(val jsonFromRaw: InputStream) : DashboardEvent()
}

sealed class DashboardState {
    data class DashboardError(val error: Throwable) : DashboardState()
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

        val streamReader = BufferedReader(InputStreamReader(jsonFromRaw, "UTF-8"))
        val des = Gson().fromJson(streamReader, MonumentConfig::class.java)

        Timber.e("Deserialized $des")
    }
}
