package com.unstableconfusion.app

import com.unstableconfusion.app.data.models.*
import com.unstableconfusion.app.data.repository.MockAppRepository
import com.unstableconfusion.app.domain.MockImageGenerationEngine
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }
    
    @Test
    fun testGenerationConfig_defaultValues() {
        val config = GenerationConfig(prompt = "test prompt")
        
        assertEquals("test prompt", config.prompt)
        assertEquals("", config.negativePrompt)
        assertEquals(512, config.width)
        assertEquals(512, config.height)
        assertEquals(20, config.steps)
        assertEquals(7.5f, config.cfgScale, 0.01f)
        assertEquals(Scheduler.DDIM, config.scheduler)
        assertEquals(1, config.batchSize)
        assertNull(config.seed)
    }
    
    @Test
    fun testMockRepository_saveAndRetrieveImages() = runTest {
        val repository = MockAppRepository()
        
        val config = GenerationConfig(prompt = "test image")
        val generatedImage = GeneratedImage(
            id = "test-id",
            imagePath = "/path/to/image.jpg",
            thumbnailPath = "/path/to/thumb.jpg",
            config = config,
            timestamp = System.currentTimeMillis(),
            generationTimeMs = 5000L
        )
        
        repository.saveGeneratedImage(generatedImage)
        
        val images = repository.getGeneratedImages()
        assertEquals(1, images.size)
        assertEquals("test-id", images[0].id)
        assertEquals("test image", images[0].config.prompt)
    }
    
    @Test
    fun testMockRepository_stylePresets() = runTest {
        val repository = MockAppRepository()
        val presets = repository.getStylePresets()
        
        assertTrue("Should have default style presets", presets.isNotEmpty())
        assertTrue("Should contain photorealistic preset", 
            presets.any { it.name == "Photorealistic" })
    }
    
    @Test
    fun testMockRepository_promptSuggestions() = runTest {
        val repository = MockAppRepository()
        val suggestions = repository.getPromptSuggestions()
        
        assertTrue("Should have prompt suggestions", suggestions.isNotEmpty())
        
        val styleSuggestions = repository.getPromptSuggestions(PromptCategory.STYLE)
        assertTrue("Should have style suggestions", styleSuggestions.isNotEmpty())
        assertTrue("All suggestions should be style category",
            styleSuggestions.all { it.category == PromptCategory.STYLE })
    }
    
    @Test
    fun testMockGenerationEngine_initialization() = runTest {
        val engine = MockImageGenerationEngine()
        
        assertFalse("Should not be ready initially", engine.isReady())
        
        val result = engine.initialize()
        assertTrue("Initialization should succeed", result.isSuccess)
        assertTrue("Should be ready after initialization", engine.isReady())
    }
    
    @Test
    fun testMockGenerationEngine_availableSchedulers() {
        val engine = MockImageGenerationEngine()
        val schedulers = engine.getAvailableSchedulers()
        
        assertEquals("Should have all scheduler types", 
            Scheduler.values().size, schedulers.size)
        assertTrue("Should contain DDIM", schedulers.contains(Scheduler.DDIM))
        assertTrue("Should contain Euler", schedulers.contains(Scheduler.EULER))
    }
    
    @Test
    fun testMockGenerationEngine_memoryInfo() {
        val engine = MockImageGenerationEngine()
        val memoryInfo = engine.getMemoryInfo()
        
        assertTrue("Used memory should be positive", memoryInfo.usedMemoryMB >= 0)
        assertTrue("Total memory should be positive", memoryInfo.totalMemoryMB > 0)
        assertTrue("Available memory should be positive", memoryInfo.availableMemoryMB >= 0)
        assertTrue("Usage percentage should be valid", 
            memoryInfo.usagePercentage >= 0f && memoryInfo.usagePercentage <= 1f)
    }
    
    // Phase 2 QNN/Genie Engine Tests
    
    @Test
    fun testQnnGenieEngine_dataCompatibility() {
        // Test that our data structures are compatible with the real engine
        val config = GenerationConfig(prompt = "test prompt")
        
        // Verify configuration is compatible with real engine requirements
        assertNotNull("Prompt should not be null", config.prompt)
        assertTrue("Steps should be positive", config.steps > 0)
        assertTrue("CFG scale should be positive", config.cfgScale > 0)
        assertTrue("Width should be positive", config.width > 0)
        assertTrue("Height should be positive", config.height > 0)
    }
    
    @Test
    fun testLoraModel_dataStructure() {
        val lora = LoraModel(
            id = "test-lora",
            name = "Test LoRA",
            path = "/path/to/lora.safetensors",
            strength = 0.8f,
            enabled = true,
            description = "Test LoRA model"
        )
        
        assertEquals("test-lora", lora.id)
        assertEquals("Test LoRA", lora.name)
        assertEquals(0.8f, lora.strength, 0.01f)
        assertTrue("Should be enabled", lora.enabled)
        assertNotNull("Path should not be null", lora.path)
    }
    
    @Test
    fun testGenerationProgress_calculation() {
        val progress = GenerationProgress(
            currentStep = 15,
            totalSteps = 20,
            message = "Generating..."
        )
        
        assertEquals(0.75f, progress.progressPercentage, 0.01f)
        assertFalse("Should not be complete", progress.isComplete)
        assertNull("Should not have error", progress.error)
    }
    
    @Test
    fun testMemoryInfo_calculation() {
        val memoryInfo = MemoryInfo(
            usedMemoryMB = 800L,
            totalMemoryMB = 1000L,
            availableMemoryMB = 200L
        )
        
        assertEquals(0.8f, memoryInfo.usagePercentage, 0.01f)
        assertEquals(800L, memoryInfo.usedMemoryMB)
        assertEquals(1000L, memoryInfo.totalMemoryMB)
        assertEquals(200L, memoryInfo.availableMemoryMB)
    }
    
    @Test
    fun testAvailableModel_types() {
        val baseModel = com.unstableconfusion.app.domain.AvailableModel(
            id = "sd_v1_5",
            name = "Stable Diffusion v1.5",
            description = "Base model",
            type = com.unstableconfusion.app.domain.ModelType.BASE_MODEL,
            sizeBytes = 1500000000L,
            downloadUrl = "https://example.com/model.bin"
        )
        
        assertEquals(com.unstableconfusion.app.domain.ModelType.BASE_MODEL, baseModel.type)
        assertEquals("sd_v1_5", baseModel.id)
        assertTrue("Size should be positive", baseModel.sizeBytes > 0)
        
        val loraModel = com.unstableconfusion.app.domain.AvailableModel(
            id = "detail_lora",
            name = "Detail LoRA",
            description = "Detail enhancement",
            type = com.unstableconfusion.app.domain.ModelType.LORA,
            sizeBytes = 150000000L,
            downloadUrl = "https://example.com/lora.safetensors"
        )
        
        assertEquals(com.unstableconfusion.app.domain.ModelType.LORA, loraModel.type)
        assertEquals("detail_lora", loraModel.id)
    }
    
    @Test
    fun testModelDownloadProgress_calculation() {
        val progress = com.unstableconfusion.app.domain.ModelDownloadProgress(
            modelName = "Test Model",
            bytesDownloaded = 75000000L,
            totalBytes = 100000000L
        )
        
        assertEquals(0.75f, progress.progressPercentage, 0.01f)
        assertEquals("Test Model", progress.modelName)
        assertFalse("Should not be complete", progress.isComplete)
        assertNull("Should not have error", progress.error)
    }
    
    // Phase 3 Advanced Features Tests
    
    @Test
    fun testBatchConfig_creation() {
        val baseConfig = GenerationConfig(prompt = "test prompt")
        val variations = listOf(
            BatchVariation(
                type = BatchVariationType.PROMPT_VARIATIONS,
                values = listOf("style1", "style2", "style3")
            ),
            BatchVariation(
                type = BatchVariationType.CFG_SCALE_RANGE,
                values = listOf("7.0", "7.5", "8.0")
            )
        )
        
        val batchConfig = BatchConfig(
            baseConfig = baseConfig,
            variations = variations,
            seedMode = BatchSeedMode.RANDOM,
            outputFormat = BatchOutputFormat.INDIVIDUAL_FILES
        )
        
        assertEquals(baseConfig, batchConfig.baseConfig)
        assertEquals(2, batchConfig.variations.size)
        assertEquals(BatchSeedMode.RANDOM, batchConfig.seedMode)
        assertEquals(BatchOutputFormat.INDIVIDUAL_FILES, batchConfig.outputFormat)
    }
    
    @Test
    fun testBatchProgress_calculation() {
        val currentImageProgress = GenerationProgress(
            currentStep = 10,
            totalSteps = 20,
            message = "Denoising..."
        )
        
        val batchProgress = BatchProgress(
            totalImages = 4,
            completedImages = 2,
            currentImageProgress = currentImageProgress
        )
        
        // Should be 50% from completed images + 25% from current image progress (50% * 1/4)
        val expectedProgress = 2f / 4f + (0.5f / 4f)
        assertEquals(expectedProgress, batchProgress.overallProgress, 0.01f)
        assertEquals(4, batchProgress.totalImages)
        assertEquals(2, batchProgress.completedImages)
        assertFalse("Should not be complete", batchProgress.isComplete)
        assertNull("Should not have error", batchProgress.error)
    }
    
    @Test
    fun testExportConfig_defaultValues() {
        val exportConfig = ExportConfig()
        
        assertEquals(ExportFormat.JPEG, exportConfig.format)
        assertEquals(90, exportConfig.quality)
        assertTrue("Should include metadata by default", exportConfig.includeMetadata)
        assertNull("Should not have watermark by default", exportConfig.watermark)
    }
    
    @Test
    fun testUpscaleConfig_defaultValues() {
        val upscaleConfig = UpscaleConfig()
        
        assertEquals(UpscaleAlgorithm.REAL_ESRGAN, upscaleConfig.algorithm)
        assertEquals(2, upscaleConfig.scaleFactor)
        assertTrue("Should preserve metadata by default", upscaleConfig.preserveMetadata)
    }
    
    @Test
    fun testBatchVariationType_allTypes() {
        val types = BatchVariationType.values()
        
        assertTrue("Should have prompt variations", types.contains(BatchVariationType.PROMPT_VARIATIONS))
        assertTrue("Should have style presets", types.contains(BatchVariationType.STYLE_PRESETS))
        assertTrue("Should have CFG scale range", types.contains(BatchVariationType.CFG_SCALE_RANGE))
        assertTrue("Should have steps range", types.contains(BatchVariationType.STEPS_RANGE))
        assertTrue("Should have scheduler all", types.contains(BatchVariationType.SCHEDULER_ALL))
        
        // Test display names
        assertEquals("Prompt Variations", BatchVariationType.PROMPT_VARIATIONS.displayName)
        assertEquals("Style Presets", BatchVariationType.STYLE_PRESETS.displayName)
        assertEquals("CFG Scale Range", BatchVariationType.CFG_SCALE_RANGE.displayName)
    }
    
    @Test
    fun testExportFormat_extensions() {
        assertEquals("jpg", ExportFormat.JPEG.extension)
        assertEquals("png", ExportFormat.PNG.extension)
        assertEquals("webp", ExportFormat.WEBP.extension)
        
        assertEquals("JPEG", ExportFormat.JPEG.displayName)
        assertEquals("PNG", ExportFormat.PNG.displayName)
        assertEquals("WebP", ExportFormat.WEBP.displayName)
    }
    
    @Test
    fun testMockGenerationEngine_batchGeneration() = runTest {
        val engine = MockImageGenerationEngine()
        engine.initialize()
        
        val baseConfig = GenerationConfig(prompt = "test prompt")
        val batchConfig = BatchConfig(
            baseConfig = baseConfig,
            variations = listOf(
                BatchVariation(
                    type = BatchVariationType.PROMPT_VARIATIONS,
                    values = listOf("variation1", "variation2")
                )
            ),
            seedMode = BatchSeedMode.RANDOM
        )
        
        val progressList = mutableListOf<BatchProgress>()
        engine.generateBatch(batchConfig).collect { progress ->
            progressList.add(progress)
        }
        
        assertTrue("Should have progress updates", progressList.isNotEmpty())
        val finalProgress = progressList.last()
        assertTrue("Final progress should be complete", finalProgress.isComplete)
        assertNull("Should not have error", finalProgress.error)
    }
    
    @Test 
    fun testMockGenerationEngine_upscaling() = runTest {
        val engine = MockImageGenerationEngine()
        engine.initialize()
        
        val upscaleConfig = UpscaleConfig(
            algorithm = UpscaleAlgorithm.REAL_ESRGAN,
            scaleFactor = 4
        )
        
        val result = engine.upscaleImage("/test/path.jpg", upscaleConfig)
        
        assertTrue("Upscaling should succeed", result.isSuccess)
        val outputPath = result.getOrNull()
        assertNotNull("Should have output path", outputPath)
        assertTrue("Output path should contain scale factor", 
            outputPath!!.contains("4x"))
        assertTrue("Output path should contain algorithm", 
            outputPath.contains("real_esrgan"))
    }
    
    @Test
    fun testMockGenerationEngine_export() = runTest {
        val engine = MockImageGenerationEngine()
        engine.initialize()
        
        val exportConfig = ExportConfig(
            format = ExportFormat.PNG,
            quality = 95
        )
        
        val result = engine.exportImage("/test/input.jpg", "/test/output", exportConfig)
        
        assertTrue("Export should succeed", result.isSuccess)
        val outputPath = result.getOrNull()
        assertNotNull("Should have output path", outputPath)
        assertTrue("Output path should have PNG extension", 
            outputPath!!.endsWith(".png"))
    }
}