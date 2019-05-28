package com.cristiandev.mymusicplayer.data.adapter

import android.view.GestureDetector
import android.view.MotionEvent
import com.cristiandev.mymusicplayer.main.MainActivity.Companion.DOUBLE_TAP
import com.cristiandev.mymusicplayer.main.MainActivity.Companion.DOWN
import com.cristiandev.mymusicplayer.main.MainActivity.Companion.LEFT
import com.cristiandev.mymusicplayer.main.MainActivity.Companion.RIGHT
import com.cristiandev.mymusicplayer.main.MainActivity.Companion.TAP
import com.cristiandev.mymusicplayer.main.MainActivity.Companion.UP
import com.cristiandev.mymusicplayer.main.MainActivity.Companion.detectGesture


class SwipeGestureListener : GestureDetector.SimpleOnGestureListener() {

    override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {

        // Get swipe delta value in x axis.
        val deltaX = e1.x - e2.x

        // Get swipe delta value in y axis.
        val deltaY = e1.y - e2.y

        // Get absolute value.
        val deltaXAbs = Math.abs(deltaX)
        val deltaYAbs = Math.abs(deltaY)

        // Only when swipe distance between minimal and maximal distance value then we treat it as effective swipe
        if (deltaXAbs in 100.0..1000.0)
            if (deltaX > 0)
                detectGesture.onNext(LEFT)
            else
                detectGesture.onNext(RIGHT)

        if (deltaYAbs in 100.0..1000.0)
            if (deltaY > 0)
                detectGesture.onNext(UP)
            else
                detectGesture.onNext(DOWN)

        return true
    }
    override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
        detectGesture.onNext(TAP)
        return true
    }
    override fun onDoubleTap(e: MotionEvent): Boolean {
        detectGesture.onNext(DOUBLE_TAP)
        return true
    }
}