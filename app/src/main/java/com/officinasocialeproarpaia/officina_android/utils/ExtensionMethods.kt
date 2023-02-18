@file:SuppressWarnings("TooManyFunctions")

package com.officinasocialeproarpaia.officina_android.utils

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.drawable.Drawable
import android.media.Image
import android.net.Uri
import android.provider.MediaStore
import android.text.Spanned
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.widget.ToggleButton
import androidx.appcompat.widget.PopupMenu
import androidx.core.app.NavUtils
import androidx.core.app.TaskStackBuilder
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.exifinterface.media.ExifInterface
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.DrawableImageViewTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.officinasocialeproarpaia.officina_android.R
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.lang.reflect.Field
import java.lang.reflect.Method
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.ZoneOffset
import timber.log.Timber

private const val IMG_QUALITY = 100
private const val LOCATION_SETTING_REQUEST = 999

val <T> T.exhaustive: T
    get() = this

fun View.visible(visible: Boolean) {
    visibility = if (visible) {
        View.VISIBLE
    } else {
        View.GONE
    }
}

fun View.actionNotImplementedListener(context: Context, actionName: String) {
    this.setOnClickListener {
        Timber.w("%s action requested - not implemented!", actionName)
        Toast.makeText(context, context.getString(R.string.action_not_implemented), Toast.LENGTH_SHORT).show()
    }
}

private fun buildActivityStack(activity: Activity) {
    val upIntent: Intent? = NavUtils.getParentActivityIntent(activity)
    if (upIntent == null) {
        Timber.e(IllegalStateException("UpIntent is null"), "cannot buildActivityStack()")
    } else {
        TaskStackBuilder.create(activity)
            .addNextIntentWithParentStack(upIntent)
            .startActivities()
    }
}

fun Activity.goToParentActivity() {
    val upIntent = NavUtils.getParentActivityIntent(this)

    if (upIntent != null) {
        if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
            buildActivityStack(this)
        } else {
            navigateUpTo(upIntent)
        }
    } else {
        finish()
    }
}

fun Context.showKeyboard() {
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)
}

fun Context.hideKeyboard() {
    val imm = this.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    //Find the currently focused view, so we can grab the correct window token from it.
    var view = (this as Activity).currentFocus
    //If no view currently has focus, create a new one, just so we can grab a window token from it
    if (view == null) {
        view = View(this)
    }
    imm.hideSoftInputFromWindow(view.windowToken, 0)
}

fun ToggleButton.unchecked() {
    this.setTextColor(ContextCompat.getColor(this.context, R.color.black))
    this.isChecked = false
}

fun ToggleButton.checked() {
    this.setTextColor(ContextCompat.getColor(this.context, R.color.white))
    this.isChecked = true
}

@Throws(IOException::class)
fun ContentResolver.getBitmapFromUri(uri: Uri, maxWidth: Int, maxHeight: Int): Bitmap {
    val angle = getRotationFromExif(uri, this)
    val parcelFileDescriptor = this.openFileDescriptor(uri, "r")
    checkNotNull(parcelFileDescriptor) { "Unable to get file descriptor for uri $uri" }

    parcelFileDescriptor.let {
        val fileDescriptor = parcelFileDescriptor.fileDescriptor
        val bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor, null, BitmapFactory.Options())
        parcelFileDescriptor.close()

        val width = bitmap.width.toFloat()
        val height = bitmap.height.toFloat()

        val ratio = Math.min(maxWidth / width, maxHeight / height)
        val newWidth = Math.min(ratio * width, width)
        val newHeight = Math.min(ratio * height, height)

        Timber.v("Original image width: " + width + ", height: " + height)
        Timber.v("Resized image width: " + newWidth + ", height: " + newHeight)

        val matrix = Matrix()
        matrix.postScale(ratio, ratio)
        matrix.postRotate(angle.toFloat())
        return Bitmap.createBitmap(bitmap, 0, 0, width.toInt(), height.toInt(), matrix, false)
    }
}

fun ImageView.loadImageOrRemove(imageUrl: String?) {
    imageUrl?.let {
        val httpsImageUrl = imageUrl.replace("http://", "https://")

        Glide.with(this.context)
            .load(httpsImageUrl)
            .fitCenter()
            .into(CustomImageViewTarget(this))
    }
}

