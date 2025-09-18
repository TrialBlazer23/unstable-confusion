package com.unstableconfusion.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.unstableconfusion.app.ui.components.*
import com.unstableconfusion.app.ui.viewmodels.GenerationViewModel

/**
 * Main generation screen with prompt input and controls
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenerateScreen(
    viewModel: GenerationViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val generationProgress by viewModel.generationProgress.collectAsStateWithLifecycle()
    val stylePresets by viewModel.stylePresets.collectAsStateWithLifecycle()
    val promptSuggestions by viewModel.promptSuggestions.collectAsStateWithLifecycle()
    
    var showAdvancedSettings by remember { mutableStateOf(false) }
    var showPromptTools by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Generate Image",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            Row {
                IconButton(onClick = { showPromptTools = !showPromptTools }) {
                    Icon(Icons.Default.AutoFixHigh, contentDescription = "Prompt Tools")
                }
                IconButton(onClick = { showAdvancedSettings = !showAdvancedSettings }) {
                    Icon(Icons.Default.Tune, contentDescription = "Advanced Settings")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Prompt Tools (expandable)
        AnimatedVisibility(visible = showPromptTools) {
            PromptToolsSection(
                promptSuggestions = promptSuggestions,
                onSuggestionClick = { viewModel.addPromptSuggestion(it) },
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
        
        // Main prompt input
        PromptInputCard(
            prompt = uiState.prompt,
            negativePrompt = uiState.negativePrompt,
            onPromptChange = viewModel::updatePrompt,
            onNegativePromptChange = viewModel::updateNegativePrompt,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Style presets
        StylePresetsSection(
            presets = stylePresets,
            selectedPreset = uiState.config.stylePreset,
            onPresetSelect = viewModel::applyStylePreset,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Advanced settings (expandable)
        AnimatedVisibility(visible = showAdvancedSettings) {
            AdvancedSettingsCard(
                config = uiState.config,
                onConfigChange = viewModel::updateGenerationConfig,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )
        }
        
        // Generation progress
        generationProgress?.let { progress ->
            GenerationProgressCard(
                progress = progress,
                onCancel = viewModel::cancelGeneration,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )
        }
        
        // Error display
        uiState.error?.let { error ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Error,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = viewModel::clearError) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Dismiss",
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }
        
        // Generate button
        Button(
            onClick = viewModel::generateImage,
            enabled = !uiState.isGenerating && uiState.prompt.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            if (uiState.isGenerating) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = if (uiState.isGenerating) "Generating..." else "Generate Image",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}