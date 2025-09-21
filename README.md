# Unstable Confusion

A fast, private, on-device text-to-image studio for Android that brings Midjourney-like creative control to your mobile device. All image generation happens locally on your device using the QNN/Genie pipeline - no data ever leaves your phone.

## Features

### 🎨 Creative Control
- **Prompt Crafting Tools**: Interactive prompt suggestions organized by categories (Style, Lighting, Composition, etc.)
- **Negative Prompts**: Fine-tune what you don't want in your generated images
- **Advanced Parameters**: Full control over steps, CFG scale, dimensions, batch size
- **Multiple Schedulers**: Choose from DDIM, Euler, Euler Ancestral, DPM Solver, and PNDM

### 🔄 Consistency Tools
- **Seed Management**: Lock seeds for reproducible results or generate random ones
- **Prompt Templates**: Pre-built templates for common image types (portraits, landscapes, fantasy)
- **Style Presets**: Quick application of artistic styles (Photorealistic, Anime, Cinematic, etc.)
- **LoRA Model Support**: Load and stack LoRA models for enhanced control

### ⚡ Speed & UX
- **Smart Defaults**: Optimized settings that work out of the box
- **Async Generation**: Non-blocking image generation with real-time progress
- **Mobile-Optimized UI**: Clean, intuitive interface built with Jetpack Compose
- **Batch Workflows**: Generate multiple variations efficiently

### 🚀 Advanced Features
- **Batch Processing**: Generate multiple images with variations (prompt changes, style presets, parameter ranges)
- **Advanced Upscaling**: Multiple algorithms (Real-ESRGAN, ESRGAN, Waifu2x) with customizable scale factors
- **Export & Sharing**: Multi-format export (JPEG, PNG, WebP) with quality control and ZIP archives
- **Multi-Selection**: Select multiple images in gallery for batch operations

### 🔒 Privacy & Efficiency
- **100% On-Device**: All processing happens locally using QNN/Genie pipeline
- **Memory Optimization**: Efficient memory management for mobile devices
- **No Internet Required**: Complete functionality works offline
- **Upscaling Support**: Built-in image upscaling capabilities

## Architecture

### Tech Stack
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose with Material 3
- **Architecture**: MVVM with Repository pattern
- **AI Engine**: QNN/Genie pipeline for on-device inference
- **Image Processing**: Native Android APIs with custom upscaling

### Key Components

#### Data Layer
- `GenerationModels.kt`: Core data models for generation configuration
- `PromptModels.kt`: Models for prompt suggestions and templates
- `AppRepository.kt`: Repository interface with mock implementation

#### Domain Layer
- `ImageGenerationEngine.kt`: Interface for AI image generation
- `MockImageGenerationEngine.kt`: Development implementation (to be replaced with QNN/Genie)

#### UI Layer
- `GenerationViewModel.kt`: Main ViewModel managing generation state
- Compose screens: Generation, Gallery, Settings
- Reusable UI components for prompts, styles, and controls

## Getting Started

### Prerequisites
- Android Studio Electric Eel (2022.1.1) or later
- Android SDK API 26+ (target API 34)
- Kotlin 1.9.10+

### Installation
1. Clone the repository
2. Open in Android Studio
3. Sync Gradle dependencies
4. Build and run on device or emulator

### Build Verification
To verify the project builds correctly in your environment, run the provided verification script:

```bash
./verify_build.sh
```

Or manually verify with:
```bash
# Clean and build
./gradlew clean build

# Run tests
./gradlew test

# Generate debug APK
./gradlew assembleDebug
```

**Note**: The project requires internet access to download Android Gradle Plugin and dependencies from Google's repositories during the first build.

### Usage
1. **Enter Prompt**: Type your image description in the main prompt field
2. **Refine with Tools**: Use prompt suggestions and negative prompts
3. **Choose Style**: Select from preset styles or create custom ones
4. **Adjust Settings**: Fine-tune generation parameters in advanced settings
5. **Generate**: Tap generate and watch real-time progress
6. **Review Results**: View generated images in the gallery with full metadata

#### Advanced Workflows
- **Batch Generation**: Use the batch icon to configure multiple variations with different prompts, styles, or parameters
- **Export & Share**: Select images in gallery and use export options for different formats and quality settings
- **Upscaling**: Choose from multiple upscaling algorithms and scale factors for enhanced image quality
- **Seed Management**: Lock seeds for reproducible results or explore variations with different seed modes

## Development Roadmap

### Phase 1: Core Implementation ✅
- [x] Android project structure with Compose UI
- [x] Data models and repository pattern
- [x] Mock image generation engine
- [x] Full UI implementation with all screens
- [x] Prompt crafting tools and suggestions
- [x] Style presets and advanced settings

### Phase 2: AI Integration ✅
- [x] Integrate QNN/Genie pipeline
- [x] Implement actual image generation
- [x] Add LoRA model loading
- [x] Performance optimization for mobile

### Phase 3: Advanced Features ✅
- [x] Batch processing workflows
- [x] Advanced upscaling algorithms  
- [x] Custom style training preparation
- [x] Export/sharing capabilities

### Phase 4: Polish & Optimization
- [ ] Comprehensive testing
- [ ] Performance profiling
- [ ] UI/UX improvements
- [ ] Documentation and tutorials

## Testing

Run unit tests:
```bash
./gradlew test
```

Run instrumentation tests:
```bash
./gradlew connectedAndroidTest
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Privacy

Unstable Confusion is designed with privacy as a core principle:
- All image generation happens on your device
- No user data is transmitted to external servers
- No analytics or tracking
- Generated images stay on your device unless you choose to share them
