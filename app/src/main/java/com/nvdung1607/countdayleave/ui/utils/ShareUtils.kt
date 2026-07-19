package com.nvdung1607.countdayleave.ui.utils

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

object ShareUtils {
    fun shareBitmap(context: Context, bitmap: Bitmap, title: String) {
        try {
            val cachePath = File(context.cacheDir, "images")
            cachePath.mkdirs() // Create directory if it doesn't exist
            val file = File(cachePath, "countdown_share.png")
            val stream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.close()

            val contentUri = FileProvider.getUriForFile(
                context,
                "com.nvdung1607.countdayleave.fileprovider",
                file
            )

            if (contentUri != null) {
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    setDataAndType(contentUri, context.contentResolver.getType(contentUri))
                    putExtra(Intent.EXTRA_STREAM, contentUri)
                    type = "image/png"
                }
                context.startActivity(Intent.createChooser(shareIntent, title))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun saveImageToGallery(context: Context, bitmap: Bitmap): Boolean {
        val filename = "Countdown_${System.currentTimeMillis()}.png"
        var fos: OutputStream? = null
        var imageUri: Uri? = null
        val resolver = context.contentResolver

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Countdown")
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
        }

        try {
            imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            if (imageUri != null) {
                fos = resolver.openOutputStream(imageUri)
                if (fos != null) {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
                    fos.flush()
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && imageUri != null) {
                contentValues.clear()
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(imageUri, contentValues, null, null)
            }
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            if (imageUri != null) {
                try {
                    resolver.delete(imageUri, null, null)
                } catch (ignored: Exception) {}
            }
            return false
        } finally {
            try {
                fos?.close()
            } catch (ignored: Exception) {}
        }
    }
}
