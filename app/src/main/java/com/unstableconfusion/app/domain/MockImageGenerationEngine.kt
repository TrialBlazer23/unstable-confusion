package com.unstableconfusion.app.domain

import com.unstableconfusion.app.data.models.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.*

/**
 * Mock implementation of ImageGenerationEngine for development and testing
 * This will be replaced with actual QNN/Genie pipeline implementation
 */
class MockImageGenerationEngine : ImageGenerationEngine {
    
    private var isInitialized = false
    private var isGenerating = false
    private var shouldCancel = false
    
    override suspend fun initialize(): Result<Unit> {
        return try {
            // Simulate initialization time
            delay(2000)
            isInitialized = true
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override fun isReady(): Boolean = isInitialized
    
    override fun generateImage(config: GenerationConfig): Flow<GenerationProgress> = flow {
        if (!isReady()) {
            emit(GenerationProgress(0, 0, error = "Engine not initialized"))
            return@flow
        }
        
        isGenerating = true
        shouldCancel = false
        
        try {
            val totalSteps = config.steps
            
            // Emit initial progress
            emit(GenerationProgress(0, totalSteps, "Starting generation..."))
            
            // Simulate generation steps
            for (step in 1..totalSteps) {
                if (shouldCancel) {
                    emit(GenerationProgress(step, totalSteps, "Cancelled", error = "Generation cancelled"))
                    return@flow
                }
                
                delay(100) // Simulate processing time per step
                
                val message = when {
                    step < totalSteps * 0.2 -> "Encoding prompt..."
                    step < totalSteps * 0.8 -> "Generating image..."
                    else -> "Finalizing..."
                }
                
                emit(GenerationProgress(step, totalSteps, message))
            }
            
            // Simulate final processing
            delay(500)
            emit(GenerationProgress(totalSteps, totalSteps, "Generation complete!", isComplete = true))
            
        } catch (e: Exception) {
            emit(GenerationProgress(0, 0, error = "Generation failed: ${e.message}"))
        } finally {
            isGenerating = false
        }
    }
    
    override suspend fun upscaleImage(imagePath: String, scaleFactor: Int): Result<String> {
        return try {
            delay(3000) // Simulate upscaling time
            Result.success("${imagePath}_upscaled_${scaleFactor}x.jpg")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun cancelGeneration() {
        shouldCancel = true
    }
    
    override fun getAvailableSchedulers(): List<Scheduler> {
        return Scheduler.values().toList()
    }
    
    override fun getMemoryInfo(): MemoryInfo {
        val runtime = Runtime.getRuntime()
        val totalMemory = runtime.totalMemory() / (1024 * 1024) // Convert to MB
        val freeMemory = runtime.freeMemory() / (1024 * 1024)
        val usedMemory = totalMemory - freeMemory
        val maxMemory = runtime.maxMemory() / (1024 * 1024)
        
        return MemoryInfo(
            usedMemoryMB = usedMemory,
            totalMemoryMB = maxMemory,
            availableMemoryMB = maxMemory - usedMemory
        )
    }
    
    override suspend fun release() {
        isInitialized = false
        isGenerating = false
        shouldCancel = false
    }
}