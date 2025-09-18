package com.unstableconfusion.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.unstableconfusion.app.data.models.PromptCategory
import com.unstableconfusion.app.data.models.PromptSuggestion

/**
 * Expandable section with prompt building tools and suggestions
 */
@Composable
fun PromptToolsSection(
    promptSuggestions: List<PromptSuggestion>,
    onSuggestionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf<PromptCategory?>(null) }
    
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Prompt Tools",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            // Category selector
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                item {
                    FilterChip(
                        onClick = { selectedCategory = null },
                        label = { Text("All") },
                        selected = selectedCategory == null
                    )
                }
                items(PromptCategory.values()) { category ->
                    FilterChip(
                        onClick = { selectedCategory = category },
                        label = { Text(category.displayName) },
                        selected = selectedCategory == category
                    )
                }
            }
            
            // Suggestions
            val filteredSuggestions = if (selectedCategory != null) {
                promptSuggestions.filter { it.category == selectedCategory }
            } else {
                promptSuggestions
            }
            
            LazyColumn(
                modifier = Modifier.height(200.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(filteredSuggestions) { suggestion ->
                    PromptSuggestionItem(
                        suggestion = suggestion,
                        onClick = { onSuggestionClick(suggestion.text) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PromptSuggestionItem(
    suggestion: PromptSuggestion,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = suggestion.text,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (suggestion.description.isNotBlank()) {
                    Text(
                        text = suggestion.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            
            Icon(
                Icons.Default.Add,
                contentDescription = "Add to prompt",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}