package com.calyptus.musicplayer.utilities

import android.content.Context
import android.graphics.drawable.GradientDrawable

import androidx.core.content.ContextCompat
import com.calyptus.musicplayer.R

object GradientCreator {
    fun createGradient(context: Context): GradientDrawable {
        return GradientDrawable(
            GradientDrawable.Orientation.BOTTOM_TOP,
            intArrayOf(
                ContextCompat.getColor(context, R.color.red_300),
                ContextCompat.getColor(context, R.color.purple_700)
            )
        )
    }
}
