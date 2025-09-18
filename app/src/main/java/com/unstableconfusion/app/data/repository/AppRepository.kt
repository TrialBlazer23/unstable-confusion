package com.unstableconfusion.app.data.repository

import com.unstableconfusion.app.data.models.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Repository for managing generated images, prompts, and app state
 */
interface AppRepository {
    
    // Generated Images
    suspend fun saveGeneratedImage(image: GeneratedImage)
    suspend fun getGeneratedImages(): List<GeneratedImage>
    suspend fun deleteGeneratedImage(imageId: String)
    fun getGeneratedImagesFlow(): Flow<List<GeneratedImage>>
    
    // Saved Prompts
    suspend fun savePrompt(prompt: SavedPrompt)
    suspend fun getSavedPrompts(): List<SavedPrompt>
    suspend fun deletePrompt(promptId: String)
    suspend fun incrementPromptUseCount(promptId: String)
    
    // Style Presets
    suspend fun getStylePresets(): List<StylePreset>
    suspend fun saveCustomStylePreset(preset: StylePreset)
    
    // LoRA Models
    suspend fun getLoraModels(): List<LoraModel>
    suspend fun saveLoraModel(lora: LoraModel)
    suspend fun updateLoraModel(lora: LoraModel)
    
    // Prompt Suggestions
    suspend fun getPromptSuggestions(category: PromptCategory? = null): List<PromptSuggestion>
    suspend fun getPromptTemplates(): List<PromptTemplate>
    
    // App Settings
    suspend fun saveGenerationConfig(config: GenerationConfig)
    suspend fun getLastGenerationConfig(): GenerationConfig?
    
    // Recent activity
    fun getRecentActivity(): StateFlow<List<GeneratedImage>>
}

/**
 * In-memory implementation for development and testing
 */
class MockAppRepository : AppRepository {
    
    private val _generatedImages = MutableStateFlow<List<GeneratedImage>>(emptyList())
    private val _savedPrompts = mutableListOf<SavedPrompt>()
    private val _stylePresets = mutableListOf<StylePreset>()
    private val _loraModels = mutableListOf<LoraModel>()
    private var _lastConfig: GenerationConfig? = null
    
    init {
        // Initialize with some default data
        initializeDefaultData()
    }
    
    override suspend fun saveGeneratedImage(image: GeneratedImage) {
        val currentImages = _generatedImages.value.toMutableList()
        currentImages.add(0, image) // Add to beginning
        _generatedImages.value = currentImages
    }
    
    override suspend fun getGeneratedImages(): List<GeneratedImage> {
        return _generatedImages.value
    }
    
    override suspend fun deleteGeneratedImage(imageId: String) {
        val currentImages = _generatedImages.value.toMutableList()
        currentImages.removeAll { it.id == imageId }
        _generatedImages.value = currentImages
    }
    
    override fun getGeneratedImagesFlow(): Flow<List<GeneratedImage>> {
        return _generatedImages
    }
    
    override suspend fun savePrompt(prompt: SavedPrompt) {
        _savedPrompts.add(0, prompt)
    }
    
    override suspend fun getSavedPrompts(): List<SavedPrompt> {
        return _savedPrompts.toList()
    }
    
    override suspend fun deletePrompt(promptId: String) {
        _savedPrompts.removeAll { it.id == promptId }
    }
    
    override suspend fun incrementPromptUseCount(promptId: String) {
        val index = _savedPrompts.indexOfFirst { it.id == promptId }
        if (index != -1) {
            val prompt = _savedPrompts[index]
            _savedPrompts[index] = prompt.copy(useCount = prompt.useCount + 1)
        }
    }
    
    override suspend fun getStylePresets(): List<StylePreset> {
        return _stylePresets.toList()
    }
    
    override suspend fun saveCustomStylePreset(preset: StylePreset) {
        _stylePresets.add(preset)
    }
    
    override suspend fun getLoraModels(): List<LoraModel> {
        return _loraModels.toList()
    }
    
    override suspend fun saveLoraModel(lora: LoraModel) {
        _loraModels.add(lora)
    }
    
    override suspend fun updateLoraModel(lora: LoraModel) {
        val index = _loraModels.indexOfFirst { it.id == lora.id }
        if (index != -1) {
            _loraModels[index] = lora
        }
    }
    
    override suspend fun getPromptSuggestions(category: PromptCategory?): List<PromptSuggestion> {
        return getDefaultPromptSuggestions().filter { category == null || it.category == category }
    }
    
    override suspend fun getPromptTemplates(): List<PromptTemplate> {
        return getDefaultPromptTemplates()
    }
    
