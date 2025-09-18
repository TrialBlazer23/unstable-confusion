package com.unstableconfusion.app.domain

import com.unstableconfusion.app.data.models.*
import kotlinx.coroutines.flow.Flow

/**
 * Interface for the AI image generation engine
 * This will be implemented to interface with QNN/Genie pipeline
 */
interface ImageGenerationEngine {
    
    /**
     * Initialize the generation engine
     */
    suspend fun initialize(): Result<Unit>
    
    /**
     * Check if the engine is ready for generation
     */
    fun isReady(): Boolean
    
    /**
     * Generate images based on configuration
     * Returns a flow of generation progress updates
     */
    fun generateImage(config: GenerationConfig): Flow<GenerationProgress>
    
    /**
     * Generate batch of images
     * Returns a flow of batch generation progress updates
     */
    fun generateBatch(batchConfig: BatchConfig): Flow<BatchProgress>
    
    /**
     * Upscale an existing image with advanced configuration
     */
    suspend fun upscaleImage(imagePath: String, upscaleConfig: UpscaleConfig): Result<String>
    
    /**
     * Export image with specific configuration
     */
    suspend fun exportImage(imagePath: String, outputPath: String, exportConfig: ExportConfig): Result<String>
    
    /**
     * Export multiple images as ZIP archive
     */
    suspend fun exportImagesAsZip(imagePaths: List<String>, outputPath: String, exportConfig: ExportConfig): Result<String>
    
    /**
     * Cancel ongoing generation
     */
    suspend fun cancelGeneration()
    
    /**
     * Get available schedulers
     */
    fun getAvailableSchedulers(): List<Scheduler>
    
    /**
     * Get memory usage information
     */
    fun getMemoryInfo(): MemoryInfo
    
    /**
     * Release resources
     */
    suspend fun release()
}

/**
 * Memory usage information
 */
data class MemoryInfo(
    val usedMemoryMB: Long,
    val totalMemoryMB: Long,
    val availableMemoryMB: Long
) {
    val usagePercentage: Float
        get() = if (totalMemoryMB > 0) usedMemoryMB.toFloat() / totalMemoryMB else 0f
}