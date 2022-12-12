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
import com.mewhpm.mewphotoprism.services.Storage
import com.mewhpm.mewphotoprism.services.proto.DirectoriesStorage
import com.mewhpm.mewphotoprism.services.proto.SecuredStorage
import com.mewhpm.mewphotoprism.utils.download
import com.mewhpm.mewphotoprism.utils.getTemporaryFileForPreview
import com.mewhpm.mewphotoprism.view_holders.AlbumItemViewHolder
import okhttp3.OkHttpClient

class DirectoryListAdapter(
    val type : Int,
    val accountEntity: AccountEntity,
    val context: Context,
    val onClick : (index : Int) -> Unit
) : RecyclerView.Adapter<AlbumItemViewHolder>() {
    var mainHandler: Handler = Handler(context.mainLooper)
    val selectedItems = HashSet<Int>()
    val http = OkHttpClient()

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
        if (Storage.getInstance(accountEntity, context, SecuredStorage::class.java).isLogin()) {
            return
        }
        Storage.getInstance(accountEntity, context, DirectoriesStorage::class.java).getDir(position, type, {
//            Log.d("IMG-DIR", "Image $position (${it.name}) loaded")
            val file = http.getTemporaryFileForPreview(it.cover.imageID, context)
            http.download(it.cover.imageFullPath, file.path, { path ->
                mainHandler.post {
                    try {
                        Glide
                            .with(context.applicationContext)
                            .load(path)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .centerCrop()
                            .into(holder.image)
                    } catch (t : Throwable) {
                        Log.e("GLIDE", "Error ${t.message}")
                    }
                }
            }, { err ->
                Log.e("PREVIEW", "Error: ${err.message}")
            })
        }, {
            Log.w("onBindViewHolder","Error while loading cover image $position")
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
        val count = Storage.getInstance(accountEntity, context, DirectoriesStorage::class.java).getDirsCount(type)
        Log.d("DIRCOUNT", "Count = $count")
        return count
    }
}