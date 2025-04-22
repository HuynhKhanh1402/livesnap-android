package dev.vku.livesnap.data.remote.dto.request

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.graphics.scale
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.io.InputStream

data class UploadSnapRequest(
    val imageUri: Uri,
    val caption: String
) {
    fun toMultipartParts(context: Context): Pair<MultipartBody.Part, RequestBody> {
        val contentResolver = context.contentResolver
        val inputStream: InputStream? = contentResolver.openInputStream(imageUri)
        val originalBitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()

        // Nén ảnh xuống 80% chất lượng (tùy chỉnh tỷ lệ nén)
        val compressedBitmap = compressImage(originalBitmap)

        // Chuyển ảnh nén thành byte array
        val byteArrayOutputStream = ByteArrayOutputStream()
        compressedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)
        val compressedBytes = byteArrayOutputStream.toByteArray()

        // Tạo Multipart cho file
        val fileName = "image_${System.currentTimeMillis()}.jpg"
        val requestFile = compressedBytes.toRequestBody("image/*".toMediaTypeOrNull())
        val imagePart = MultipartBody.Part.createFormData("image", fileName, requestFile)

        // Tạo Multipart cho caption
        val captionPart = caption.toRequestBody("text/plain".toMediaTypeOrNull())

        return imagePart to captionPart
    }

    private fun compressImage(bitmap: Bitmap): Bitmap {
        // Nén ảnh để giảm kích thước file mà không làm giảm quá nhiều chất lượng
        // Bạn có thể tùy chỉnh kích thước ảnh sau khi nén bằng cách thay đổi `width` và `height`
        val maxSize = 1024 // Đặt giới hạn kích thước ảnh tối đa (Ví dụ 1024px)
        var width = bitmap.width
        var height = bitmap.height

        if (width > maxSize || height > maxSize) {
            val ratio = width.toFloat() / height.toFloat()
            if (ratio > 1) {
                width = maxSize
                height = (maxSize / ratio).toInt()
            } else {
                height = maxSize
                width = (maxSize * ratio).toInt()
            }
        }

        return bitmap.scale(width, height)
    }
}