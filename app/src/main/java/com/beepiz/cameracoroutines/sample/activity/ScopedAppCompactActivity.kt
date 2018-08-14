package com.beepiz.cameracoroutines.sample.activity

import android.support.v7.app.AppCompatActivity
import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.Job
import kotlin.coroutines.experimental.CoroutineContext

abstract class ScopedAppCompactActivity : AppCompatActivity(), CoroutineScope {

    protected lateinit var activityJob: Job

    override val coroutineContext: CoroutineContext
        get() = activityJob


    override fun onStart() {
        super.onStart()
        activityJob = createActivityJob()
    }

    override fun onStop() {
        super.onStop()
        // Can also join and handle exceptions if necessary
        activityJob.cancel()
    }

    protected abstract fun createActivityJob(): Job

}
