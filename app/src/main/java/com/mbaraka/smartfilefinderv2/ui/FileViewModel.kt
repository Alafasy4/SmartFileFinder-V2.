package com.mbaraka.smartfilefinderv2.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mbaraka.smartfilefinderv2.data.AppDatabase
import com.mbaraka.smartfilefinderv2.data.FileRepository
import com.mbaraka.smartfilefinderv2.data.SmartFile
import com.mbaraka.smartfilefinderv2.utils.FileScanner
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

class FileViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: FileRepository
    private val scanner: FileScanner

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory = _selectedCategory.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning = _isScanning.asStateFlow()

    init {
        val dao = AppDatabase.getDatabase(application).fileDao()
        repository = FileRepository(dao)
        scanner = FileScanner(application)
    }

    val allFiles = combine(_searchQuery, _selectedCategory) { query, category ->
        query to category
    }.flatMapLatest { (query, category) ->
        when {
            query.isNotEmpty() -> repository.searchFiles(query)
            category != null -> repository.getFilesByCategory(category)
            else -> repository.allFiles
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val recentFiles = repository.recentFiles.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val frequentlyUsed = repository.frequentlyUsed.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Recommendation logic: High frequency + Recent + Time matching
    val recommendedFiles = repository.allFiles.map { all ->
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        
        all.map { file ->
            // Calculate a score (Intelligence)
            var score = 0f
            
            // 1. Frequency score (up to 50 points)
            score += (file.accessCount * 5).coerceAtMost(50)
            
            // 2. Recency score (up to 30 points)
            val hoursSinceLastOpen = (System.currentTimeMillis() - file.lastOpened) / (1000 * 60 * 60)
            if (hoursSinceLastOpen < 24) score += 30
            else if (hoursSinceLastOpen < 72) score += 15
            
            // 3. Time-of-day affinity (Simulated Intelligence - 20 points)
            // If it's morning and it's a "Document", or evening and it's "Video"
            if (currentHour in 8..17 && file.category == "Documents") score += 20
            if (currentHour in 19..23 && (file.category == "Videos" || file.category == "Audio")) score += 20

            file.copy(score = score)
        }
        .filter { it.score > 20 }
        .sortedByDescending { it.score }
        .take(5)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun scanFiles() {
        viewModelScope.launch {
            _isScanning.value = true
            val files = scanner.scanDevice()
            repository.insertFiles(files)
            _isScanning.value = false
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        if (query.isNotEmpty()) _selectedCategory.value = null
    }

    fun onCategorySelect(category: String?) {
        _selectedCategory.value = if (_selectedCategory.value == category) null else category
        if (_selectedCategory.value != null) _searchQuery.value = ""
    }

    fun onFileOpened(file: SmartFile) {
        viewModelScope.launch {
            repository.markFileOpened(file.path)
        }
    }
}
