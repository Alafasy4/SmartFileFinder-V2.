package com.mbaraka.smartfilefinderv2.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mbaraka.smartfilefinderv2.data.SmartFile
import com.mbaraka.smartfilefinderv2.ui.components.FileItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartFileFinderScreen(
    viewModel: FileViewModel = viewModel(),
    onOpenFile: (SmartFile) -> Unit
) {
    val allFiles by viewModel.allFiles.collectAsState()
    val recentFiles by viewModel.recentFiles.collectAsState()
    val recommendedFiles by viewModel.recommendedFiles.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()

    // Handle System Back Button
    BackHandler(enabled = selectedCategory != null || searchQuery.isNotEmpty()) {
        viewModel.onCategorySelect(null)
        viewModel.onSearchQueryChange("")
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.background(MaterialTheme.colorScheme.surface),
        topBar = {
            LargeTopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Smart File Finder",
                            fontWeight = FontWeight.ExtraBold,
                            style = MaterialTheme.typography.headlineMedium
                        )
                        if (isScanning) {
                            Text(
                                text = "Scanning your device...",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                navigationIcon = {
                    if (selectedCategory != null || searchQuery.isNotEmpty()) {
                        IconButton(onClick = { 
                            viewModel.onCategorySelect(null)
                            viewModel.onSearchQueryChange("")
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.scanFiles() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = if (isScanning) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.scanFiles() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape
            ) {
                if (isScanning) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.Search, contentDescription = "Scan")
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChange(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Tafuta mafaili (e.g. 'picha')") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                            Icon(Icons.Default.Close, contentDescription = null)
                        }
                    }
                },
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedBorderColor = MaterialTheme.colorScheme.primary
                ),
                singleLine = true
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (searchQuery.isEmpty() && selectedCategory == null) {
                    // Recommendation Section
                    if (recommendedFiles.isNotEmpty()) {
                        item {
                            SectionHeader(
                                title = "Zilizopendekezwa",
                                subtitle = "Kulingana na matumizi yako",
                                icon = Icons.Default.AutoAwesome
                            )
                            RecommendationRow(recommendedFiles) {
                                viewModel.onFileOpened(it)
                                onOpenFile(it)
                            }
                        }
                    }

                    // Categories
                    item {
                        SectionHeader(title = "Makundi", icon = Icons.Default.Category)
                        CategoryGrid(
                            selectedCategory = selectedCategory,
                            onCategoryClick = { viewModel.onCategorySelect(it) }
                        )
                    }

                    // Recent Files
                    if (recentFiles.isNotEmpty()) {
                        item {
                            SectionHeader(title = "Faili za Karibuni", icon = Icons.Default.History)
                        }
                        items(recentFiles) { file ->
                            PaddingWrapper {
                                FileItem(file = file) {
                                    viewModel.onFileOpened(file)
                                    onOpenFile(file)
                                }
                            }
                        }
                    }
                } else {
                    // Results
                    item {
                        val title = if (searchQuery.isNotEmpty()) "Matokeo ya Utafutaji" else "Kundi: $selectedCategory"
                        SectionHeader(title = title, icon = Icons.AutoMirrored.Filled.List)
                    }
                    items(allFiles) { file ->
                        PaddingWrapper {
                            FileItem(file = file) {
                                viewModel.onFileOpened(file)
                                onOpenFile(file)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PaddingWrapper(content: @Composable () -> Unit) {
    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
        content()
    }
}

@Composable
fun SectionHeader(title: String, subtitle: String? = null, icon: ImageVector? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun CategoryGrid(
    selectedCategory: String?,
    onCategoryClick: (String) -> Unit
) {
    val categories = listOf(
        "Images" to Icons.Default.Image,
        "Videos" to Icons.Default.VideoLibrary,
        "Audio" to Icons.Default.AudioFile,
        "Documents" to Icons.Default.Description,
        "APKs" to Icons.Default.Android,
        "Others" to Icons.Default.Folder
    )

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(categories) { (name, icon) ->
            CategoryChip(
                name = name,
                icon = icon,
                isSelected = name == selectedCategory,
                onClick = { onCategoryClick(name) }
            )
        }
    }
}

@Composable
fun CategoryChip(
    name: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .size(width = 110.dp, height = 100.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = name,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun RecommendationRow(files: List<SmartFile>, onFileClick: (SmartFile) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(files) { file ->
            Card(
                modifier = Modifier
                    .width(180.dp)
                    .clickable { onFileClick(file) },
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Decorative background element
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(60.dp)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                    
                    Column(modifier = Modifier.padding(16.dp)) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = file.fileName,
                            maxLines = 2,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                lineHeight = 18.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "${file.score.toInt()}%",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Match",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}
