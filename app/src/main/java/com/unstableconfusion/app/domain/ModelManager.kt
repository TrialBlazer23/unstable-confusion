package com.unstableconfusion.app.domain

import android.content.Context
import com.unstableconfusion.app.data.models.LoraModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File

/**
 * Model download and management progress
 */
data class ModelDownloadProgress(
    val modelName: String,
    val bytesDownloaded: Long,
    val totalBytes: Long,
    val isComplete: Boolean = false,
    val error: String? = null
) {
    val progressPercentage: Float
        get() = if (totalBytes > 0) bytesDownloaded.toFloat() / totalBytes else 0f
}

/**
 * Available models for download
 */
data class AvailableModel(
    val id: String,
    val name: String,
    val description: String,
    val type: ModelType,
    val sizeBytes: Long,
    val downloadUrl: String,
    val version: String = "1.0"
)

enum class ModelType {
    BASE_MODEL,
    LORA,
    UPSCALER,
    VAE
}

/**
 * Manages AI model downloads, caching, and loading for the QNN/Genie pipeline
 */
class ModelManager(private val context: Context) {
    
    private val modelsDir = File(context.filesDir, "models")
    private val baseModelsDir = File(modelsDir, "base")
    private val loraModelsDir = File(modelsDir, "lora")
    private val upscalerModelsDir = File(modelsDir, "upscaler")
    private val vaeModelsDir = File(modelsDir, "vae")
    
    init {
        createDirectories()
    }
    
    /**
     * Get list of available models for download
     */
    fun getAvailableModels(): List<AvailableModel> {
        return listOf(
            AvailableModel(
                id = "stable_diffusion_v1_5",
                name = "Stable Diffusion v1.5",
                description = "Base text-to-image model optimized for mobile",
                type = ModelType.BASE_MODEL,
                sizeBytes = 1_500_000_000L, // ~1.5GB
                downloadUrl = "https://example.com/models/sd_v1_5_mobile.bin"
            ),
            AvailableModel(
                id = "detail_enhancer_lora",
                name = "Detail Enhancer LoRA",
                description = "Enhances fine details in generated images",
                type = ModelType.LORA,
                sizeBytes = 150_000_000L, // ~150MB
                downloadUrl = "https://example.com/models/detail_enhancer.safetensors"
            ),
            AvailableModel(
                id = "anime_style_lora",
                name = "Anime Style LoRA",
                description = "Anime and manga style enhancement",
                type = ModelType.LORA,
                sizeBytes = 120_000_000L, // ~120MB
                downloadUrl = "https://example.com/models/anime_style.safetensors"
            ),
            AvailableModel(
                id = "real_esrgan_upscaler",
                name = "Real-ESRGAN Upscaler",
                description = "High-quality image upscaling model",
                type = ModelType.UPSCALER,
                sizeBytes = 800_000_000L, // ~800MB
                downloadUrl = "https://example.com/models/real_esrgan_mobile.bin"
            )
        )
    }
    
    /**
     * Check if a model is already downloaded
     */
    fun isModelDownloaded(modelId: String): Boolean {
        val model = getAvailableModels().find { it.id == modelId } ?: return false
        val modelFile = getModelFile(model)
        return modelFile.exists() && modelFile.length() > 0
    }
    
    /**
     * Get downloaded models
     */
    fun getDownloadedModels(): List<AvailableModel> {
        return getAvailableModels().filter { isModelDownloaded(it.id) }
    }
    
    /**
     * Download a model with progress tracking
     */
    fun downloadModel(modelId: String): Flow<ModelDownloadProgress> = flow {
        val model = getAvailableModels().find { it.id == modelId }
            ?: throw IllegalArgumentException("Model not found: $modelId")
        
        if (isModelDownloaded(modelId)) {
            emit(ModelDownloadProgress(model.name, model.sizeBytes, model.sizeBytes, true))
            return@flow
        }
        
        val modelFile = getModelFile(model)
        
        try {
            // Simulate download progress
            // In real implementation, this would use HTTP client to download from model.downloadUrl
            val chunkSize = model.sizeBytes / 20 // 20 progress updates
            var downloaded = 0L
            
            emit(ModelDownloadProgress(model.name, 0, model.sizeBytes))
            
            for (i in 1..20) {
                kotlinx.coroutines.delay(500) // Simulate download time
                downloaded += chunkSize
                if (downloaded > model.sizeBytes) downloaded = model.sizeBytes
                
                emit(ModelDownloadProgress(model.name, downloaded, model.sizeBytes))
            }
            
            // Create placeholder file to represent downloaded model
            modelFile.parentFile?.mkdirs()
            modelFile.writeText("Model data placeholder for ${model.name}")
            
            emit(ModelDownloadProgress(model.name, model.sizeBytes, model.sizeBytes, true))
            
        } catch (e: Exception) {
            emit(ModelDownloadProgress(model.name, 0, model.sizeBytes, false, e.message))
        }
    }
    
    /**
     * Delete a downloaded model
     */
    fun deleteModel(modelId: String): Boolean {
        val model = getAvailableModels().find { it.id == modelId } ?: return false
        val modelFile = getModelFile(model)
        return if (modelFile.exists()) {
            modelFile.delete()
        } else {
            false
        }
    }
    
    /**
     * Get available LoRA models from downloads
     */
    fun getDownloadedLoraModels(): List<LoraModel> {
        return getDownloadedModels()
            .filter { it.type == ModelType.LORA }
            .map { model ->
                LoraModel(
                    id = model.id,
                    name = model.name,
                    path = getModelFile(model).absolutePath,
                    description = model.description,
                    strength = 1.0f,
                    enabled = false
                )
            }
    }
    
    /**
     * Get total storage used by models
     */
    fun getTotalModelStorageMB(): Long {
        return getDownloadedModels().sumOf { it.sizeBytes } / (1024 * 1024)
    }
    
    /**
     * Get available storage space
     */
    fun getAvailableStorageMB(): Long {
        return modelsDir.usableSpace / (1024 * 1024)
    }
    
    /**
     * Clear all model cache
     */
    fun clearAllModels(): Boolean {
        return try {
            modelsDir.deleteRecursively()
            createDirectories()
            true
        } catch (e: Exception) {
            false
        }
    }
    
    private fun createDirectories() {
        baseModelsDir.mkdirs()
        loraModelsDir.mkdirs()
        upscalerModelsDir.mkdirs()
        vaeModelsDir.mkdirs()
    }
    
    private fun getModelFile(model: AvailableModel): File {
        val dir = when (model.type) {
            ModelType.BASE_MODEL -> baseModelsDir
            ModelType.LORA -> loraModelsDir
            ModelType.UPSCALER -> upscalerModelsDir
            ModelType.VAE -> vaeModelsDir
        }
        
        val extension = when (model.type) {
            ModelType.BASE_MODEL, ModelType.UPSCALER -> ".bin"
            ModelType.LORA, ModelType.VAE -> ".safetensors"
        }
        
        return File(dir, "${model.id}${extension}")
    }
}