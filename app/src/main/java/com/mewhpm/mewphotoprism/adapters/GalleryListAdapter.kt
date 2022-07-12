package com.mewhpm.mewphotoprism.adapters

import android.content.Context
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.mewhpm.mewphotoprism.R
import com.mewhpm.mewphotoprism.entity.AccountEntity
import com.mewhpm.mewphotoprism.services.MainImageSource
import com.mewhpm.mewphotoprism.view_holders.GalleryItemViewHolder

class GalleryListAdapter(
    val accountEntity: AccountEntity,
    val context: Context,
    val onClick : (index : Int) -> Unit
) : RecyclerView.Adapter<GalleryItemViewHolder>() {
    var mainHandler: Handler = Handler(context.mainLooper)
    val selectedItems = HashSet<Int>()

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
        if (MainImageSource.getInstance(accountEntity).isLogin()) {
            return
        }
        MainImageSource.getInstance(accountEntity).preview(position, {
            Log.d("IMG", "Image $position loaded")
            mainHandler.post {
                Glide
                    .with(context.applicationContext)
                    .load(it.imageFullPath)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop()
                    .into(holder.image)
            }
        }, {
            Log.w("onBindViewHolder","Error while loading image $position")
            mainHandler.post {
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
        val count = MainImageSource.getInstance(accountEntity).getImagesCount()
        Log.d("IMGCOUNT", "Count = $count")
        return count
    }
}