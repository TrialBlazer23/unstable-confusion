package com.unstableconfusion.app.data.models

/**
 * Configuration for image generation parameters
 */
data class GenerationConfig(
    val prompt: String,
    val negativePrompt: String = "",
    val width: Int = 512,
    val height: Int = 512,
    val steps: Int = 20,
    val cfgScale: Float = 7.5f,
    val seed: Long? = null,
    val scheduler: Scheduler = Scheduler.DDIM,
    val batchSize: Int = 1,
    val stylePreset: StylePreset? = null,
    val loraModels: List<LoraModel> = emptyList(),
    val referenceImage: String? = null,
    val styleStrength: Float = 0.7f
)

/**
 * Available schedulers for diffusion process
 */
enum class Scheduler(val displayName: String) {
    DDIM("DDIM"),
    EULER("Euler"),
    EULER_ANCESTRAL("Euler Ancestral"),
    DPM_SOLVER("DPM Solver"),
    PNDM("PNDM")
}

/**
 * Style presets for quick application
 */
data class StylePreset(
    val id: String,
    val name: String,
    val description: String,
    val promptAddition: String,
    val negativePromptAddition: String = "",
    val cfgScale: Float = 7.5f,
    val steps: Int = 20,
    val thumbnailPath: String? = null
)

/**
 * LoRA model configuration
 */
data class LoraModel(
    val id: String,
    val name: String,
    val path: String,
    val strength: Float = 1.0f,
    val enabled: Boolean = true,
    val description: String = "",
    val tags: List<String> = emptyList()
)

/**
 * Generated image result
 */
data class GeneratedImage(
    val id: String,
    val imagePath: String,
    val thumbnailPath: String,
    val config: GenerationConfig,
    val timestamp: Long,
    val generationTimeMs: Long,
    val isUpscaled: Boolean = false,
    val originalImageId: String? = null
)

/**
 * Generation progress state
 */
data class GenerationProgress(
    val currentStep: Int,
    val totalSteps: Int,
    val message: String = "",
    val isComplete: Boolean = false,
    val error: String? = null
) {
    val progressPercentage: Float
        get() = if (totalSteps > 0) currentStep.toFloat() / totalSteps else 0f
}