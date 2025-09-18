package com.unstableconfusion.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unstableconfusion.app.data.models.*
import com.unstableconfusion.app.data.repository.AppRepository
import com.unstableconfusion.app.data.repository.MockAppRepository
import com.unstableconfusion.app.domain.ImageGenerationEngine
import com.unstableconfusion.app.domain.MockImageGenerationEngine
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

/**
 * Main ViewModel for image generation functionality
 */
class GenerationViewModel : ViewModel() {
    
    private val repository: AppRepository = MockAppRepository()
    private val generationEngine: ImageGenerationEngine = MockImageGenerationEngine()
    
    private val _uiState = MutableStateFlow(GenerationUiState())
    val uiState: StateFlow<GenerationUiState> = _uiState.asStateFlow()
    
    private val _generationProgress = MutableStateFlow<GenerationProgress?>(null)
    val generationProgress: StateFlow<GenerationProgress?> = _generationProgress.asStateFlow()
    
    private val _generatedImages = MutableStateFlow<List<GeneratedImage>>(emptyList())
    val generatedImages: StateFlow<List<GeneratedImage>> = _generatedImages.asStateFlow()
    
    private val _stylePresets = MutableStateFlow<List<StylePreset>>(emptyList())
    val stylePresets: StateFlow<List<StylePreset>> = _stylePresets.asStateFlow()
    
    private val _promptSuggestions = MutableStateFlow<List<PromptSuggestion>>(emptyList())
    val promptSuggestions: StateFlow<List<PromptSuggestion>> = _promptSuggestions.asStateFlow()
    
    init {
        initializeEngine()
        loadData()
    }
    
    private fun initializeEngine() {
        viewModelScope.launch {
            generationEngine.initialize()
        }
    }
    
    private fun loadData() {
        viewModelScope.launch {
            // Load style presets
            _stylePresets.value = repository.getStylePresets()
            
            // Load prompt suggestions
            _promptSuggestions.value = repository.getPromptSuggestions()
            
            // Load generated images
            repository.getGeneratedImagesFlow().collect { images ->
                _generatedImages.value = images
            }
        }
    }
    
    fun updatePrompt(prompt: String) {
        _uiState.update { it.copy(prompt = prompt) }
    }
    
    fun updateNegativePrompt(negativePrompt: String) {
        _uiState.update { it.copy(negativePrompt = negativePrompt) }
    }
    
    fun updateGenerationConfig(config: GenerationConfig) {
        _uiState.update { it.copy(config = config) }
    }
    
    fun generateImage() {
        if (!generationEngine.isReady()) {
            _uiState.update { it.copy(error = "Generation engine not ready") }
            return
        }
        
        val currentState = _uiState.value
        val config = currentState.config.copy(
            prompt = currentState.prompt,
            negativePrompt = currentState.negativePrompt
        )
        
        viewModelScope.launch {
            _uiState.update { it.copy(isGenerating = true, error = null) }
            
            generationEngine.generateImage(config).collect { progress ->
                _generationProgress.value = progress
                
                if (progress.isComplete) {
                    // Create mock generated image
                    val generatedImage = GeneratedImage(
                        id = UUID.randomUUID().toString(),
                        imagePath = "/mock/path/generated_${System.currentTimeMillis()}.jpg",
                        thumbnailPath = "/mock/path/thumb_${System.currentTimeMillis()}.jpg",
                        config = config,
                        timestamp = System.currentTimeMillis(),
                        generationTimeMs = (config.steps * 100).toLong() // Mock timing
                    )
                    
                    repository.saveGeneratedImage(generatedImage)
                    _uiState.update { it.copy(isGenerating = false) }
                    _generationProgress.value = null
                }
                
                if (progress.error != null) {
                    _uiState.update { it.copy(isGenerating = false, error = progress.error) }
                    _generationProgress.value = null
                }
            }
        }
    }
    
    fun cancelGeneration() {
        viewModelScope.launch {
            generationEngine.cancelGeneration()
            _uiState.update { it.copy(isGenerating = false) }
            _generationProgress.value = null
        }
    }
    
    fun applyStylePreset(preset: StylePreset) {
        val currentState = _uiState.value
        val updatedConfig = currentState.config.copy(
            cfgScale = preset.cfgScale,
            steps = preset.steps,
            stylePreset = preset
        )
        
        val updatedPrompt = if (currentState.prompt.isBlank()) {
            preset.promptAddition
        } else {
            "${currentState.prompt}, ${preset.promptAddition}"
        }
        
        val updatedNegativePrompt = if (currentState.negativePrompt.isBlank()) {
            preset.negativePromptAddition
        } else if (preset.negativePromptAddition.isNotBlank()) {
            "${currentState.negativePrompt}, ${preset.negativePromptAddition}"
        } else {
            currentState.negativePrompt
        }
        
        _uiState.update { 
            it.copy(
                config = updatedConfig,
                prompt = updatedPrompt,
                negativePrompt = updatedNegativePrompt
            ) 
        }
    }
    
    fun addPromptSuggestion(suggestion: String) {
        val currentPrompt = _uiState.value.prompt
        val updatedPrompt = if (currentPrompt.isBlank()) {
            suggestion
        } else {
            "$currentPrompt, $suggestion"
        }
        updatePrompt(updatedPrompt)
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    fun getMemoryInfo() = generationEngine.getMemoryInfo()
    
    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            generationEngine.release()
        }
    }
}

/**
 * UI state for the generation screen
 */
data class GenerationUiState(
    val prompt: String = "",
    val negativePrompt: String = "",
    val config: GenerationConfig = GenerationConfig(prompt = ""),
    val isGenerating: Boolean = false,
    val error: String? = null
)