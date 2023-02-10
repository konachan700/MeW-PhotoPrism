package com.mewhpm.mewphotoprism.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mewhpm.mewphotoprism.MainActivity
import com.mewhpm.mewphotoprism.R
import com.mewhpm.mewphotoprism.entity.AccountEntity
import com.mewhpm.mewphotoprism.services.helpers.PhotoprismHelper
import com.mewhpm.mewphotoprism.services.helpers.PhotoprismPredefinedFilters
import com.mewhpm.mewphotoprism.view_holders.GalleryItemViewHolder
import java.util.concurrent.ConcurrentHashMap

class GalleryListAdapter(
    private val activity        : MainActivity,
    private val filter          : PhotoprismPredefinedFilters,
    private val extra           : ConcurrentHashMap<String, Any>,
    private val accountEntity   : AccountEntity,
    private val context         : Context,
    private val onClick         : (index : Int) -> Unit
) : RecyclerView.Adapter<GalleryItemViewHolder>() {
    private val selectedItems = HashSet<Int>()

    private fun getPhotoprismService() : PhotoprismHelper {
        return activity.fgService!!.photoprismHelper!!
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
        holder.setImageAsync(activity, context, filter!!, extra, position)
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
        val count = getPhotoprismService().getImagesCount(filter)
        //Log.d("COUNT", "Count = $count; filter = ${filter.name}")
        return count
    }
}