package com.cs007s.live.notes.utilities.helpers

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import java.io.File
import java.util.*

object Utils {

    fun isViewOnResumeState(lifecycleOwner: LifecycleOwner): Boolean {
        return lifecycleOwner.lifecycle.currentState == Lifecycle.State.RESUMED
    }

    fun generateRandomColor(): Int {
        val mRandom = Random(System.currentTimeMillis())
        // This is the base color which will be mixed with the generated one
        val baseColor = Color.WHITE
        val baseRed = Color.red(baseColor)
        val baseGreen = Color.green(baseColor)
        val baseBlue = Color.blue(baseColor)
        var red = (baseRed + mRandom.nextInt(256)) / 2
        var green = (baseGreen + mRandom.nextInt(256)) / 2
        var blue = (baseBlue + mRandom.nextInt(256)) / 2

        return Color.rgb(red, green, blue)
    }

    fun shareNotes(activity: Activity, shareDescription: String?) {
        val intent = Intent()
        intent.action = Intent.ACTION_SEND
        intent.putExtra(Intent.EXTRA_TEXT, shareDescription)
        intent.type = "text/plain"
        try {
            activity.startActivity(Intent.createChooser(intent, ""))
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
        }
    }
}