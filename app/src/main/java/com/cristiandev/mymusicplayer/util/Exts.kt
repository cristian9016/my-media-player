package com.cristiandev.mymusicplayer.util

import android.support.annotation.LayoutRes
import android.view.LayoutInflater
import android.view.ViewGroup

fun ViewGroup.inflate(@LayoutRes resId: Int) = LayoutInflater.from(context).inflate(resId, this, false)