#!/bin/bash

# Build Verification Script for Unstable Confusion
# Run this script in a local Android development environment to verify build readiness

echo "🔧 Unstable Confusion Build Verification"
echo "======================================="

# Check prerequisites
echo "📋 Checking Prerequisites..."

# Java version
echo -n "☕ Java version: "
java -version 2>&1 | head -1

# Android SDK
if [ -n "$ANDROID_HOME" ]; then
    echo "✅ Android SDK: $ANDROID_HOME"
    if [ -d "$ANDROID_HOME/platforms/android-34" ]; then
        echo "✅ Target API 34 available"
    else
        echo "⚠️  Target API 34 not found - install Android SDK 34"
    fi
else
    echo "❌ ANDROID_HOME not set"
fi

# Gradle wrapper
echo -n "🔨 Gradle wrapper: "
if [ -x "./gradlew" ]; then
    echo "✅ Available"
    ./gradlew --version | grep "Gradle"
else
    echo "❌ Not found or not executable"
fi

echo ""
echo "🏗️  Building Project..."

# Clean build
echo "🧹 Cleaning project..."
if ./gradlew clean; then
    echo "✅ Clean successful"
else
    echo "❌ Clean failed"
    exit 1
fi

# Build project
echo "🔨 Building project..."
if ./gradlew build; then
    echo "✅ Build successful"
else
    echo "❌ Build failed"
    exit 1
fi

# Run tests
echo "🧪 Running unit tests..."
if ./gradlew test; then
    echo "✅ Tests passed"
else
    echo "❌ Tests failed"
    exit 1
fi

# Generate debug APK
echo "📱 Generating debug APK..."
if ./gradlew assembleDebug; then
    echo "✅ APK generated successfully"
    echo "📍 APK location: app/build/outputs/apk/debug/"
    ls -la app/build/outputs/apk/debug/ 2>/dev/null || echo "APK directory not found"
else
    echo "❌ APK generation failed"
    exit 1
fi

echo ""
echo "🎉 Build verification completed successfully!"
echo "The Unstable Confusion project is ready for development."