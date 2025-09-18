package com.unstableconfusion.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.unstableconfusion.app.data.models.GenerationConfig
import com.unstableconfusion.app.data.models.Scheduler

/**
 * Advanced settings card with generation parameters
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedSettingsCard(
    config: GenerationConfig,
    onConfigChange: (GenerationConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Advanced Settings",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Dimensions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = config.width.toString(),
                    onValueChange = { value ->
                        value.toIntOrNull()?.let { width ->
                            onConfigChange(config.copy(width = width))
                        }
                    },
                    label = { Text("Width") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                
                OutlinedTextField(
                    value = config.height.toString(),
                    onValueChange = { value ->
                        value.toIntOrNull()?.let { height ->
                            onConfigChange(config.copy(height = height))
                        }
                    },
                    label = { Text("Height") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Steps
            Text(
                text = "Steps: ${config.steps}",
                style = MaterialTheme.typography.bodyMedium
            )
            Slider(
                value = config.steps.toFloat(),
                onValueChange = { value ->
                    onConfigChange(config.copy(steps = value.toInt()))
                },
                valueRange = 10f..50f,
                steps = 39,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // CFG Scale
            Text(
                text = "CFG Scale: ${"%.1f".format(config.cfgScale)}",
                style = MaterialTheme.typography.bodyMedium
            )
            Slider(
                value = config.cfgScale,
                onValueChange = { value ->
                    onConfigChange(config.copy(cfgScale = value))
                },
                valueRange = 1f..20f,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Scheduler
            var expanded by remember { mutableStateOf(false) }
            
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it }
            ) {
                OutlinedTextField(
                    value = config.scheduler.displayName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Scheduler") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    Scheduler.values().forEach { scheduler ->
                        DropdownMenuItem(
                            text = { Text(scheduler.displayName) },
                            onClick = {
                                onConfigChange(config.copy(scheduler = scheduler))
                                expanded = false
                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Seed
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = config.seed?.toString() ?: "",
                    onValueChange = { value ->
                        val seed = if (value.isBlank()) null else value.toLongOrNull()
                        onConfigChange(config.copy(seed = seed))
                    },
                    label = { Text("Seed (optional)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                
                OutlinedButton(
                    onClick = {
                        val randomSeed = (0..Long.MAX_VALUE).random()
                        onConfigChange(config.copy(seed = randomSeed))
                    }
                ) {
                    Text("Random")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Batch Size
            Text(
                text = "Batch Size: ${config.batchSize}",
                style = MaterialTheme.typography.bodyMedium
            )
            Slider(
                value = config.batchSize.toFloat(),
                onValueChange = { value ->
                    onConfigChange(config.copy(batchSize = value.toInt()))
                },
                valueRange = 1f..4f,
                steps = 3,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}