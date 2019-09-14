package com.calyptus.musicplayer

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.LifecycleOwner

class MainActivity : AppCompatActivity(), LifecycleOwner {

    private val musicPlayerFragment by lazy {
        MusicPlayerFragment.getInstance()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        changeNavigationBarColor()
        changeStatusBarColor()
        setContentView(R.layout.activity_main)

        supportFragmentManager.beginTransaction().
            add(R.id.fragmentContainter, musicPlayerFragment)
            .addToBackStack("music_player_fragment")
            .commit()

    }

    private fun changeStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.navigationBarColor = ResourcesCompat.getColor(resources, R.color.white, theme)
        }
    }

    private fun changeNavigationBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = ResourcesCompat.getColor(resources, R.color.white, theme)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
    }

}
