package com.mewhpm.mewphotoprism.services.impl

@Deprecated("too old")
class MediaserverStorage {
//    var moduleContext: Context? = null
//    private val images : ArrayList<SimpleImage> = ArrayList()
//    private val lastUpdate = AtomicLong(0)
//
//    override fun getImagesCount(): Int {
//        if (images.isEmpty() && lastUpdate.get() < Date().time) {
//            queryImg({
//                images.add(SimpleImage(File(it).name, it, Date()))
//            }) {
//                //moduleContext!!
//            }
//        }
//        lastUpdate.set(Date().time +  2500)
//        return images.count()
//    }
//
//    override fun preview(index: Int, onSuccess: (image: SimpleImage) -> Unit, onError: () -> Unit) {
//        onSuccess(images[index])
//    }
//
//    override fun download(imageIndex: Int, onSuccess: (path: String) -> Unit, onError: () -> Unit) {
//        onSuccess(images[imageIndex].imageFullPath)
//    }
//
//    override fun setFilter(filterType: Int, additionalData: Map<String, String>) { }
//
//    override fun setContext(context: Context) {
//        moduleContext = context
//    }
//
//    private fun queryImg(onSuccess: (imagePath: String) -> Unit, onError: () -> Unit) {
//        if (moduleContext == null) {
//            throw IllegalStateException("moduleContext is null; run setContext() before this call")
//        }
//        try {
//            val contentResolver = moduleContext!!.contentResolver
//            //contentResolver.registerContentObserver()
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                contentResolver.query(
//                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
//                    arrayOf(MediaStore.Images.Media._ID),
//                    null,
//                    null,
//                    MediaStore.Images.Media.DATE_TAKEN + " DESC"
//                )?.use { cursor ->
//                    if (cursor.moveToFirst()) {
//                        do {
//                            val index = cursor.getColumnIndex(MediaStore.Images.Media._ID)
//                            val path = ContentUris.withAppendedId(
//                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
//                                cursor.getLong(if (index >= 0) index else 0)
//                            ).toString()
//                            onSuccess.invoke(path)
//                        } while (cursor.moveToNext())
//                    } else {
//                        moduleContext!!.contentResolver.query(
//                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
//                            null,
//                            null,
//                            null,
//                            MediaStore.Images.Media.DATA + " DESC"
//                        )?.use { cursor2 ->
//                            if (cursor2.moveToFirst()) {
//                                do {
//                                    val index2 = cursor2.getColumnIndex(MediaStore.Images.Media.DATA)
//                                    val path = cursor2.getString(if (index2 >= 0) index2 else 0);
//                                    onSuccess.invoke(path)
//                                } while (cursor2.moveToNext());
//                            }
//                        }
//                    }
//                }
//            }
//        } catch (t : Throwable) {
//            onError.invoke()
//        }
//    }
}