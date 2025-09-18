package com.unstableconfusion.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
    var selectedImage by remember { mutableStateOf<GeneratedImage?>(null) }
    
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
            Text(
                text = "Gallery",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "${generatedImages.size} images",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
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
                        onClick = { selectedImage = image }
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
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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