    override suspend fun saveGenerationConfig(config: GenerationConfig) {
        _lastConfig = config
    }
    
    override suspend fun getLastGenerationConfig(): GenerationConfig? {
        return _lastConfig
    }
    
    override fun getRecentActivity(): StateFlow<List<GeneratedImage>> {
        return _generatedImages
    }
    
    private fun initializeDefaultData() {
        // Add default style presets
        _stylePresets.addAll(getDefaultStylePresets())
        
        // Add default LoRA models (placeholder)
        _loraModels.addAll(getDefaultLoraModels())
    }
    
    private fun getDefaultStylePresets(): List<StylePreset> {
        return listOf(
            StylePreset(
                id = "photorealistic",
                name = "Photorealistic",
                description = "High quality photorealistic images",
                promptAddition = "photorealistic, high quality, detailed, 8k resolution",
                negativePromptAddition = "blurry, low quality, cartoon, anime"
            ),
            StylePreset(
                id = "artistic",
                name = "Artistic",
                description = "Artistic and creative style",
                promptAddition = "artistic, creative, masterpiece, detailed",
                negativePromptAddition = "low quality, blurry"
            ),
            StylePreset(
                id = "anime",
                name = "Anime",
                description = "Anime and manga style",
                promptAddition = "anime style, manga, detailed, colorful",
                negativePromptAddition = "realistic, photorealistic, 3d"
            ),
            StylePreset(
                id = "cinematic",
                name = "Cinematic",
                description = "Cinematic movie-like quality",
                promptAddition = "cinematic lighting, dramatic, film grain, professional",
                negativePromptAddition = "amateur, low quality"
            )
        )
    }
    
    private fun getDefaultLoraModels(): List<LoraModel> {
        return listOf(
            LoraModel(
                id = "detail_enhancer",
                name = "Detail Enhancer",
                path = "/models/lora/detail_enhancer.safetensors",
                description = "Enhances fine details in generated images"
            ),
            LoraModel(
                id = "lighting_control",
                name = "Lighting Control",
                path = "/models/lora/lighting_control.safetensors",
                description = "Better control over lighting and shadows"
            )
        )
    }
    
    private fun getDefaultPromptSuggestions(): List<PromptSuggestion> {
        return listOf(
            // Style suggestions
            PromptSuggestion("1", "photorealistic", PromptCategory.STYLE, "High quality realistic images"),
            PromptSuggestion("2", "oil painting", PromptCategory.STYLE, "Traditional oil painting style"),
            PromptSuggestion("3", "watercolor", PromptCategory.STYLE, "Soft watercolor painting style"),
            
            // Lighting suggestions
            PromptSuggestion("4", "golden hour lighting", PromptCategory.LIGHTING, "Warm sunset lighting"),
            PromptSuggestion("5", "dramatic lighting", PromptCategory.LIGHTING, "High contrast dramatic lighting"),
            PromptSuggestion("6", "soft diffused light", PromptCategory.LIGHTING, "Gentle even lighting"),
            
            // Quality suggestions
            PromptSuggestion("7", "highly detailed", PromptCategory.QUALITY, "Enhanced detail quality"),
            PromptSuggestion("8", "8k resolution", PromptCategory.QUALITY, "Ultra high resolution"),
            PromptSuggestion("9", "masterpiece", PromptCategory.QUALITY, "Highest quality artwork"),
            
            // Composition suggestions
            PromptSuggestion("10", "rule of thirds", PromptCategory.COMPOSITION, "Balanced composition"),
            PromptSuggestion("11", "close-up portrait", PromptCategory.COMPOSITION, "Tight portrait framing"),
            PromptSuggestion("12", "wide landscape", PromptCategory.COMPOSITION, "Expansive landscape view")
        )
    }
    
    private fun getDefaultPromptTemplates(): List<PromptTemplate> {
        return listOf(
            PromptTemplate(
                id = "portrait",
                name = "Portrait Template",
                template = "portrait of {subject}, {style}, {lighting}, highly detailed",
                placeholders = listOf("subject", "style", "lighting"),
                category = "Portrait"
            ),
            PromptTemplate(
                id = "landscape",
                name = "Landscape Template", 
                template = "{environment} landscape, {time_of_day}, {weather}, {style}",
                placeholders = listOf("environment", "time_of_day", "weather", "style"),
                category = "Landscape"
            ),
            PromptTemplate(
                id = "fantasy",
                name = "Fantasy Template",
                template = "{creature} in {environment}, magical, {style}, dramatic lighting",
                placeholders = listOf("creature", "environment", "style"),
                category = "Fantasy"
            )
        )
    }
}