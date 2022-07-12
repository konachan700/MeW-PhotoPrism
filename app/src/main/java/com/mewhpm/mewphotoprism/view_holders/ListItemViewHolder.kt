package com.mewhpm.mewphotoprism.view_holders

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mewhpm.mewphotoprism.R

class ListItemViewHolder(
    val itemView: View
    ) : RecyclerView.ViewHolder(itemView) {
    val txtHeader : TextView  = itemView.findViewById<View>(R.id.lblHeader) as TextView
    val txtText   : TextView  = itemView.findViewById<View>(R.id.lblText) as TextView
    val icon      : ImageView = itemView.findViewById<View>(R.id.iconMain) as ImageView
    val rootView  : View      = itemView.findViewById<View>(R.id.listItemRoot) as View
}