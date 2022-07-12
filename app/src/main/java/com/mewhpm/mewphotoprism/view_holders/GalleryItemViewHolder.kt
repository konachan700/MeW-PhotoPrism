package com.mewhpm.mewphotoprism.view_holders

import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.mewhpm.mewphotoprism.R

class GalleryItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val image : ImageView = itemView.findViewById<View>(R.id.galleryViewItemIV) as ImageView
    val selected : ImageView = itemView.findViewById<View>(R.id.imageViewSelected) as ImageView
}