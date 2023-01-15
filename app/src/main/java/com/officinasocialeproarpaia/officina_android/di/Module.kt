package com.officinasocialeproarpaia.officina_android.di

import android.content.Context
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.officinasocialeproarpaia.officina_android.features.AppNavigator
import com.officinasocialeproarpaia.officina_android.features.Navigator
import com.officinasocialeproarpaia.officina_android.features.main.MainViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import org.koin.android.ext.koin.androidContext
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

typealias DebugFlag = Boolean

sealed class GoogleApi {
    object Available : GoogleApi()
    object NotAvailable : GoogleApi()
}

val viewModels = module {
    viewModel { MainViewModel(scheduler = AndroidSchedulers.mainThread()) }
}

val androidComponents = module {
    single { androidContext().resources }
    single { androidContext().packageManager }
    single { androidContext().contentResolver }
    single { getGoogleApiAvailability(get()) }
}

private fun getGoogleApiAvailability(context: Context): GoogleApi =
    if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS) {
        GoogleApi.Available
    } else GoogleApi.NotAvailable

val appComponents = module {
    single<Navigator> { AppNavigator() }
}
