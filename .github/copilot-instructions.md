# Unstable Confusion - Android Text-to-Image Studio

**ALWAYS follow these instructions first and fallback to additional search and context gathering only when the information here is incomplete or found to be in error.**

## Project Overview

Unstable Confusion is a fast, private, on-device text-to-image studio for Android that provides Midjourney-like creative control on mobile devices. All image generation happens locally using the QNN/Genie pipeline - no data ever leaves the device.

**Key Technologies:**
- Language: Kotlin
- UI Framework: Jetpack Compose with Material 3
- Architecture: MVVM with Repository pattern
- AI Engine: QNN/Genie pipeline for on-device inference
- Build System: Gradle with Kotlin DSL
- Target: Android API 26+ (covers 95%+ of devices)

## Working Effectively

### Prerequisites and Environment Setup
- **REQUIRED**: Android Studio Electric Eel (2022.1.1) or later
- **REQUIRED**: Android SDK API 26+ with target API 34  
- **REQUIRED**: Kotlin 1.9.10+
- **REQUIRED**: JVM version 17 or 21

### Build and Development Commands

#### **CRITICAL BUILD LIMITATIONS**
- **Build environment limitations**: Full builds require Android SDK and may fail in environments without proper Android development setup
- **Plugin resolution**: Android Gradle Plugin requires specific repository access that may not be available in all environments
- **Workaround**: Code analysis and inspection can be done without building, but testing requires Android environment

#### Core Build Commands (when Android SDK available):
```bash
# NEVER CANCEL: Initial build takes 2-5 minutes depending on dependencies. Set timeout to 10+ minutes.
./gradlew build

# NEVER CANCEL: Clean build takes 3-7 minutes. Set timeout to 15+ minutes.
./gradlew clean build

# NEVER CANCEL: Assembling APK takes 2-4 minutes. Set timeout to 10+ minutes.
./gradlew assembleDebug
```

#### Testing Commands:
```bash
# NEVER CANCEL: Unit tests take 30-90 seconds. Set timeout to 5+ minutes.
./gradlew test

# NEVER CANCEL: Connected tests take 2-5 minutes. Set timeout to 10+ minutes.
./gradlew connectedAndroidTest

# Quick test run for specific class (30-60 seconds)
./gradlew testDebugUnitTest --tests="com.unstableconfusion.app.ExampleUnitTest"
```

#### Development Tools:
```bash
# Check project structure and dependencies
./gradlew dependencies

# Lint check (1-2 minutes)
./gradlew lint

# Generate reports
./gradlew check
```

### Alternative Development Without Build
When Android SDK is not available, you can still work effectively:
- **Code Analysis**: Review and modify Kotlin source files directly
- **Architecture Review**: Examine MVVM patterns, data models, UI components
- **Documentation**: Update README, documentation files
- **Configuration**: Modify Gradle build files, manifest, resources

## Validation and Testing

### **Manual Validation Scenarios**
After making changes, always test these key user workflows:

1. **Basic Generation Flow**:
   - Launch app → Enter prompt → Adjust settings → Generate image
   - Verify: UI responsiveness, progress tracking, result display

2. **Creative Control Testing**:
   - Test prompt suggestions across categories (Style, Lighting, Composition)
   - Verify negative prompts integration
   - Test style presets application
   - Validate advanced parameter controls (steps, CFG scale, dimensions)

3. **Consistency Features**:
   - Test seed management (random generation, manual entry, reuse)
   - Verify gallery image metadata and seed replication
   - Test batch generation with different settings

4. **Performance Validation**:
   - Monitor memory usage during generation
   - Test async generation (non-blocking UI)
   - Verify generation cancellation works properly

### **Code Quality Validation**
Always run these before committing:
```bash
# REQUIRED: Lint check to ensure code quality
./gradlew lint

# REQUIRED: Run unit tests to verify functionality  
./gradlew test

# RECOMMENDED: Full check including tests and lint
./gradlew check
```

## Project Structure and Navigation

### Key Directories
```
app/src/main/java/com/unstableconfusion/app/
├── MainActivity.kt                    # Entry point
├── data/
│   ├── models/                        # Data classes and enums
│   │   ├── GenerationModels.kt        # Core generation configuration
│   │   └── PromptModels.kt           # Prompt suggestions and templates
│   └── repository/
│       └── AppRepository.kt          # Data access interface
├── domain/
│   ├── ImageGenerationEngine.kt      # AI generation interface
│   ├── MockImageGenerationEngine.kt  # Development implementation
│   ├── QnnGenieImageGenerationEngine.kt # Production implementation
│   └── ModelManager.kt              # LoRA and model management
└── ui/
    ├── components/                   # Reusable UI components
    ├── screens/                      # Main app screens
    ├── viewmodels/                   # MVVM ViewModels
    └── theme/                        # Material 3 theming
```

### Important Files to Know

#### **Core Data Models** (`app/src/main/java/com/unstableconfusion/app/data/models/`)
- `GenerationModels.kt`: Contains `GenerationConfig`, `Scheduler`, `StylePreset`, `LoraModel`
- `PromptModels.kt`: Contains `PromptSuggestion`, `PromptCategory`, `PromptTemplate`

