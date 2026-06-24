package com.mbaraka.smartfilefinderv2.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FileDao {
    @Query("SELECT * FROM smart_files ORDER BY modifiedDate DESC")
    fun getAllFiles(): Flow<List<SmartFile>>

    @Query("SELECT * FROM smart_files WHERE fileName LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%'")
    fun searchFiles(query: String): Flow<List<SmartFile>>

    @Query("SELECT * FROM smart_files WHERE category = :category")
    fun getFilesByCategory(category: String): Flow<List<SmartFile>>

    @Query("SELECT * FROM smart_files WHERE lastOpened > 0 ORDER BY lastOpened DESC LIMIT 10")
    fun getRecentFiles(): Flow<List<SmartFile>>

    @Query("SELECT * FROM smart_files ORDER BY accessCount DESC LIMIT 10")
    fun getFrequentlyUsedFiles(): Flow<List<SmartFile>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFiles(files: List<SmartFile>)

    @Query("UPDATE smart_files SET accessCount = accessCount + 1, lastOpened = :timestamp WHERE path = :path")
    suspend fun incrementAccessCount(path: String, timestamp: Long)

    @Query("DELETE FROM smart_files")
    suspend fun deleteAll()
}
