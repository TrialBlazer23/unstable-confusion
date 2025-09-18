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

/**
 * Batch generation configuration
 */
data class BatchConfig(
    val baseConfig: GenerationConfig,
    val variations: List<BatchVariation> = emptyList(),
    val seedMode: BatchSeedMode = BatchSeedMode.RANDOM,
    val outputFormat: BatchOutputFormat = BatchOutputFormat.INDIVIDUAL_FILES
)

/**
 * Batch variation options
 */
data class BatchVariation(
    val type: BatchVariationType,
    val values: List<String>
)

/**
 * Types of batch variations
 */
enum class BatchVariationType(val displayName: String) {
    PROMPT_VARIATIONS("Prompt Variations"),
    STYLE_PRESETS("Style Presets"),
    CFG_SCALE_RANGE("CFG Scale Range"),
    STEPS_RANGE("Steps Range"),
    SCHEDULER_ALL("All Schedulers")
}

/**
 * Batch seed generation modes
 */
enum class BatchSeedMode(val displayName: String) {
    RANDOM("Random Seeds"),
    SEQUENTIAL("Sequential Seeds"),
    FIXED("Fixed Seed")
}

/**
 * Batch output formats
 */
enum class BatchOutputFormat(val displayName: String) {
    INDIVIDUAL_FILES("Individual Files"),
    ZIP_ARCHIVE("ZIP Archive"),
    GALLERY_COLLECTION("Gallery Collection")
}

/**
 * Batch generation progress
 */
data class BatchProgress(
    val totalImages: Int,
    val completedImages: Int,
    val currentImageProgress: GenerationProgress?,
    val isComplete: Boolean = false,
    val error: String? = null
) {
    val overallProgress: Float
        get() = if (totalImages > 0) {
            val baseProgress = completedImages.toFloat() / totalImages
            val currentProgress = currentImageProgress?.progressPercentage ?: 0f
            baseProgress + (currentProgress / totalImages)
        } else 0f
}

/**
 * Export configuration
 */
data class ExportConfig(
    val format: ExportFormat = ExportFormat.JPEG,
    val quality: Int = 90,
    val includeMetadata: Boolean = true,
    val watermark: String? = null
)

/**
 * Export formats
 */
enum class ExportFormat(val displayName: String, val extension: String) {
    JPEG("JPEG", "jpg"),
    PNG("PNG", "png"),
    WEBP("WebP", "webp")
}

/**
 * Upscaling configuration
 */
data class UpscaleConfig(
    val algorithm: UpscaleAlgorithm = UpscaleAlgorithm.REAL_ESRGAN,
    val scaleFactor: Int = 2,
    val preserveMetadata: Boolean = true
)

/**
 * Upscaling algorithms
 */
enum class UpscaleAlgorithm(val displayName: String) {
    REAL_ESRGAN("Real-ESRGAN"),
    ESRGAN("ESRGAN"),
    WAIFU2X("Waifu2x"),
    BICUBIC("Bicubic"),
    LANCZOS("Lanczos")
}