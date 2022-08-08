package com.mewhpm.mewphotoprism.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.github.ybq.android.spinkit.SpinKitView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.mewhpm.mewphotoprism.AppDatabase
import com.mewhpm.mewphotoprism.Const
import com.mewhpm.mewphotoprism.R
import com.mewhpm.mewphotoprism.entity.AccountEntity
import com.mewhpm.mewphotoprism.services.Storage
import com.mewhpm.mewphotoprism.services.proto.ReadableStorage
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import kotlin.io.path.name

class ImageViewFragment : Fragment() {
    private var imageIndex: Long? = null
    private var account : AccountEntity? = null
    private var accountID : Long = -1
    private var currentImagePath : String? = null

    private lateinit var db : AppDatabase
    private lateinit var imageSource: ReadableStorage

    var imageView : SubsamplingScaleImageView? = null
    var spinner : SpinKitView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            imageIndex = it.getLong(Const.ARG_IMAGE_INDEX)
            accountID = it.getLong(Const.ARG_ACCOUNT_ID)
        }
        if (accountID == -1L) {
            throw IllegalArgumentException("Bad parameter 'accID'")
        }
        db = AppDatabase.getDB(requireContext())
        account = db.AccountsDAO().getByUID(accountID)
        imageSource = Storage.getInstance(account!!, requireContext(), ReadableStorage::class.java)
    }

    private fun reload(index : Int) {
        if (index > 0) imageIndex = index.toLong()
        imageSource.download(imageIndex!!.toInt(), {
            requireActivity().runOnUiThread {
                imageView!!.setImage(ImageSource.uri(it));
                spinner!!.visibility = View.INVISIBLE
                requireView().findViewById<BottomNavigationView>(R.id.bottomNavView1).visibility = View.VISIBLE
                currentImagePath = it
            }
        }, {
            requireActivity().runOnUiThread {
                imageView!!.visibility = View.INVISIBLE
                spinner!!.visibility = View.INVISIBLE
            }
        })
    }

    private fun download() {
        val dwPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val src = File(currentImagePath!!).absoluteFile.toPath()
        val dst = File(dwPath, src.fileName.name).absoluteFile.toPath()
        Files.copy(src, dst, StandardCopyOption.REPLACE_EXISTING)

        val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        val contentUri = Uri.fromFile(dst.toFile())
        mediaScanIntent.data = contentUri
        requireActivity().sendBroadcast(mediaScanIntent)
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

        reload(-1)
        return view
    }

    companion object {
        @JvmStatic
        fun newInstance(account : AccountEntity?, imageIndex : Long) =
            ImageViewFragment().apply {
                arguments = Bundle().apply {
                    putLong(Const.ARG_IMAGE_INDEX, imageIndex)
                    putLong(Const.ARG_ACCOUNT_ID, account?.uid ?: -1)
                }
            }
    }
}