package com.officinasocialeproarpaia.officina_android.features

import android.app.Activity
import android.content.Intent
import android.net.Uri

interface Navigator {
    fun openWebBrowser(activity: Activity, url: String)
    fun startActivityWithIntent(activity: Activity, intent: Intent)
}

class AppNavigator() : Navigator {
    override fun openWebBrowser(activity: Activity, url: String) {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivityWithIntent(activity, browserIntent)
    }

    override fun startActivityWithIntent(activity: Activity, intent: Intent) {
        activity.startActivity(intent)
    }
}
