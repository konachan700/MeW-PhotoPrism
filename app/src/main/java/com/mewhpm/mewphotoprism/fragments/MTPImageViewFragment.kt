package com.mewhpm.mewphotoprism.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.github.ybq.android.spinkit.SpinKitView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.mewhpm.mewphotoprism.*
import com.mewhpm.mewphotoprism.services.helpers.MTPHelper
import com.mewhpm.mewphotoprism.utils.OnSwipeTouchListener
import com.mewhpm.mewphotoprism.utils.PP_SWIPE_LEFT
import com.mewhpm.mewphotoprism.utils.PP_SWIPE_RIGHT
import com.mewhpm.mewphotoprism.utils.runIO
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.concurrent.atomic.AtomicReference
import kotlin.io.path.absolutePathString
import kotlin.io.path.name

class MTPImageViewFragment : Fragment() {
    private var imageIndex : Long? = null

    var imageView : SubsamplingScaleImageView? = null
    var spinner : SpinKitView? = null
    var panel : BottomNavigationView? = null
    var downloadingProgress : TextView? = null
    val initialImageScale = AtomicReference<Float>(null)

    private fun getMTPHelper() : MTPHelper {
        return (requireActivity() as MainActivity).fgService!!.mtpHelper!!
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            imageIndex = it.getLong(ARG_IMAGE_INDEX)
        }
    }

    private fun reload(index : Int) {
        requireActivity().runIO({
            val svc = getMTPHelper()
            val bytes = svc.getOriginal(index)
            requireActivity().runOnUiThread {
                Glide
                    .with(requireContext().applicationContext)
                    .asBitmap()
                    .load(bytes)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .centerCrop()
                    .into(object : CustomTarget<Bitmap>() {
                        override fun onResourceReady(
                            resource: Bitmap,
                            transition: Transition<in Bitmap>?
                        ) {
                            val src = ImageSource.cachedBitmap(resource)
                            imageView!!.setImage(src)
                            imageView!!.visibility = View.VISIBLE
                            spinner!!.visibility = View.INVISIBLE
                            panel!!.visibility = View.VISIBLE
                        }
                        override fun onLoadCleared(placeholder: Drawable?) { }
                    })
            }
        }, {
            requireActivity().runOnUiThread {
                // TODO: add error message
                imageView!!.visibility = View.INVISIBLE
                spinner!!.visibility = View.INVISIBLE
                panel!!.visibility = View.INVISIBLE
            }
        })
    }

    private fun share(index: Int) {
        panel!!.visibility = View.INVISIBLE
        downloadingProgress!!.text = "Downloading..."
        downloadingProgress!!.visibility = View.VISIBLE
        requireActivity().runIO({
            val svc = getMTPHelper()
            val file = svc.downloadOriginal(index)
            requireActivity().runOnUiThread {
                val photoURI = FileProvider.getUriForFile(
                    requireContext().applicationContext,
                    requireContext().applicationContext.packageName.toString() + ".provider",
                    file.absoluteFile)
                val intent = Intent(Intent.ACTION_SEND)
                intent.type = "image/jpeg"
                intent.putExtra(Intent.EXTRA_SUBJECT, R.string.app_name)
                intent.putExtra(Intent.EXTRA_TEXT, "")
                intent.putExtra(Intent.EXTRA_STREAM, photoURI)
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                startActivity(intent)

                panel!!.visibility = View.VISIBLE
                downloadingProgress!!.visibility = View.INVISIBLE
            }
        }, {
            it.printStackTrace()
            requireActivity().runOnUiThread {
                panel!!.visibility = View.VISIBLE
                downloadingProgress!!.visibility = View.INVISIBLE
                Toast.makeText(requireContext(), "Error while downloading file", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun download(index : Int) {
        downloadingProgress!!.text = "Start downloading..."
        downloadingProgress!!.visibility = View.VISIBLE
        requireActivity().runIO({
            val svc = getMTPHelper()
            val file = svc.downloadOriginal(index)
            val dwPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val src = file.absoluteFile.toPath()
            val dst = File(dwPath, src.fileName.name).absoluteFile.toPath()
            Files.copy(src, dst, StandardCopyOption.REPLACE_EXISTING)

            val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            val contentUri = Uri.fromFile(dst.toFile())
            mediaScanIntent.data = contentUri
            requireActivity().sendBroadcast(mediaScanIntent)
            MediaScannerConnection.scanFile(context, arrayOf(dst.absolutePathString()), null, null)

            requireActivity().runOnUiThread {
                downloadingProgress!!.visibility = View.INVISIBLE
                Toast.makeText(requireContext(), "Download complete!", Toast.LENGTH_LONG).show()
            }
        }, {
            it.printStackTrace()
            requireActivity().runOnUiThread {
                downloadingProgress!!.visibility = View.INVISIBLE
                Toast.makeText(requireContext(), "Error while downloading file", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun swipe(dir : Int) {
        imageIndex = imageIndex!!.plus(dir)
        if (imageIndex!! < 0) {
            imageIndex = 0
            return
        }
        val svc = getMTPHelper()
        val count = svc.getCount()
        if (imageIndex!! > (count - 1)) {
            imageIndex = count.toLong() - 1L
            return
        }
        reload(imageIndex?.toInt() ?: -1)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_image_view, container, false)
        imageView = view.findViewById<SubsamplingScaleImageView>(R.id.imageView1)
        imageView!!.setOnTouchListener(object : OnSwipeTouchListener(requireContext()) {
            override fun onSwipeRight() {
                super.onSwipeRight()
                if (initialImageScale.get() != null &&
                    initialImageScale.get() > 0.0 &&
                    initialImageScale.get() == (imageView?.state?.scale ?: 0)) {
                    swipe(PP_SWIPE_RIGHT)
                }
            }

            override fun onSwipeLeft() {
                super.onSwipeLeft()
                if (initialImageScale.get() != null &&
                    initialImageScale.get() > 0.0 &&
                    initialImageScale.get() == (imageView?.state?.scale ?: 0)) {
                    swipe(PP_SWIPE_LEFT)
                }
            }
        })
        imageView!!.setOnImageEventListener(object : SubsamplingScaleImageView.OnImageEventListener {
            override fun onReady() {}

            override fun onImageLoaded() {
                initialImageScale.set(imageView!!.state!!.scale)
            }
            override fun onPreviewLoadError(e: Exception?) {}
            override fun onImageLoadError(e: Exception?) {}
            override fun onTileLoadError(e: Exception?) {}
            override fun onPreviewReleased() {}
        })

        spinner = view.findViewById<SpinKitView>(R.id.waitSpinner2)
        downloadingProgress = view.findViewById<TextView>(R.id.downloadingProgress)
        panel = view.findViewById<BottomNavigationView>(R.id.bottomNavView1)
        //panel!!.itemIconTintList = null
        panel!!.menu.findItem(R.id.likeButton).isVisible = false
        panel!!.visibility = View.INVISIBLE
        panel!!.setOnItemSelectedListener {
            onMenuItemClick(it)
            false
        }
        panel!!.setOnItemReselectedListener {
            onMenuItemClick(it)
        }
        reload(imageIndex?.toInt() ?: -1)
        return view
    }

    private fun onMenuItemClick(it : MenuItem) {
        when (it.itemId) {
            R.id.downloadButton -> {
                download(imageIndex!!.toInt())
            }
            R.id.shareButton -> {
                share(imageIndex!!.toInt())
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(imageIndex : Long) =
            MTPImageViewFragment().apply {
                arguments = Bundle().apply {
                    putLong(ARG_IMAGE_INDEX, imageIndex)
                }
            }
    }
}