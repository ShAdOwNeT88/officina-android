package com.officinasocialeproarpaia.officina_android.features.main

import com.google.gson.Gson
import com.officinasocialeproarpaia.officina_android.base.BaseViewModel
import com.officinasocialeproarpaia.officina_android.features.main.domain.MonumentConfig
import io.reactivex.rxjava3.core.Scheduler
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import timber.log.Timber


sealed class MainEvent {
    data class RetrieveMonumentsConfig(val jsonFromRaw: InputStream) : MainEvent()
}

sealed class MainState {
    data class MainError(val error: Throwable) : MainState()
    object InProgress : MainState()
}

class MainViewModel(private val scheduler: Scheduler) : BaseViewModel<MainState, MainEvent>() {

    override fun send(event: MainEvent) {
        when (event) {
            is MainEvent.RetrieveMonumentsConfig -> getMonumentsConfig(event.jsonFromRaw)
        }
    }

    private fun getMonumentsConfig(jsonFromRaw: InputStream) {
        post(MainState.InProgress)

        val streamReader = BufferedReader(InputStreamReader(jsonFromRaw, "UTF-8"))
        val des = Gson().fromJson(streamReader, MonumentConfig::class.java)

        Timber.e("Deserialized $des")
    }
}
