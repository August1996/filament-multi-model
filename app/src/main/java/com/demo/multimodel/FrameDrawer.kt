package com.demo.multimodel

import android.view.Choreographer
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent


class FrameDrawer(
    private val lifecycleOwner: LifecycleOwner,
    private val frameCallback: (Long) -> Unit
) :

    Choreographer.FrameCallback, LifecycleObserver {


    private val choreographer = Choreographer.getInstance()

    fun init() {
        lifecycleOwner.lifecycle.addObserver(this)
        choreographer.postFrameCallback(this)
    }

    override fun doFrame(frameTimeNanos: Long) {
        choreographer.postFrameCallback(this)
        frameCallback(frameTimeNanos)
    }


    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onStart() {
        choreographer.postFrameCallback(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onStop() {
        choreographer.removeFrameCallback(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        lifecycleOwner.lifecycle.removeObserver(this)
    }

}