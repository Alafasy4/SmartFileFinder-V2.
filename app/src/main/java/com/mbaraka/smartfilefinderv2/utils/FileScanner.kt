package com.mbaraka.smartfilefinderv2.utils

import android.content.Context
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import com.mbaraka.smartfilefinderv2.data.SmartFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class FileScanner(private val context: Context) {

    suspend fun scanDevice(): List<SmartFile> = withContext(Dispatchers.IO) {
        val fileList = mutableListOf<SmartFile>()
        val contentResolver = context.contentResolver

        val uri = MediaStore.Files.getContentUri("external")
        val projection = arrayOf(
            MediaStore.Files.FileColumns.DATA,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.SIZE,
            MediaStore.Files.FileColumns.DATE_MODIFIED,
            MediaStore.Files.FileColumns.MIME_TYPE
        )

        val cursor = contentResolver.query(uri, projection, null, null, null)

        cursor?.use {
            val dataIndex = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)
            val nameIndex = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
            val sizeIndex = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
            val dateIndex = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_MODIFIED)
            val mimeIndex = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE)

            while (it.moveToNext()) {
                val path = it.getString(dataIndex)
                val name = it.getString(nameIndex) ?: File(path).name
                val size = it.getLong(sizeIndex)
                val modified = it.getLong(dateIndex) * 1000 // Convert to ms
                val mime = it.getString(mimeIndex) ?: getMimeType(path)
                
                val extension = name.substringAfterLast(".", "").lowercase()
                val category = getCategory(mime, extension)

                fileList.add(
                    SmartFile(
                        path = path,
                        fileName = name,
                        extension = extension,
                        size = size,
                        modifiedDate = modified,
                        mimeType = mime,
                        category = category
                    )
                )
            }
        }
        return@withContext fileList
    }

    private fun getMimeType(url: String): String {
        val extension = MimeTypeMap.getFileExtensionFromUrl(url)
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: "application/octet-stream"
    }

    private fun getCategory(mime: String, extension: String): String {
        val mimeLower = mime.lowercase()
        return when {
            mimeLower.startsWith("image") -> "Images"
            mimeLower.startsWith("video") -> "Videos"
            mimeLower.startsWith("audio") -> "Audio"
            // Documents - Expanded to cover more types
            mimeLower.contains("pdf") || extension == "pdf" -> "Documents"
            mimeLower.contains("word") || mimeLower.contains("officedocument") || 
                extension in listOf("doc", "docx", "txt", "rtf", "odt") -> "Documents"
            mimeLower.contains("excel") || mimeLower.contains("sheet") || 
                extension in listOf("xls", "xlsx", "csv", "ods") -> "Documents"
            mimeLower.contains("powerpoint") || mimeLower.contains("presentation") || 
                extension in listOf("ppt", "pptx", "odp") -> "Documents"
            extension == "apk" -> "APKs"
            else -> "Others"
        }
    }
}
