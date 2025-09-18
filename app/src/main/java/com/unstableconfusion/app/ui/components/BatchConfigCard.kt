package com.unstableconfusion.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
 * Batch configuration card for setting up batch generation workflows
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatchConfigCard(
    baseConfig: GenerationConfig,
    batchConfig: BatchConfig?,
    onBatchConfigChange: (BatchConfig) -> Unit,
    onStartBatch: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedSeedMode by remember { mutableStateOf(BatchSeedMode.RANDOM) }
    var selectedOutputFormat by remember { mutableStateOf(BatchOutputFormat.INDIVIDUAL_FILES) }
    var variations by remember { mutableStateOf(listOf<BatchVariation>()) }
    
    // Update batch config when values change
    LaunchedEffect(selectedSeedMode, selectedOutputFormat, variations) {
        val newBatchConfig = BatchConfig(
            baseConfig = baseConfig,
            variations = variations,
            seedMode = selectedSeedMode,
            outputFormat = selectedOutputFormat
        )
        onBatchConfigChange(newBatchConfig)
    }
    
    Card(
        modifier = modifier,
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
                        Icons.Default.Queue,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Batch Generation",
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
            
            if (expanded) {
                Spacer(modifier = Modifier.height(16.dp))
                
                // Seed Mode Selection
                Text(
                    text = "Seed Mode",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BatchSeedMode.values().forEach { mode ->
                        FilterChip(
                            onClick = { selectedSeedMode = mode },
                            label = { Text(mode.displayName) },
                            selected = selectedSeedMode == mode,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Output Format Selection
                Text(
                    text = "Output Format",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Column {
                    BatchOutputFormat.values().forEach { format ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedOutputFormat == format,
                                onClick = { selectedOutputFormat = format }
                            )
                            Text(
                                text = format.displayName,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Variations Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Variations",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    
                    OutlinedButton(
                        onClick = {
                            variations = variations + BatchVariation(
                                type = BatchVariationType.PROMPT_VARIATIONS,
                                values = emptyList()
                            )
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Variation")
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Variation List
                variations.forEachIndexed { index, variation ->
                    VariationItem(
                        variation = variation,
                        onVariationChange = { newVariation ->
                            variations = variations.toMutableList().apply {
                                set(index, newVariation)
                            }
                        },
                        onRemove = {
                            variations = variations.toMutableList().apply {
                                removeAt(index)
                            }
                        }
                    )
                    
                    if (index < variations.size - 1) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Start Batch Button
                Button(
                    onClick = onStartBatch,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = batchConfig != null
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Start Batch Generation")
                }
            }
        }
    }
}

@Composable
private fun VariationItem(
    variation: BatchVariation,
    onVariationChange: (BatchVariation) -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var variationValues by remember { mutableStateOf(variation.values.joinToString(", ")) }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Variation Type Dropdown
                Box {
                    OutlinedButton(onClick = { expanded = true }) {
                        Text(variation.type.displayName)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                    
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        BatchVariationType.values().forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.displayName) },
                                onClick = {
                                    onVariationChange(variation.copy(type = type))
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                
                IconButton(onClick = onRemove) {
                    Icon(Icons.Default.Delete, contentDescription = "Remove variation")
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Values input
            OutlinedTextField(
                value = variationValues,
                onValueChange = { 
                    variationValues = it
                    val values = it.split(",").map { value -> value.trim() }.filter { value -> value.isNotBlank() }
                    onVariationChange(variation.copy(values = values))
                },
                label = { Text("Values (comma-separated)") },
                placeholder = { Text("Enter values separated by commas") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )
        }
    }
}