package com.mewhpm.mewphotoprism.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.mewhpm.mewphotoprism.MainActivity
import com.mewhpm.mewphotoprism.R
import com.mewhpm.mewphotoprism.entity.AccountEntity
import com.mewhpm.mewphotoprism.services.helpers.PhotoprismHelper
import com.mewhpm.mewphotoprism.services.helpers.PhotoprismPredefinedFilters
import com.mewhpm.mewphotoprism.utils.runIO
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
        holder.image.setImageResource(android.R.color.transparent)
        val svc = getPhotoprismService()
        activity.runIO({
            svc.createTaskForGenerateImagePreview(context, filter, extra, position, {
                activity.runOnUiThread {
                    try {
                        Glide
                            .with(context.applicationContext)
                            .load(it.img)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .centerCrop()
                            .into(holder.image)
                    } catch (t : Throwable) {
                        holder.image.setImageResource(R.drawable.icon_broken_image)
                        Log.e("GLIDE", "Error ${t.message}")
                    }
                }
            }, {
                activity.runOnUiThread {
                    // TODO: add error message
                    holder.image.setImageResource(R.drawable.icon_broken_image)
                }
            })
        }, {
            // TODO: add error message
            holder.image.setImageResource(R.drawable.icon_broken_image)
        })
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
        return getPhotoprismService().getImagesCount(filter)
    }
}