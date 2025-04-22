package com.rekoj134.argamedemo

import android.content.Context
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.WindowManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object TransitionManager {
    private var transitionView: TransitionView? = null
    private var windowManager: WindowManager? = null

    fun startTransition(context: Context) {
        windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        transitionView = TransitionView(context).apply {
            onEnd = {
                CoroutineScope(Dispatchers.Main).launch {
                    delay(1200)
                    finishTransition()
                }
            }
            post {
                startAnimRipple(false)
            }
        }

        val params = WindowManager.LayoutParams().apply {
            width = WindowManager.LayoutParams.MATCH_PARENT
            height = WindowManager.LayoutParams.MATCH_PARENT
            type = WindowManager.LayoutParams.TYPE_APPLICATION
            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
            format = PixelFormat.TRANSLUCENT
            gravity = Gravity.CENTER
        }

        try {
            windowManager?.addView(transitionView, params)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun finishTransition() {
        transitionView?.apply {
            onEnd = {
                try {
                    windowManager?.removeView(this)
                    transitionView = null
                    windowManager = null
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            post {
                startAnimRipple(true)
            }
        }
    }
}