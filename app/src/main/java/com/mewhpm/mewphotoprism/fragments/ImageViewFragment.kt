package com.mewhpm.mewphotoprism.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.github.ybq.android.spinkit.SpinKitView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.mewhpm.mewphotoprism.*
import com.mewhpm.mewphotoprism.entity.AccountEntity
import com.mewhpm.mewphotoprism.services.helpers.PhotoprismHelper
import com.mewhpm.mewphotoprism.services.helpers.PhotoprismPredefinedFilters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.concurrent.ConcurrentHashMap
import kotlin.io.path.name

class ImageViewFragment : Fragment() {
    private var imageIndex          : Long? = null
    private var account             : AccountEntity? = null
    private var accountID           : Long = -1
    private var currentImagePath    : String? = null
    private var filter              : PhotoprismPredefinedFilters? = null
    private var extra               : HashMap<String, Any>? = null

    private lateinit var db : AppDatabase

    var imageView : SubsamplingScaleImageView? = null
    var spinner : SpinKitView? = null

    private fun getPhotoprismService() : PhotoprismHelper {
        return (requireActivity() as MainActivity).fgService!!.photoprismHelper!!
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            imageIndex = it.getLong(ARG_IMAGE_INDEX)
            accountID = it.getLong(ARG_ACCOUNT_ID)
            //filter = it.getSerializable(ARG_FILTER) as PhotoprismPredefinedFilters
            //extra = it.getSerializable(ARG_EXTRA) as HashMap<String, Any>
        }
        if (accountID == -1L) {
            throw IllegalArgumentException("Bad parameter 'accID'")
        }
        db = AppDatabase.getDB(requireContext())
        account = db.AccountsDAO().getByUID(accountID)
    }

    private fun reload(filter : PhotoprismPredefinedFilters, extra : HashMap<String, Any>, index : Int) {
        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                if (index > 0) {
                    imageIndex = index.toLong()
                } else {
                    requireActivity().runOnUiThread {
                        // TODO: add error message
                        imageView!!.visibility = View.INVISIBLE
                        spinner!!.visibility = View.INVISIBLE
                    }
                    return@runCatching
                }
                try {
                    val svc = getPhotoprismService()
                    // TODO: add selector for big preview (fastly) or original (slowly, but high quality)
                    val img = svc.getImage(requireContext(), filter, extra, index)
                    val file = getPhotoprismService()
                        .downloadOriginalAsFile(
                            requireContext(),
                            img.hash
                        ) { fileSize, downloaded ->
                            // TODO: add progress
                        }
                    requireActivity().runOnUiThread {
                        imageView!!.setImage(ImageSource.uri(file!!.absolutePath));
                        spinner!!.visibility = View.INVISIBLE
                        requireView().findViewById<BottomNavigationView>(R.id.bottomNavView1).visibility = View.VISIBLE
                        currentImagePath = file.absolutePath
                    }
                } catch (e : Exception) {
                    requireActivity().runOnUiThread {
                        // TODO: add error message
                        imageView!!.visibility = View.INVISIBLE
                        spinner!!.visibility = View.INVISIBLE
                    }
                }
            }
        }
    }

    private fun download() {
        try {
            // TODO: add "copying started" message, because sometimes copy process is very slow
            val dwPath =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val src = File(currentImagePath!!).absoluteFile.toPath()
            val dst = File(dwPath, src.fileName.name).absoluteFile.toPath()
            Files.copy(src, dst, StandardCopyOption.REPLACE_EXISTING)

            val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            val contentUri = Uri.fromFile(dst.toFile())
            mediaScanIntent.data = contentUri
            requireActivity().sendBroadcast(mediaScanIntent)
        } catch (e : Exception) {
            e.printStackTrace()
            // TODO: add error message
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_image_view, container, false)
        imageView = view.findViewById<SubsamplingScaleImageView>(R.id.imageView1)
        spinner = view.findViewById<SpinKitView>(R.id.waitSpinner2)

        val panel = view.findViewById<BottomNavigationView>(R.id.bottomNavView1)
        panel.visibility = View.INVISIBLE
        panel.setOnItemReselectedListener {
            when (it.itemId) {
                R.id.downloadButton -> {
                    download()
                    Toast.makeText(requireContext(),
                        "File moved to download folder", Toast.LENGTH_SHORT).show()
                }
                R.id.shareButton -> {
                    if (currentImagePath != null) {
                        val photoURI = FileProvider.getUriForFile(
                            requireContext().applicationContext,
                            requireContext().applicationContext.packageName.toString() + ".provider",
                            File(currentImagePath!!).absoluteFile)
                        val intent = Intent(Intent.ACTION_SEND)
                        intent.type = "image/jpeg"
                        intent.putExtra(Intent.EXTRA_SUBJECT, R.string.app_name)
                        intent.putExtra(Intent.EXTRA_TEXT, "")
                        intent.putExtra(Intent.EXTRA_STREAM, photoURI)
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        startActivity(intent)
                    }
                }
            }
        }

        reload(filter!!, extra!!, imageIndex?.toInt() ?: -1)
        return view
    }

    companion object {
        @JvmStatic
        fun newInstance(account : AccountEntity?, imageIndex : Long, filter : PhotoprismPredefinedFilters, extra : ConcurrentHashMap<String, Any>) =
            ImageViewFragment().apply {
                arguments = Bundle().apply {
                    putLong(ARG_IMAGE_INDEX, imageIndex)
                    putLong(ARG_ACCOUNT_ID, account?.uid ?: -1)
                    putSerializable(ARG_FILTER, filter)
                    putSerializable(ARG_EXTRA, extra)
                }
            }
    }
}