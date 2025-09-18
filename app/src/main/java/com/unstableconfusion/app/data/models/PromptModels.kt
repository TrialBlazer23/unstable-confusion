package com.unstableconfusion.app.data.models

/**
 * Prompt suggestion categories and templates
 */
data class PromptSuggestion(
    val id: String,
    val text: String,
    val category: PromptCategory,
    val description: String = "",
    val tags: List<String> = emptyList()
)

enum class PromptCategory(val displayName: String) {
    STYLE("Style"),
    LIGHTING("Lighting"),
    COMPOSITION("Composition"),
    SUBJECT("Subject"),
    ENVIRONMENT("Environment"),
    QUALITY("Quality"),
    CAMERA("Camera"),
    ARTIST("Artist"),
    MEDIUM("Medium"),
    COLOR("Color")
}

/**
 * Prompt building tools
 */
data class PromptTemplate(
    val id: String,
    val name: String,
    val template: String,
    val placeholders: List<String>,
    val category: String,
    val description: String = ""
)

/**
 * Saved prompt for reuse
 */
data class SavedPrompt(
    val id: String,
    val name: String,
    val prompt: String,
    val negativePrompt: String = "",
    val tags: List<String> = emptyList(),
    val timestamp: Long,
    val useCount: Int = 0
)