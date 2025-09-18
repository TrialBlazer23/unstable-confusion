package com.unstableconfusion.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.unstableconfusion.app.domain.ModelType
import com.unstableconfusion.app.ui.viewmodels.GenerationViewModel
import com.unstableconfusion.app.ui.viewmodels.SettingsViewModel

/**
 * Enhanced settings screen with AI model management
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: GenerationViewModel
) {
    val context = LocalContext.current
    val settingsViewModel: SettingsViewModel = viewModel { SettingsViewModel(context.applicationContext as android.app.Application) }
    
    val availableModels by settingsViewModel.availableModels.collectAsStateWithLifecycle()
    val downloadedModels by settingsViewModel.downloadedModels.collectAsStateWithLifecycle()
    val downloadProgress by settingsViewModel.downloadProgress.collectAsStateWithLifecycle()
    val storageInfo by settingsViewModel.storageInfo.collectAsStateWithLifecycle()
    val engineInitialized by viewModel.engineInitialized.collectAsStateWithLifecycle()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // AI Engine Status
        EngineStatusCard(isInitialized = engineInitialized)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Model Management
        ModelManagementCard(
            settingsViewModel = settingsViewModel,
            availableModels = availableModels,
            downloadedModels = downloadedModels,
            downloadProgress = downloadProgress,
            storageInfo = storageInfo
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Memory Usage
        MemoryUsageCard(viewModel = viewModel)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Generation Settings
        GenerationSettingsCard()
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // App Information
        AppInfoCard()
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // About
        AboutCard()
    }
}

@Composable
private fun MemoryUsageCard(viewModel: GenerationViewModel) {
    val memoryInfo = viewModel.getMemoryInfo()
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Memory,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Memory Usage",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Used: ${memoryInfo.usedMemoryMB} MB",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Available: ${memoryInfo.availableMemoryMB} MB",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LinearProgressIndicator(
                progress = memoryInfo.usagePercentage,
                modifier = Modifier.fillMaxWidth(),
                color = when {
                    memoryInfo.usagePercentage > 0.8f -> MaterialTheme.colorScheme.error
                    memoryInfo.usagePercentage > 0.6f -> MaterialTheme.colorScheme.tertiary
                    else -> MaterialTheme.colorScheme.primary
                }
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "Usage: ${"%.1f".format(memoryInfo.usagePercentage * 100)}%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun GenerationSettingsCard() {
    var enableAutoSave by remember { mutableStateOf(true) }
    var enableHighQuality by remember { mutableStateOf(false) }
    var enableBatchMode by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Tune,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Generation Settings",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Auto-save toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Auto-save images",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Automatically save generated images",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = enableAutoSave,
                    onCheckedChange = { enableAutoSave = it }
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // High quality toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "High quality mode",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Use higher quality settings (slower)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = enableHighQuality,
                    onCheckedChange = { enableHighQuality = it }
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Batch mode toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Batch mode",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Enable batch generation workflows",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = enableBatchMode,
                    onCheckedChange = { enableBatchMode = it }
                )
            }
        }
    }
}

@Composable
private fun AppInfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "App Information",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Version:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "1.0.0",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "AI Engine:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "QNN/Genie Pipeline",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Privacy:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "100% On-Device",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun AboutCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "About Unstable Confusion",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "A fast, private, on-device text-to-image studio that brings Midjourney-like creative control to your mobile device. All image generation happens locally on your device - no data ever leaves your phone.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Features:",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            val features = listOf(
                "• Creative control with prompt tools and suggestions",
                "• Consistency tools for reproducible results",
                "• Style presets and LoRA model support",
                "• Advanced generation parameters",
                "• Batch workflows and upscaling",
                "• Complete privacy - everything stays on your device"
            )
            
            features.forEach { feature ->
                Text(
                    text = feature,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun EngineStatusCard(isInitialized: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    if (isInitialized) Icons.Default.CheckCircle else Icons.Default.Error,
                    contentDescription = null,
                    tint = if (isInitialized) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "AI Engine Status",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "QNN/Genie Pipeline",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = if (isInitialized) "Ready" else "Initializing...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isInitialized) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }
            
            if (!isInitialized) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun ModelManagementCard(
    settingsViewModel: SettingsViewModel,
    availableModels: List<com.unstableconfusion.app.domain.AvailableModel>,
    downloadedModels: List<com.unstableconfusion.app.domain.AvailableModel>,
    downloadProgress: com.unstableconfusion.app.domain.ModelDownloadProgress?,
    storageInfo: com.unstableconfusion.app.ui.viewmodels.StorageInfo
) {
    var expandedSection by remember { mutableStateOf<ModelType?>(null) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Storage,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "AI Models",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Storage usage
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Storage Used: ${storageInfo.usedMB} MB",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Available: ${storageInfo.availableMB} MB",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LinearProgressIndicator(
                progress = storageInfo.usagePercentage,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.tertiary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Download progress
            downloadProgress?.let { progress ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "Downloading ${progress.modelName}...",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = progress.progressPercentage,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${(progress.progressPercentage * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Model sections
            ModelType.values().forEach { modelType ->
                val models = availableModels.filter { it.type == modelType }
                if (models.isNotEmpty()) {
                    ModelTypeSection(
                        modelType = modelType,
                        models = models,
                        settingsViewModel = settingsViewModel,
                        isExpanded = expandedSection == modelType,
                        onToggleExpanded = { 
                            expandedSection = if (expandedSection == modelType) null else modelType 
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
            
            // Clear all models button
            if (downloadedModels.isNotEmpty()) {
                OutlinedButton(
                    onClick = { settingsViewModel.clearAllModels() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Clear All Models")
                }
            }
        }
    }
}

@Composable
private fun ModelTypeSection(
    modelType: ModelType,
    models: List<com.unstableconfusion.app.domain.AvailableModel>,
    settingsViewModel: SettingsViewModel,
    isExpanded: Boolean,
    onToggleExpanded: () -> Unit
) {
    val modelTypeName = when (modelType) {
        ModelType.BASE_MODEL -> "Base Models"
        ModelType.LORA -> "LoRA Models"
        ModelType.UPSCALER -> "Upscaler Models"
        ModelType.VAE -> "VAE Models"
    }
    
    val icon = when (modelType) {
        ModelType.BASE_MODEL -> Icons.Default.Hub
        ModelType.LORA -> Icons.Default.Tune
        ModelType.UPSCALER -> Icons.Default.HighQuality
        ModelType.VAE -> Icons.Default.Transform
    }
    
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = modelTypeName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }
            IconButton(onClick = onToggleExpanded) {
                Icon(
                    if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null
                )
            }
        }
        
        if (isExpanded) {
            models.forEach { model ->
                ModelItem(
                    model = model,
                    isDownloaded = settingsViewModel.isModelDownloaded(model.id),
                    onDownload = { settingsViewModel.downloadModel(model.id) },
                    onDelete = { settingsViewModel.deleteModel(model.id) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun ModelItem(
    model: com.unstableconfusion.app.domain.AvailableModel,
    isDownloaded: Boolean,
    onDownload: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = model.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = model.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Size: ${model.sizeBytes / (1024 * 1024)} MB",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                if (isDownloaded) {
                    Row {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(onClick = onDelete) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete model",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                } else {
                    Button(
                        onClick = onDownload,
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Download,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Download", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}