@Suppress("MemberVisibilityCanBePrivate")
object Versions {

    //app
    const val versionCode = 2 //used for local builds, CI uses codes used for published versions
    const val versionName = "1.0.0"

    // platform
    const val buildTools = "29.0.3"
    const val kotlin = "1.5.20"
    const val minSdk = 21
    const val targetSdk = 33

    // 3rd party
    const val androidx_navigation_version = "2.5.3"
    const val arrow_version = "0.11.0"
    const val camerax_version = "1.0.0-rc03"
    const val detekt = "1.14.2"
    const val firebase_core = "18.0.0"
    const val koin = "2.2.1"
    const val lifecycle_version = "2.2.0"
    const val okhttp = "4.9.0"
    const val retrofit = "2.9.0"
    const val rxbinding = "4.0.0"
    const val mockk = "1.10.3"
    const val kotlinx_coroutines = "1.4.2"
}

@Suppress("unused")
object BuildLibs {
    const val detekt = "io.gitlab.arturbosch.detekt:detekt-gradle-plugin:${Versions.detekt}"
    const val firebase_crashlytics = "com.google.firebase:firebase-crashlytics-gradle:2.2.0"
    const val firebase_performance_sdk = "com.google.firebase:perf-plugin:1.3.1"
    const val google_services = "com.google.gms:google-services:4.3.3"
    const val gradle_android = "com.android.tools.build:gradle:4.1.1"
    const val gradle_versions = "com.github.ben-manes:gradle-versions-plugin:0.27.0"
    const val jacoco = "org.jacoco:org.jacoco.core:0.8.5"
    const val kotlin_plugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}"
    const val sentry_gradle_plugin = "io.sentry:sentry-android-gradle-plugin:1.7.36"
    const val sonarqube = "org.sonarsource.scanner.gradle:sonarqube-gradle-plugin:2.8"
    const val test_logger = "com.adarshr:gradle-test-logger-plugin:2.0.0"
}

