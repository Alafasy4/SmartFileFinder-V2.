package com.mbaraka.smartfilefinderv2.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FileRepository(private val fileDao: FileDao) {
    val allFiles: Flow<List<SmartFile>> = fileDao.getAllFiles()
    val recentFiles: Flow<List<SmartFile>> = fileDao.getRecentFiles()
    val frequentlyUsed: Flow<List<SmartFile>> = fileDao.getFrequentlyUsedFiles()

    private val semanticMap = mapOf(
        "assignment" to listOf("homework", "task", "project", "lab", "assignment"),
        "lecture" to listOf("notes", "class", "slides", "presentation", "lecture"),
        "video" to listOf("movie", "clip", "film", "mp4", "mkv"),
        "music" to listOf("song", "audio", "mp3", "track", "quran"),
        "photo" to listOf("image", "picture", "vacation", "dcim", "jpg", "png")
    )

    fun searchFiles(query: String): Flow<List<SmartFile>> {
        val lowerQuery = query.trim().lowercase()
        if (lowerQuery.isEmpty()) return allFiles
        
        val keywords = semanticMap[lowerQuery] ?: listOf(lowerQuery)
        
        return fileDao.getAllFiles().map { list ->
            list.filter { file ->
                keywords.any { keyword ->
                    file.fileName.contains(keyword, ignoreCase = true) || 
                    file.category.contains(keyword, ignoreCase = true)
                }
            }
        }
    }

    fun getFilesByCategory(category: String): Flow<List<SmartFile>> = fileDao.getFilesByCategory(category)

    suspend fun insertFiles(files: List<SmartFile>) = fileDao.insertFiles(files)

    suspend fun markFileOpened(path: String) {
        fileDao.incrementAccessCount(path, System.currentTimeMillis())
    }

    suspend fun clearDatabase() = fileDao.deleteAll()
}
