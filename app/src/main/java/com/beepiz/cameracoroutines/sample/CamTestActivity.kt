package com.beepiz.cameracoroutines.sample

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.os.HandlerThread
import android.support.annotation.RequiresPermission
import com.beepiz.cameracoroutines.extensions.HandlerElement
import com.beepiz.cameracoroutines.sample.activity.ScopedAppCompactActivity
import com.beepiz.cameracoroutines.sample.extensions.CamCharacteristics
import com.beepiz.cameracoroutines.sample.extensions.media.recordVideo
import kotlinx.coroutines.experimental.GlobalScope
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import splitties.viewdsl.core.setContentView
import timber.log.Timber

class CamTestActivity : ScopedAppCompactActivity() {
    private val camThread by lazy { HandlerThread("camera") }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val ui = CamTestUi(this)
        setContentView(ui)
        camThread.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        camThread.quitSafely()
    }

    @SuppressLint("MissingPermission")
    @RequiresPermission(Manifest.permission.CAMERA)
    override fun createActivityJob() = GlobalScope.launch(UI.immediate + HandlerElement(camThread)) {
        try {
            val externalFilesDir = getExternalFilesDir(null).absolutePath
            val backVideoPath = "$externalFilesDir/BackRecorded.mp4"
            val frontVideoPath = "$externalFilesDir/FrontRecorded.mp4"

            recordVideo(CamCharacteristics.LensFacing.BACK, backVideoPath) {
                delay(10_000)
            }

            recordVideo(CamCharacteristics.LensFacing.FRONT, frontVideoPath) {
                delay(10_000)
            }
        } catch (e: Exception) {
            Timber.e(e)
        }

        finish()
    }
}