class CustomImageViewTarget(view: ImageView?) : DrawableImageViewTarget(view) {
    override fun onLoadFailed(errorDrawable: Drawable?) {
        super.onLoadFailed(errorDrawable)
        view.visibility = View.GONE
    }

    override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
        super.onResourceReady(resource, transition)
        view.visibility = View.VISIBLE
    }
}

fun Context.getImageUri(image: Bitmap): Uri? {
    val bytes = ByteArrayOutputStream()
    image.compress(Bitmap.CompressFormat.JPEG, IMG_QUALITY, bytes)
    val path = MediaStore.Images.Media.insertImage(this.contentResolver, image, "Title", null)
    return Uri.parse(path)
}

fun ImageView.loadRoundImage(imageRes: Int) {
    Glide.with(this.context)
        .load(imageRes)
        .apply(RequestOptions.circleCropTransform())
        .into(this)
}

fun Image.toBitmap(): Bitmap {
    val buffer = planes[0].buffer
    buffer.rewind()
    val bytes = ByteArray(buffer.capacity())
    buffer.get(bytes)
    return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
}

fun Bitmap.rotate(degrees: Float): Bitmap {
    val matrix = Matrix().apply { postRotate(degrees) }
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}

fun ImageView.setAutoMirrored() {
    val drawable = this.drawable
    drawable.isAutoMirrored = true
    this.setImageDrawable(drawable)
}

@Suppress("ComplexMethod", "MagicNumber")
private fun getRotationFromExif(uri: Uri, contentResolver: ContentResolver): Int {
    var inputStream: InputStream? = null
    try {
        inputStream = contentResolver.openInputStream(uri)
        return if (inputStream == null) {
            0
        } else {
            val exif = ExifInterface(inputStream)
            when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1)) {
                1 -> 0
                3 -> 180
                6 -> 90
                8 -> 270
                else -> 0
            }
        }
    } catch (e: IOException) {
        return 0
    } finally {
        if (inputStream != null) {
            try {
                inputStream.close()
            } catch (ignored: IOException) {
            }
        }
    }
}

fun extractDate(year: Int, monthOfYear: Int, dayOfMonth: Int): OffsetDateTime {
    return OffsetDateTime.of(
        year,
        monthOfYear,
        dayOfMonth,
        0, 0, 0, 0,
        ZoneOffset.UTC
    )
}

fun TextView.textOrGone(text: String?) {
    if (text.isNullOrEmpty()) {
        this.visible(false)
    } else {
        this.text = text
    }
}

fun createSpannableString(stringWoValue: String, valueToUse: String): Spanned {
    val output = String.format(stringWoValue, valueToUse)
    return HtmlCompat.fromHtml(output, HtmlCompat.FROM_HTML_MODE_LEGACY)
}

fun setForceShowMenuIcon(popupMenu: PopupMenu) {
    try {
        val fields: Array<Field> = popupMenu.javaClass.declaredFields
        for (field in fields) {
            if ("mPopup" == field.name) {
                field.isAccessible = true
                val menuPopupHelper: Any = field.get(popupMenu)
                val classPopupHelper = Class.forName(
                    menuPopupHelper
                        .javaClass.name
                )
                val setForceIcons: Method = classPopupHelper.getMethod(
                    "setForceShowIcon", Boolean::class.javaPrimitiveType
                )
                setForceIcons.invoke(menuPopupHelper, true)
                break
            }
        }
    } catch (e: Throwable) {
        Timber.e("error force show menu icons $e")
    }
}

fun Activity.showEnableLocationSettingDialog() {
    this.let {
        val locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)

        val task = LocationServices.getSettingsClient(it)
            .checkLocationSettings(builder.build())

        task.addOnSuccessListener { response ->
            val states = response.locationSettingsStates
            if (states.isLocationPresent) {
                //Do something
            }
        }
        task.addOnFailureListener { e ->
            if (e is ResolvableApiException) {
                try {
                    // Handle result in onActivityResult()
                    e.startResolutionForResult(
                        it,
                        LOCATION_SETTING_REQUEST
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    //do nothing
                }
            }
        }
    }
}
