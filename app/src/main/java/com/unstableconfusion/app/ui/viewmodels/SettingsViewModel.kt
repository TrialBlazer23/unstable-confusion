package com.unstableconfusion.app.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.unstableconfusion.app.domain.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel for settings screen with model management
 */
class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    
    private val modelManager = ModelManager(application)
    
    private val _availableModels = MutableStateFlow<List<AvailableModel>>(emptyList())
    val availableModels: StateFlow<List<AvailableModel>> = _availableModels.asStateFlow()
    
    private val _downloadedModels = MutableStateFlow<List<AvailableModel>>(emptyList())
    val downloadedModels: StateFlow<List<AvailableModel>> = _downloadedModels.asStateFlow()
    
    private val _downloadProgress = MutableStateFlow<ModelDownloadProgress?>(null)
    val downloadProgress: StateFlow<ModelDownloadProgress?> = _downloadProgress.asStateFlow()
    
    private val _storageInfo = MutableStateFlow(StorageInfo(0L, 0L))
    val storageInfo: StateFlow<StorageInfo> = _storageInfo.asStateFlow()
    
    init {
        loadModels()
        updateStorageInfo()
    }
    
    private fun loadModels() {
        viewModelScope.launch {
            _availableModels.value = modelManager.getAvailableModels()
            _downloadedModels.value = modelManager.getDownloadedModels()
        }
    }
    
    private fun updateStorageInfo() {
        viewModelScope.launch {
            val usedMB = modelManager.getTotalModelStorageMB()
            val availableMB = modelManager.getAvailableStorageMB()
            _storageInfo.value = StorageInfo(usedMB, availableMB)
        }
    }
    
    fun downloadModel(modelId: String) {
        viewModelScope.launch {
            modelManager.downloadModel(modelId).collect { progress ->
                _downloadProgress.value = progress
                
                if (progress.isComplete) {
                    _downloadProgress.value = null
                    loadModels()
                    updateStorageInfo()
                }
            }
        }
    }
    
    fun deleteModel(modelId: String) {
        viewModelScope.launch {
            modelManager.deleteModel(modelId)
            loadModels()
            updateStorageInfo()
        }
    }
    
    fun clearAllModels() {
        viewModelScope.launch {
            modelManager.clearAllModels()
            loadModels()
            updateStorageInfo()
        }
    }
    
    fun isModelDownloaded(modelId: String): Boolean {
        return modelManager.isModelDownloaded(modelId)
    }
    
    fun getModelsByType(type: ModelType): List<AvailableModel> {
        return _availableModels.value.filter { it.type == type }
    }
    
    fun getDownloadedModelsByType(type: ModelType): List<AvailableModel> {
        return _downloadedModels.value.filter { it.type == type }
    }
}

/**
 * Storage information for models
 */
data class StorageInfo(
    val usedMB: Long,
    val availableMB: Long
) {
    val totalMB: Long get() = usedMB + availableMB
    val usagePercentage: Float get() = if (totalMB > 0) usedMB.toFloat() / totalMB else 0f
}