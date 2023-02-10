package com.mewhpm.mewphotoprism.view_holders

import android.content.Context
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.mewhpm.mewphotoprism.MainActivity
import com.mewhpm.mewphotoprism.R
import com.mewhpm.mewphotoprism.services.helpers.PhotoprismPredefinedFilters
import com.mewhpm.mewphotoprism.utils.runIO
import java.security.SecureRandom
import java.util.concurrent.atomic.AtomicLong

class GalleryItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val image : ImageView = itemView.findViewById<View>(R.id.galleryViewItemIV) as ImageView
    val selected : ImageView = itemView.findViewById<View>(R.id.imageViewSelected) as ImageView

    private val taskCode = AtomicLong()
    private val secureRandom = SecureRandom()

    fun setImageAsync(
        activity    : MainActivity,
        context     : Context,
        filter      : PhotoprismPredefinedFilters,
        extra       : Map<String, Any>,
        id          : Int
    ) {
        taskCode.set(secureRandom.nextLong())
        val currentTaskCode = taskCode.get()

        image.setImageResource(android.R.color.transparent)
        image.visibility = View.INVISIBLE
        val svc = activity.fgService!!.photoprismHelper!!

        activity.runIO({
            svc.createTaskForGenerateImagePreview(context, filter, extra, id, {
                activity.runOnUiThread {
                    try {
                        Glide
                            .with(context.applicationContext)
                            .load(it.img)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .centerCrop()
                            .into(image)
                        image.visibility = if (currentTaskCode == taskCode.get()) View.VISIBLE else View.INVISIBLE
                    } catch (t : Throwable) {
                        image.setImageResource(R.drawable.icon_broken_image)
                        image.visibility = View.VISIBLE
                        Log.e("GLIDE", "Error ${t.message}")
                    }
                }
            }, {
                activity.runOnUiThread {
                    // TODO: add error message
                    image.setImageResource(R.drawable.icon_broken_image)
                    image.visibility = View.VISIBLE
                }
            })
        }, {
            // TODO: add error message
            image.setImageResource(R.drawable.icon_broken_image)
            image.visibility = View.VISIBLE
        })
    }
}