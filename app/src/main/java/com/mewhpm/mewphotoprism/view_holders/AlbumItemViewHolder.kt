package com.mewhpm.mewphotoprism.view_holders

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mewhpm.mewphotoprism.R

class AlbumItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val image : ImageView = itemView.findViewById<View>(R.id.galleryAlbumViewItemIV) as ImageView
    val selected : ImageView = itemView.findViewById<View>(R.id.albumViewSelected) as ImageView
    val name : TextView = itemView.findViewById<View>(R.id.txtAlbumName) as TextView
}