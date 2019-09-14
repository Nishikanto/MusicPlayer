package com.calyptus.musicplayer.viewmodels

import android.content.ContentResolver
import android.provider.MediaStore

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.calyptus.musicplayer.data.Music

import java.util.ArrayList
import android.content.ContentUris.withAppendedId


class MusicPlayerFragmentViewModel : ViewModel() {

    private var musicList: MutableLiveData<List<Music>>? = null

    fun fetchAllMusic(contentResolver: ContentResolver): MutableLiveData<List<Music>> {

        if (musicList == null) {
            musicList = MutableLiveData<List<Music>>()
        }

        val musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val musicCursor = contentResolver.query(musicUri, null, null, null, null)
        val musicArray = ArrayList<Music>()

        if (musicCursor != null && musicCursor.moveToFirst()) {

            //get columns
            val titleColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
            val idColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media._ID)
            val artistColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
            val mediaId = musicCursor.getColumnIndex(MediaStore.Audio.Media._ID)
            val imageId = musicCursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ID)
            //add songs to list
            do {
                val thisId = musicCursor.getLong(idColumn)
                val thisTitle = musicCursor.getString(titleColumn)
                val thisArtist = musicCursor.getString(artistColumn)
                val musicUri = withAppendedId(musicUri, musicCursor.getLong(mediaId))
                val imageUri = withAppendedId(musicUri, musicCursor.getLong(imageId))
                val music = Music(thisId, thisTitle, musicUri, thisArtist, imageUri)
                musicArray.add(music)
            } while (musicCursor.moveToNext())
        }

        musicList?.postValue(musicArray)
        return musicList as MutableLiveData<List<Music>>
    }
}
