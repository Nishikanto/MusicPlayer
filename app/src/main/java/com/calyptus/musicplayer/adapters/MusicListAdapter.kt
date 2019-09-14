package com.calyptus.musicplayer.adapters

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.os.CancellationSignal
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView

import androidx.recyclerview.widget.RecyclerView

import com.bumptech.glide.Glide
import com.calyptus.musicplayer.R
import com.calyptus.musicplayer.data.Music

/**
 * Created by yarolegovich on 07.03.2017.
 */

class MusicListAdapter(private val data: List<Music>) : RecyclerView.Adapter<MusicListAdapter.ViewHolder>() {

    private var context : Context? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val v = inflater.inflate(R.layout.item_music_card, parent, false)
        this.context = parent.context
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.mmr.setDataSource(context, data[position].path)
        holder.rawArt = holder.mmr.embeddedPicture

        if (null != holder.rawArt){
            holder.art = BitmapFactory.decodeByteArray(holder.rawArt, 0, holder.rawArt!!.size, holder.bfo)
            Glide.with(holder.itemView.context)
                .load(holder.art)
                .into(holder.image)
        } else{
            holder.image.setImageDrawable(context!!.getDrawable(R.drawable.ic_headphone_symbol))
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.image) as ImageView
        val mmr = MediaMetadataRetriever()
        var rawArt: ByteArray? = null
        lateinit var art: Bitmap
        val bfo = BitmapFactory.Options()

    }
}