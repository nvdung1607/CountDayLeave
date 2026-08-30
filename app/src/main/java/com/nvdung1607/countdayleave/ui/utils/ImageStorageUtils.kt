package com.nvdung1607.countdayleave.ui.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object ImageStorageUtils {

    private const val BACKGROUND_DIR = "event_backgrounds"
    private const val MAX_DIMENSION = 1920

    /**
     * Sao chép và thu nhỏ ảnh từ Uri vào bộ nhớ trong của ứng dụng.
     * Trả về đường dẫn tuyệt đối của file đã lưu, hoặc null nếu lỗi.
     */
    suspend fun saveImageFromUri(context: Context, uri: Uri, eventId: String): String? = withContext(Dispatchers.IO) {
        try {
            val dir = File(context.filesDir, BACKGROUND_DIR).apply {
                if (!exists()) mkdirs()
            }
            val targetFile = File(dir, "bg_${eventId}.jpg")

            // Đọc kích thước ảnh trước để decode kích thước hợp lý (tránh OutOfMemory)
            var inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream?.close()

            // Tính inSampleSize
            var sampleSize = 1
            val maxSide = maxOf(options.outWidth, options.outHeight)
            if (maxSide > MAX_DIMENSION) {
                sampleSize = maxSide / MAX_DIMENSION
            }

            // Decode ảnh thực tế
            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
            }
            inputStream = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream, null, decodeOptions)
            inputStream?.close()

            if (bitmap != null) {
                FileOutputStream(targetFile).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
                }
                bitmap.recycle()
                targetFile.absolutePath
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Xóa file ảnh nền của sự kiện khi xóa sự kiện hoặc xóa ảnh.
     */
    fun deleteImage(filePath: String?) {
        if (filePath.isNullOrBlank()) return
        try {
            val file = File(filePath)
            if (file.exists()) {
                file.delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
