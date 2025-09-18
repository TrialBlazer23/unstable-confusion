package com.unstableconfusion.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.unstableconfusion.app.data.models.*

/**
 * Export and sharing options card
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportShareCard(
    selectedImages: List<GeneratedImage>,
    onExportSingle: (GeneratedImage, ExportConfig) -> Unit,
    onExportMultiple: (List<GeneratedImage>, ExportConfig) -> Unit,
    onUpscale: (GeneratedImage, UpscaleConfig) -> Unit,
    onShare: (GeneratedImage) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var exportFormat by remember { mutableStateOf(ExportFormat.JPEG) }
    var exportQuality by remember { mutableStateOf(90) }
    var includeMetadata by remember { mutableStateOf(true) }
    var upscaleAlgorithm by remember { mutableStateOf(UpscaleAlgorithm.REAL_ESRGAN) }
    var upscaleScale by remember { mutableStateOf(2) }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Share,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Export & Share",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (expanded) "Collapse" else "Expand"
                    )
                }
            }
            
            if (selectedImages.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${selectedImages.size} image(s) selected",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (expanded) {
                Spacer(modifier = Modifier.height(16.dp))
                
                // Quick actions
                if (selectedImages.isNotEmpty()) {
                    Text(
                        text = "Quick Actions",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (selectedImages.size == 1) {
                            item {
                                AssistChip(
                                    onClick = { onShare(selectedImages.first()) },
                                    label = { Text("Share") },
                                    leadingIcon = {
                                        Icon(Icons.Default.Share, contentDescription = null)
                                    }
                                )
                            }
                        }
                        
                        item {
                            AssistChip(
                                onClick = {
                                    val config = ExportConfig(
                                        format = exportFormat,
                                        quality = exportQuality,
                                        includeMetadata = includeMetadata
                                    )
                                    if (selectedImages.size == 1) {
                                        onExportSingle(selectedImages.first(), config)
                                    } else {
                                        onExportMultiple(selectedImages, config)
                                    }
                                },
                                label = { Text("Export") },
                                leadingIcon = {
                                    Icon(Icons.Default.Download, contentDescription = null)
                                }
                            )
                        }
                        
                        if (selectedImages.size == 1) {
                            item {
                                AssistChip(
                                    onClick = {
                                        val config = UpscaleConfig(
                                            algorithm = upscaleAlgorithm,
                                            scaleFactor = upscaleScale,
                                            preserveMetadata = includeMetadata
                                        )
                                        onUpscale(selectedImages.first(), config)
                                    },
                                    label = { Text("Upscale") },
                                    leadingIcon = {
                                        Icon(Icons.Default.HighQuality, contentDescription = null)
                                    }
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                // Export format settings
                Text(
                    text = "Export Settings",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Format selection
                Text(
                    text = "Format",
                    style = MaterialTheme.typography.bodySmall
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(ExportFormat.values()) { format ->
                        FilterChip(
                            onClick = { exportFormat = format },
                            label = { Text(format.displayName) },
                            selected = exportFormat == format
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Quality slider
                if (exportFormat == ExportFormat.JPEG || exportFormat == ExportFormat.WEBP) {
                    Text(
                        text = "Quality: $exportQuality%",
                        style = MaterialTheme.typography.bodySmall
                    )
                    
                    Slider(
                        value = exportQuality.toFloat(),
                        onValueChange = { exportQuality = it.toInt() },
                        valueRange = 50f..100f,
                        steps = 9,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                }
                
                // Metadata toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Include metadata",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Switch(
                        checked = includeMetadata,
                        onCheckedChange = { includeMetadata = it }
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                Divider()
                Spacer(modifier = Modifier.height(16.dp))
                
                // Upscaling settings
                Text(
                    text = "Upscaling Settings",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Algorithm selection
                Text(
                    text = "Algorithm",
                    style = MaterialTheme.typography.bodySmall
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(UpscaleAlgorithm.values()) { algorithm ->
                        FilterChip(
                            onClick = { upscaleAlgorithm = algorithm },
                            label = { Text(algorithm.displayName) },
                            selected = upscaleAlgorithm == algorithm
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Scale factor
                Text(
                    text = "Scale Factor: ${upscaleScale}x",
                    style = MaterialTheme.typography.bodySmall
                )
                
                Slider(
                    value = upscaleScale.toFloat(),
                    onValueChange = { upscaleScale = it.toInt() },
                    valueRange = 2f..8f,
                    steps = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}