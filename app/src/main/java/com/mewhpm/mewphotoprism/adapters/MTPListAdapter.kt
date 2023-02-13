package com.mewhpm.mewphotoprism.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mewhpm.mewphotoprism.MainActivity
import com.mewhpm.mewphotoprism.R
import com.mewhpm.mewphotoprism.services.helpers.MTPHelper
import com.mewhpm.mewphotoprism.view_holders.GalleryItemViewHolder

class MTPListAdapter(
    private val activity        : MainActivity,
    private val context         : Context,
    private val onClick         : (index : Int) -> Unit
) : RecyclerView.Adapter<GalleryItemViewHolder>() {
    private val selectedItems = HashSet<Int>()

    private fun getMTPHelper() : MTPHelper {
        return activity.fgService!!.mtpHelper!!
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GalleryItemViewHolder {
        val view: View = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.layout_gallery_item, parent, false)
        return GalleryItemViewHolder(view)
    }

    private fun setSelect(holder: GalleryItemViewHolder, position: Int) {
        holder.selected.visibility = if (selectedItems.contains(position)) {
            selectedItems.remove(position)
            View.INVISIBLE
        } else {
            selectedItems.add(position)
            View.VISIBLE
        }
    }

    override fun onBindViewHolder(holder: GalleryItemViewHolder, position: Int) {
//        Log.d("COUNT", "onBindViewHolder position = $position")
        holder.setMTPImageAsync(activity, context, position)
        holder.image.setOnClickListener {
            if (selectedItems.isNotEmpty()) {
                setSelect(holder, position)
            } else {
                onClick.invoke(position)
            }
        }
        holder.image.setOnLongClickListener {
            setSelect(holder, position)
            return@setOnLongClickListener true
        }
    }

    override fun getItemCount(): Int {
        val count = getMTPHelper().getCount()
        Log.d("MTP-COUNT", "Count = $count;")
        return count
    }
}