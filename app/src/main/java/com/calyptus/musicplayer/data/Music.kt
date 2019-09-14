package com.calyptus.musicplayer.data

import android.net.Uri

class Music(
    val id: Long,
    val musicTitle: String,
    val path: Uri,
    val artist: String,
    val imagePath: Uri
)