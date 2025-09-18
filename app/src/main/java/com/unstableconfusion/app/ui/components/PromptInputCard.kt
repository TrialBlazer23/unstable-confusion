package com.unstableconfusion.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp

/**
 * Card component for prompt input with positive and negative prompts
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromptInputCard(
    prompt: String,
    negativePrompt: String,
    onPromptChange: (String) -> Unit,
    onNegativePromptChange: (String) -> Unit,
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
                text = "Prompt",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            OutlinedTextField(
                value = prompt,
                onValueChange = onPromptChange,
                placeholder = { Text("Enter your prompt here...") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 6,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Negative Prompt (Optional)",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            OutlinedTextField(
                value = negativePrompt,
                onValueChange = onNegativePromptChange,
                placeholder = { Text("Enter negative prompt (optional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences
                )
            )
        }
    }
}