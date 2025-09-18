package com.unstableconfusion.app.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.unstableconfusion.app.data.models.*
import com.unstableconfusion.app.data.repository.AppRepository
import com.unstableconfusion.app.data.repository.MockAppRepository
import com.unstableconfusion.app.domain.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

/**
 * Main ViewModel for image generation functionality with AI integration
 */
class GenerationViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository: AppRepository = MockAppRepository()
    private val generationEngine: ImageGenerationEngine = QnnGenieImageGenerationEngine(application)
    private val modelManager: ModelManager = ModelManager(application)
    
    private val _uiState = MutableStateFlow(GenerationUiState())
    val uiState: StateFlow<GenerationUiState> = _uiState.asStateFlow()
    
    private val _generationProgress = MutableStateFlow<GenerationProgress?>(null)
    val generationProgress: StateFlow<GenerationProgress?> = _generationProgress.asStateFlow()
    
    private val _batchProgress = MutableStateFlow<BatchProgress?>(null)
    val batchProgress: StateFlow<BatchProgress?> = _batchProgress.asStateFlow()
    
    private val _generatedImages = MutableStateFlow<List<GeneratedImage>>(emptyList())
    val generatedImages: StateFlow<List<GeneratedImage>> = _generatedImages.asStateFlow()
    
    private val _batchConfig = MutableStateFlow<BatchConfig?>(null)
    val batchConfig: StateFlow<BatchConfig?> = _batchConfig.asStateFlow()
    
    private val _exportProgress = MutableStateFlow<String?>(null)
    val exportProgress: StateFlow<String?> = _exportProgress.asStateFlow()
    
    private val _stylePresets = MutableStateFlow<List<StylePreset>>(emptyList())
    val stylePresets: StateFlow<List<StylePreset>> = _stylePresets.asStateFlow()
    
    private val _promptSuggestions = MutableStateFlow<List<PromptSuggestion>>(emptyList())
    val promptSuggestions: StateFlow<List<PromptSuggestion>> = _promptSuggestions.asStateFlow()
    
    // Model management state
    private val _availableModels = MutableStateFlow<List<AvailableModel>>(emptyList())
    val availableModels: StateFlow<List<AvailableModel>> = _availableModels.asStateFlow()
    
    private val _downloadProgress = MutableStateFlow<ModelDownloadProgress?>(null)
    val downloadProgress: StateFlow<ModelDownloadProgress?> = _downloadProgress.asStateFlow()
    
    private val _engineInitialized = MutableStateFlow(false)
    val engineInitialized: StateFlow<Boolean> = _engineInitialized.asStateFlow()
    
    init {
        initializeEngine()
        loadData()
        loadModelData()
    }
    
    private fun initializeEngine() {
        viewModelScope.launch {
            _uiState.update { it.copy(isInitializing = true) }
            
            val result = generationEngine.initialize()
            if (result.isSuccess) {
                _engineInitialized.value = true
                _uiState.update { it.copy(isInitializing = false) }
            } else {
                _engineInitialized.value = false
                _uiState.update { 
                    it.copy(
                        isInitializing = false, 
                        error = "Failed to initialize AI engine: ${result.exceptionOrNull()?.message}"
                    ) 
                }
            }
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
    
    private fun loadModelData() {
        viewModelScope.launch {
            _availableModels.value = modelManager.getAvailableModels()
            
            // Update LoRA models in repository with downloaded ones
            val downloadedLoraModels = modelManager.getDownloadedLoraModels()
            // In a real implementation, you'd update the repository with these models
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
            _uiState.update { it.copy(error = "AI engine not ready. Please wait for initialization to complete.") }
            return
        }
        
        val currentState = _uiState.value
        if (currentState.prompt.isBlank()) {
            _uiState.update { it.copy(error = "Please enter a prompt") }
            return
        }
        
        val config = currentState.config.copy(
            prompt = currentState.prompt,
            negativePrompt = currentState.negativePrompt
        )
        
        viewModelScope.launch {
            _uiState.update { it.copy(isGenerating = true, error = null) }
            
            generationEngine.generateImage(config).collect { progress ->
                _generationProgress.value = progress
                
                if (progress.isComplete && progress.error == null) {
                    // In real implementation, the engine would return the actual image path
                    // For now we'll create a realistic generated image entry
                    val generatedImage = GeneratedImage(
                        id = UUID.randomUUID().toString(),
                        imagePath = "${getApplication<Application>().filesDir}/generated_images/generated_${System.currentTimeMillis()}.jpg",
                        thumbnailPath = "${getApplication<Application>().filesDir}/generated_images/thumb_${System.currentTimeMillis()}.jpg",
                        config = config,
                        timestamp = System.currentTimeMillis(),
                        generationTimeMs = (progress.currentStep * 150).toLong() // More realistic timing
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
    
    // Model Management Functions
    
    fun downloadModel(modelId: String) {
        viewModelScope.launch {
            modelManager.downloadModel(modelId).collect { progress ->
                _downloadProgress.value = progress
                
                if (progress.isComplete) {
                    _downloadProgress.value = null
                    loadModelData() // Refresh available models
                }
            }
        }
    }
    
    fun deleteModel(modelId: String) {
        viewModelScope.launch {
            val success = modelManager.deleteModel(modelId)
            if (success) {
                loadModelData() // Refresh available models
            }
        }
    }
    
    fun isModelDownloaded(modelId: String): Boolean {
        return modelManager.isModelDownloaded(modelId)
    }
    
    fun getModelStorageInfo(): Pair<Long, Long> {
        return Pair(
            modelManager.getTotalModelStorageMB(),
            modelManager.getAvailableStorageMB()
        )
    }
    
    fun clearAllModels() {
        viewModelScope.launch {
            modelManager.clearAllModels()
            loadModelData()
        }
    }
    
    fun upscaleImage(imagePath: String, scaleFactor: Int = 2) {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true) }
            
            val result = generationEngine.upscaleImage(imagePath, scaleFactor)
            if (result.isSuccess) {
                // Image upscaled successfully
                _uiState.update { it.copy(isProcessing = false) }
            } else {
                _uiState.update { 
                    it.copy(
                        isProcessing = false, 
                        error = "Upscaling failed: ${result.exceptionOrNull()?.message}"
                    ) 
                }
            }
        }
    }
    
    fun upscaleImageAdvanced(imagePath: String, upscaleConfig: UpscaleConfig) {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true) }
            _exportProgress.value = "Upscaling with ${upscaleConfig.algorithm.displayName}..."
            
            val result = generationEngine.upscaleImage(imagePath, upscaleConfig)
            if (result.isSuccess) {
                _exportProgress.value = "Upscaling complete!"
                _uiState.update { it.copy(isProcessing = false) }
                // Add upscaled image to gallery
                val upscaledImage = GeneratedImage(
                    id = UUID.randomUUID().toString(),
                    imagePath = result.getOrNull() ?: "",
                    thumbnailPath = result.getOrNull() ?: "",
                    config = GenerationConfig(prompt = "Upscaled image"),
                    timestamp = System.currentTimeMillis(),
                    generationTimeMs = 0,
                    isUpscaled = true
                )
                _generatedImages.update { it + upscaledImage }
            } else {
                _uiState.update { 
                    it.copy(
                        isProcessing = false, 
                        error = "Upscaling failed: ${result.exceptionOrNull()?.message}"
                    ) 
                }
            }
            _exportProgress.value = null
        }
    }
    
    fun generateBatch(batchConfig: BatchConfig) {
        if (!generationEngine.isReady()) {
            _uiState.update { it.copy(error = "AI engine not ready. Please wait for initialization to complete.") }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isGenerating = true, error = null) }
            _batchConfig.value = batchConfig
            
            generationEngine.generateBatch(batchConfig).collect { progress ->
                _batchProgress.value = progress
                
                if (progress.isComplete && progress.error == null) {
                    _uiState.update { it.copy(isGenerating = false) }
                    _batchProgress.value = null
                    _batchConfig.value = null
                    
                    // In real implementation, would add all batch generated images to gallery
                    // For now, simulate by adding multiple generated images
                    val batchImages = (1..progress.totalImages).map { index ->
                        GeneratedImage(
                            id = UUID.randomUUID().toString(),
                            imagePath = "${getApplication<Application>().filesDir}/generated_images/batch_${System.currentTimeMillis()}_$index.jpg",
                            thumbnailPath = "${getApplication<Application>().filesDir}/generated_images/batch_thumb_${System.currentTimeMillis()}_$index.jpg",
                            config = batchConfig.baseConfig,
                            timestamp = System.currentTimeMillis(),
                            generationTimeMs = 0
                        )
                    }
                    _generatedImages.update { it + batchImages }
                    
                } else if (progress.error != null) {
                    _uiState.update { it.copy(isGenerating = false, error = progress.error) }
                    _batchProgress.value = null
                    _batchConfig.value = null
                }
            }
        }
    }
    
    fun exportImage(imagePath: String, outputPath: String, exportConfig: ExportConfig) {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true) }
            _exportProgress.value = "Exporting as ${exportConfig.format.displayName}..."
            
            val result = generationEngine.exportImage(imagePath, outputPath, exportConfig)
            if (result.isSuccess) {
                _exportProgress.value = "Export complete!"
                _uiState.update { it.copy(isProcessing = false) }
            } else {
                _uiState.update { 
                    it.copy(
                        isProcessing = false, 
                        error = "Export failed: ${result.exceptionOrNull()?.message}"
                    ) 
                }
            }
            _exportProgress.value = null
        }
    }
    
    fun exportImagesAsZip(imagePaths: List<String>, outputPath: String, exportConfig: ExportConfig) {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true) }
            _exportProgress.value = "Creating ZIP archive..."
            
            val result = generationEngine.exportImagesAsZip(imagePaths, outputPath, exportConfig)
            if (result.isSuccess) {
                _exportProgress.value = "ZIP archive created!"
                _uiState.update { it.copy(isProcessing = false) }
            } else {
                _uiState.update { 
                    it.copy(
                        isProcessing = false, 
                        error = "ZIP export failed: ${result.exceptionOrNull()?.message}"
                    ) 
                }
            }
            _exportProgress.value = null
        }
    }
    
    fun updateBatchConfig(batchConfig: BatchConfig) {
        _batchConfig.value = batchConfig
    }
    
    fun cancelBatchGeneration() {
        viewModelScope.launch {
            generationEngine.cancelGeneration()
            _uiState.update { it.copy(isGenerating = false) }
            _batchProgress.value = null
            _batchConfig.value = null
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            generationEngine.release()
        }
    }
}

/**
 * Enhanced UI state for the generation screen with AI integration
 */
data class GenerationUiState(
    val prompt: String = "",
    val negativePrompt: String = "",
    val config: GenerationConfig = GenerationConfig(prompt = ""),
    val isGenerating: Boolean = false,
    val isInitializing: Boolean = false,
    val isProcessing: Boolean = false,
    val error: String? = null
)