@Suppress("unused")
object Libs {
    const val androidx_appcompat = "androidx.appcompat:appcompat:1.2.0"
    const val androidx_cardview = "androidx.cardview:cardview:1.0.0"
    const val androidx_constraint = "androidx.constraintlayout:constraintlayout:2.0.4"
    const val androidx_exifinterface = "androidx.exifinterface:exifinterface:1.3.2"
    const val androidx_recyclerview = "androidx.recyclerview:recyclerview:1.1.0"
    const val androidx_navigation_fragment = "androidx.navigation:navigation-fragment:${Versions.androidx_navigation_version}"
    const val androidx_navigation_fragment_ktx = "androidx.navigation:navigation-fragment-ktx:${Versions.androidx_navigation_version}"
    const val androidx_navigation_ui = "androidx.navigation:navigation-ui:${Versions.androidx_navigation_version}"
    const val androidx_navigation_ui_ktx = "androidx.navigation:navigation-ui-ktx:${Versions.androidx_navigation_version}"
    const val arrow_core = "io.arrow-kt:arrow-core:${Versions.arrow_version}"
    const val arrow_meta = "io.arrow-kt:arrow-meta:${Versions.arrow_version}"
    const val arrow_syntax = "io.arrow-kt:arrow-syntax:${Versions.arrow_version}"
    const val camerax = "androidx.camera:camera-camera2:${Versions.camerax_version}"
    const val camerax_lifecycle = "androidx.camera:camera-lifecycle:${Versions.camerax_version}"
    const val camerax_view = "androidx.camera:camera-view:1.0.0-alpha22"
    const val firebase_analytics = "com.google.firebase:firebase-analytics:18.0.0"
    const val firebase_auth = "com.google.firebase:firebase-auth:20.0.1"
    const val firebase_auth_ktx = "com.google.firebase:firebase-auth-ktx:20.0.1"
    const val firebase_config = "com.google.firebase:firebase-config:${Versions.firebase_core}"
    const val firebase_core = "com.google.firebase:firebase-core:${Versions.firebase_core}"
    const val firebase_crashlytics = "com.google.firebase:firebase-crashlytics:17.3.0"
    const val firebase_notifications = "com.google.firebase:firebase-messaging:21.0.0"
    const val firebase_performance_monitoring = "com.google.firebase:firebase-perf:19.1.0"
    const val firebase_performance_monitoring_ktx = "com.google.firebase:firebase-perf-ktx:19.1.0"
    const val firebase_remote_config = "com.google.firebase:firebase-config-ktx:20.0.1"
    const val firebase_storage = "com.google.firebase:firebase-storage-ktx:19.2.0"
    const val glide = "com.github.bumptech.glide:glide:4.11.0"
    const val google_material = "com.google.android.material:material:1.2.1"
    const val html_spanner = "com.github.NightWhistler:HtmlSpanner:0.4"
    const val play_services_maps = "com.google.android.gms:play-services-maps:18.1.0"
    const val play_services_location = "com.google.android.gms:play-services-location:17.0.0"
    const val play_services_maps_utils = "com.google.maps.android:maps-utils-ktx:3.1.0"
    const val gson = "com.google.code.gson:gson:2.8.6"
    const val koin = "org.koin:koin-android:${Versions.koin}"
    const val koinViewModel = "org.koin:koin-android-viewmodel:${Versions.koin}"
    const val kotlin_reflect = "org.jetbrains.kotlin:kotlin-reflect:${Versions.kotlin}"
    const val kotlin_stdlib = "org.jetbrains.kotlin:kotlin-stdlib-jdk7:${Versions.kotlin}"
    const val lifecycle_extensions = "androidx.lifecycle:lifecycle-extensions:${Versions.lifecycle_version}"
    const val lifecycle_viewmodel = "androidx.lifecycle:lifecycle-viewmodel:${Versions.lifecycle_version}"
    const val lifecycle_viewmodel_ktx = "androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.lifecycle_version}"
    const val lottie = "com.airbnb.android:lottie:3.5.0"
    const val okhttp = "com.squareup.okhttp3:okhttp:${Versions.okhttp}"
    const val okhttp_logging = "com.squareup.okhttp3:logging-interceptor:${Versions.okhttp}"
    const val pageViewIndicator = "com.romandanylyk:pageindicatorview:1.0.3"
    const val pdfViewer = "com.github.barteksc:android-pdf-viewer:3.2.0-beta.1"
    const val retrofit = "com.squareup.retrofit2:retrofit:${Versions.retrofit}"
    const val retrofit_gson = "com.squareup.retrofit2:converter-gson:${Versions.retrofit}"
    const val retrofit_rx = "com.github.akarnokd:rxjava3-retrofit-adapter:3.0.0"
    const val rxandroid = "io.reactivex.rxjava3:rxandroid:3.0.0"
    const val rxbinding4 = "com.jakewharton.rxbinding4:rxbinding:${Versions.rxbinding}"
    const val rxbinding4_core = "com.jakewharton.rxbinding4:rxbinding-core:${Versions.rxbinding}"
    const val rxjava = "io.reactivex.rxjava3:rxjava:3.0.8"
    const val rxkotlin = "io.reactivex.rxjava3:rxkotlin:3.0.1"
    const val sentry = "io.sentry:sentry-android:3.2.0"
    const val three_ten_abp = "com.jakewharton.threetenabp:threetenabp:1.3.0"
    const val timber = "com.jakewharton.timber:timber:4.7.1"
    const val ucrop = "com.github.yalantis:ucrop:2.2.6"
    const val country_code_picker_library = "com.hbb20:ccp:2.4.2"
    const val prdownloader = "com.mindorks.android:prdownloader:0.6.0"
    const val icu4j = "com.ibm.icu:icu4j:68.2"
    const val altBeacon = "org.altbeacon:android-beacon-library:2.19.4"
}

@Suppress("unused")
object DebugLibs {
    const val leak_canary = "com.squareup.leakcanary:leakcanary-android:2.5"
    const val rxlint = "nl.littlerobots.rxlint:rxlint:1.7.6"
}

@Suppress("unused")
object TestLibs {
    const val assertk = "com.willowtreeapps.assertk:assertk:0.23"
    const val core_testing = "android.arch.core:core-testing:1.1.1"
    const val junit_test_core = "de.mannodermaus.junit5:android-test-core:1.2.0"
    const val junit_test_runner = "de.mannodermaus.junit5:android-test-runner:1.2.0"
    const val androidx_test_runner = "androidx.test:runner:1.2.0"
    const val junit = "junit:junit:4.13.1"
    const val koin_test = "org.koin:koin-test:${Versions.koin}"
    const val kotlin_test = "org.jetbrains.kotlin:kotlin-test-junit:${Versions.kotlin}"
    const val okhttp_mockwebserver = "com.squareup.okhttp3:mockwebserver:${Versions.okhttp}"
    const val mockk = "io.mockk:mockk:${Versions.mockk}"
    const val mockito_kotlin = "com.nhaarman:mockito-kotlin:1.6.0"
    const val threetenbp = "org.threeten:threetenbp:1.5.0"
    const val kotlinx_coroutines = "org.jetbrains.kotlinx:kotlinx-coroutines-test:${Versions.kotlinx_coroutines}"
    const val androidx_core = "androidx.arch.core:core-testing:2.1.0"
}

object GreaterJDKLibs {
    const val jdk9_deps = "com.github.pengrad:jdk9-deps:1.0"
    const val jaxb_api = "javax.xml.bind:jaxb-api:2.3.1"
    const val jaxb_core = "com.sun.xml.bind:jaxb-core:2.3.0.1"
    const val jaxb_impl = "com.sun.xml.bind:jaxb-impl:2.3.2"
}
