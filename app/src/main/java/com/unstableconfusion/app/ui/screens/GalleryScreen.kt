package com.unstableconfusion.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.unstableconfusion.app.data.models.GeneratedImage
import com.unstableconfusion.app.ui.components.ExportShareCard
import com.unstableconfusion.app.ui.viewmodels.GenerationViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Gallery screen displaying generated images
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(
    viewModel: GenerationViewModel
) {
    val generatedImages by viewModel.generatedImages.collectAsStateWithLifecycle()
    val exportProgress by viewModel.exportProgress.collectAsStateWithLifecycle()
    var selectedImages by remember { mutableStateOf(setOf<String>()) }
    var selectedImage by remember { mutableStateOf<GeneratedImage?>(null) }
    var showExportOptions by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (selectedImages.isNotEmpty()) {
                Text(
                    text = "${selectedImages.size} selected",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            } else {
                Text(
                    text = "Gallery",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Row {
                if (selectedImages.isNotEmpty()) {
                    IconButton(onClick = { showExportOptions = !showExportOptions }) {
                        Icon(Icons.Default.Share, contentDescription = "Export & Share")
                    }
                    IconButton(onClick = { selectedImages = setOf() }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear selection")
                    }
                } else {
                    Text(
                        text = "${generatedImages.size} images",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Export options (when images are selected)
        if (showExportOptions && selectedImages.isNotEmpty()) {
            val selectedImageObjects = generatedImages.filter { selectedImages.contains(it.id) }
            ExportShareCard(
                selectedImages = selectedImageObjects,
                onExportSingle = { image, config ->
                    viewModel.exportImage(image.imagePath, "${image.id}_export", config)
                },
                onExportMultiple = { images, config ->
                    val imagePaths = images.map { it.imagePath }
                    viewModel.exportImagesAsZip(imagePaths, "batch_export", config)
                },
                onUpscale = { image, config ->
                    viewModel.upscaleImageAdvanced(image.imagePath, config)
                },
                onShare = { image ->
                    // In real implementation, would use Android's share intent
                    // For now, just show a message
                },
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
        
        // Export progress
        exportProgress?.let { progress ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = progress,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
        
        if (generatedImages.isEmpty()) {
            // Empty state
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.PhotoLibrary,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "No images generated yet",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Text(
                        text = "Start generating images to see them here",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            // Image grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(generatedImages) { image ->
                    ImageGridItem(
                        image = image,
                        isSelected = selectedImages.contains(image.id),
                        onClick = { selectedImage = image },
                        onLongClick = {
                            selectedImages = if (selectedImages.contains(image.id)) {
                                selectedImages - image.id
                            } else {
                                selectedImages + image.id
                            }
                        },
                        onSelectionClick = {
                            selectedImages = if (selectedImages.contains(image.id)) {
                                selectedImages - image.id
                            } else {
                                selectedImages + image.id
                            }
                        }
                    )
                }
            }
        }
    }
    
    // Image detail dialog
    selectedImage?.let { image ->
        ImageDetailDialog(
            image = image,
            onDismiss = { selectedImage = null },
            onUsePrompt = { prompt ->
                viewModel.updatePrompt(prompt)
                selectedImage = null
            },
            onUseSeed = { seed ->
                val currentConfig = viewModel.uiState.value.config
                viewModel.updateGenerationConfig(currentConfig.copy(seed = seed))
                selectedImage = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ImageGridItem(
    image: GeneratedImage,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onSelectionClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable { 
                if (isSelected) {
                    onSelectionClick()
                } else {
                    onClick()
                }
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = if (isSelected) {
            BorderStroke(3.dp, MaterialTheme.colorScheme.primary)
        } else null
    ) {
        Box {
            // Placeholder for image (since we don't have actual images in mock)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = listOf(
                            Color(0xFF6750A4),
                            Color(0xFF625B71),
                            Color(0xFF7D5260),
                            Color(0xFF2196F3),
                            Color(0xFF4CAF50)
                        ).random()
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Image,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = Color.White.copy(alpha = 0.7f)
                )
            }
            
            // Selection indicator
            if (isSelected) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Selected",
                        modifier = Modifier.padding(4.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
            
            // Generation info overlay
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth(),
                color = Color.Black.copy(alpha = 0.7f),
                shape = RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text(
                        text = image.config.prompt,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Text(
                        text = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
                            .format(Date(image.timestamp)),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Composable
private fun ImageDetailDialog(
    image: GeneratedImage,
    onDismiss: () -> Unit,
    onUsePrompt: (String) -> Unit,
    onUseSeed: (Long?) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Image Details") },
        text = {
            Column {
                Text(
                    text = "Prompt:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = image.config.prompt,
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                if (image.config.negativePrompt.isNotBlank()) {
                    Text(
                        text = "Negative Prompt:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = image.config.negativePrompt,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Size: ${image.config.width}x${image.config.height}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "Steps: ${image.config.steps}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "CFG: ${image.config.cfgScale}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    
                    Column {
                        Text(
                            text = "Scheduler: ${image.config.scheduler.displayName}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        image.config.seed?.let { seed ->
                            Text(
                                text = "Seed: $seed",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Text(
                            text = "Time: ${image.generationTimeMs}ms",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        },
        confirmButton = {
            Row {
                TextButton(onClick = { onUsePrompt(image.config.prompt) }) {
                    Text("Use Prompt")
                }
                
                image.config.seed?.let { seed ->
                    TextButton(onClick = { onUseSeed(seed) }) {
                        Text("Use Seed")
                    }
                }
                
                TextButton(onClick = onDismiss) {
                    Text("Close")
                }
            }
        }
    )
}