#### **Key Components** (`app/src/main/java/com/unstableconfusion/app/ui/components/`)
- `PromptInputCard.kt`: Dual-input for positive/negative prompts
- `StylePresetsSection.kt`: Visual style selection interface  
- `PromptToolsSection.kt`: Categorized suggestion browser
- `AdvancedSettingsCard.kt`: Parameter controls (steps, CFG, dimensions)
- `GenerationProgressCard.kt`: Real-time generation monitoring

#### **Main Screens** (`app/src/main/java/com/unstableconfusion/app/ui/screens/`)
- `GenerateScreen.kt`: Primary interface with all creative controls
- `GalleryScreen.kt`: Image browsing with metadata and reuse options
- `SettingsScreen.kt`: App configuration and memory monitoring

### **Architecture Patterns**

#### MVVM Implementation
- **ViewModels**: Handle UI state and business logic
- **Repository**: Abstract data access layer
- **Models**: Immutable data classes with clear separation

#### State Management
- Use `StateFlow` and `collectAsStateWithLifecycle()` for reactive UI
- Prefer immutable data structures
- Handle loading, success, and error states consistently

#### Async Operations
- All generation operations use Kotlin Coroutines
- UI operations are non-blocking with progress feedback
- Use `Flow` for streaming progress updates

## Common Development Tasks

### Adding New Generation Parameters
1. Update `GenerationConfig` in `GenerationModels.kt`
2. Add UI controls in `AdvancedSettingsCard.kt`
3. Update `MockImageGenerationEngine.kt` to handle new parameter
4. Add corresponding tests in `ExampleUnitTest.kt`
5. **Always verify**: Parameter validation and default values

### Adding New Prompt Suggestions
1. Update `PromptCategory` enum if needed
2. Add suggestions to `MockAppRepository.getPromptSuggestions()`
3. Test display in `PromptToolsSection.kt`
4. **Always verify**: Category filtering works correctly

### Adding New Style Presets
1. Add preset to `MockAppRepository.getStylePresets()`
2. Include prompt additions, negative prompts, and optimal parameters
3. Test visual display in `StylePresetsSection.kt` 
4. **Always verify**: Style application affects generation config correctly

### UI Component Development
1. **Follow Material 3 guidelines**: Use established design tokens
2. **Responsive design**: Test on different screen sizes
3. **Accessibility**: Include content descriptions and semantic roles
4. **State management**: Use `remember` for local state, ViewModel for shared state

### Integration Points for QNN/Genie
The codebase is designed for seamless AI pipeline integration:

1. **Replace MockImageGenerationEngine**: Implement `ImageGenerationEngine` interface
2. **Model Loading**: Use `ModelManager.kt` for LoRA and base model management  
3. **Progress Updates**: Real-time generation progress via `Flow<GenerationProgress>`
4. **Memory Management**: Utilize existing `MemoryInfo` monitoring
5. **File Storage**: Image paths are ready for actual file operations

## Performance and Memory

### Memory Optimization
- **Monitor usage**: `MemoryInfo` provides real-time metrics
- **Image handling**: Use appropriate compression and caching
- **Model management**: Load/unload models based on memory constraints
- **Batch processing**: Limit concurrent generation based on available memory

### UI Performance
- **Lazy loading**: Use `LazyColumn` and `LazyRow` for lists
- **Composition optimization**: Minimize recomposition with stable data
- **Image rendering**: Use Coil for efficient image loading and caching

## Troubleshooting

### Common Build Issues
1. **Plugin resolution errors**: Ensure Android SDK is properly configured
2. **Dependency conflicts**: Check `app/build.gradle.kts` for version mismatches
3. **API level issues**: Target API 34, minimum API 26

### Development Without Android Studio
- **Code editing**: All Kotlin files can be modified directly
- **Architecture review**: Examine patterns and relationships
- **Documentation updates**: Improve README, code comments
- **Gradle configuration**: Modify build scripts and dependencies

### Memory Issues During Development
- **Generation settings**: Use lower resolution (256x256) for testing
- **Batch size**: Limit to 1 image during development
- **Model loading**: Monitor memory usage in Settings screen

## Quick Reference

### Default Generation Settings (Optimized for Development)
- **Dimensions**: 512x512 (good quality/speed balance)
- **Steps**: 20 (good quality, reasonable time)
- **CFG Scale**: 7.5 (balanced creativity/coherence)
- **Scheduler**: DDIM (stable, reliable)
- **Batch Size**: 1 (memory efficient)

### File Locations Summary
- **Tests**: `app/src/test/java/com/unstableconfusion/app/ExampleUnitTest.kt`
- **Resources**: `app/src/main/res/values/strings.xml`
- **Manifest**: `app/src/main/AndroidManifest.xml`
- **Build config**: `app/build.gradle.kts`
- **Theme**: `app/src/main/java/com/unstableconfusion/app/ui/theme/`

### Dependencies Overview
- **Compose BOM**: 2023.10.01 (UI framework)
- **Kotlin**: 1.9.10 (language)
- **Coroutines**: 1.7.3 (async operations)
- **Coil**: 2.5.0 (image loading)
- **Navigation**: 2.7.5 (screen navigation)

**Remember**: This is a production-ready application with comprehensive testing and a clear architecture. Make minimal, targeted changes and always validate with the existing test suite and user scenarios.