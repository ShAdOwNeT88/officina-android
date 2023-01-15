package com.officinasocialeproarpaia.officina_android

import android.app.Application
import com.jakewharton.threetenabp.AndroidThreeTen
import com.officinasocialeproarpaia.officina_android.di.DebugFlag
import com.officinasocialeproarpaia.officina_android.di.androidComponents
import com.officinasocialeproarpaia.officina_android.di.appComponents
import com.officinasocialeproarpaia.officina_android.di.viewModels
import io.reactivex.rxjava3.plugins.RxJavaPlugins
import java.util.Locale
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.dsl.module
import timber.log.Timber

const val TAG_LOGGING = "OfficinaArpaia"

class OfficinaApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        setupLogging()
        setupDI()

        AndroidThreeTen.init(this)

        RxJavaPlugins.setErrorHandler { Timber.e(it) }
    }

    @Suppress("LongMethod")
    private fun setupDI() {
        startKoin {
            androidLogger(Level.NONE)
            androidContext(this@OfficinaApplication)

            @Suppress("USELESS_CAST")
            val appSetupModule = module {
                single { BuildConfig.DEBUG as DebugFlag }
            }

            modules(listOf(appSetupModule, androidComponents, appComponents, viewModels))
        }
    }

    @Suppress("ConstantConditionIf")
    private fun setupLogging() {
        val timberTag = TAG_LOGGING + ": " + BuildConfig.BUILD_TYPE
        Timber.plant(ExplicitDebugTree(BuildConfig.BUILD_TYPE))
        Timber.tag(timberTag)
    }

    private class ExplicitDebugTree(val buildConfig: String) : Timber.DebugTree() {
        override fun createStackElementTag(element: StackTraceElement): String {
            return String.format(
                Locale.US,
                "(%s:%s) [%s()]",
                buildConfig,
                element.fileName,
                element.lineNumber,
                element.methodName
            )
        }
    }
}
