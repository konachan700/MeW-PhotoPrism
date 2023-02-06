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
import com.mewhpm.mewphotoprism.services.helpers.PhotoprismAlbumType
import com.mewhpm.mewphotoprism.services.helpers.PhotoprismHelper
import com.mewhpm.mewphotoprism.utils.runIO
import com.mewhpm.mewphotoprism.view_holders.AlbumItemViewHolder
import kotlinx.coroutines.runBlocking
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

class DirectoryListAdapter(
    private val activity        : MainActivity,
    private val context         : Context,
    private var galleriesFilter : PhotoprismAlbumType,
    private val onClick         : (index : Int) -> Unit
) : RecyclerView.Adapter<AlbumItemViewHolder>() {
    private val selectedItems = HashSet<Int>()
    private val forceRefresh  = AtomicBoolean(true)

    private fun getPhotoprismService() : PhotoprismHelper {
        return activity.fgService!!.photoprismHelper!!
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumItemViewHolder {
        val view: View = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.layout_album_item, parent, false)
        return AlbumItemViewHolder(view)
    }

    private fun setSelect(holder: AlbumItemViewHolder, position: Int) {
        holder.selected.visibility = if (selectedItems.contains(position)) {
            selectedItems.remove(position)
            View.INVISIBLE
        } else {
            selectedItems.add(position)
            View.VISIBLE
        }
    }

    override fun onBindViewHolder(holder: AlbumItemViewHolder, position: Int) {
        holder.image.setImageResource(android.R.color.transparent)
        val svc = getPhotoprismService()
        svc.createTaskForGenerateDirPreview(context, galleriesFilter, position, { image ->
            activity.runOnUiThread {
                try {
                    Glide
                        .with(context.applicationContext)
                        .load(image.img)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .centerCrop()
                        .into(holder.image)
                    holder.name.text = image.displayName
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
        val value = AtomicInteger(0)
        runBlocking {
            val job = activity.runIO({
                val count = getPhotoprismService().getAllGalleries(galleriesFilter, forceRefresh.getAndSet(false)).size
                //Log.d("DIRCOUNT", "Count = $count")
                value.set(count)
            }, {
                it.printStackTrace()
                //activity.runOnUiThread {
                    // TODO: add error message
                //}
            })
            job.join()
        }
        return value.get()
    }
}