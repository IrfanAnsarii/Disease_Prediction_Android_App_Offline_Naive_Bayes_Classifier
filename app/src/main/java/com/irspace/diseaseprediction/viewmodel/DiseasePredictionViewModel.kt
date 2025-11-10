package com.irspace.diseaseprediction.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.irspace.diseaseprediction.ml.CSVParser
import com.irspace.diseaseprediction.ml.NaiveBayesClassifier
import com.irspace.diseaseprediction.model.PredictionResult
import com.irspace.diseaseprediction.model.Symptom
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel for Disease Prediction
 * Manages UI state, model training, and predictions
 */
class DiseasePredictionViewModel(application: Application) : AndroidViewModel(application) {
    
    companion object {
        private const val TAG = "DiseasePredictionVM"
    }
    
    // Naive Bayes Classifier instance
    private val classifier = NaiveBayesClassifier()
    
    // CSV Parser instance
    private val csvParser = CSVParser(application.applicationContext)
    
    // UI State: List of all symptoms
    private val _symptoms = MutableStateFlow<List<Symptom>>(emptyList())
    val symptoms: StateFlow<List<Symptom>> = _symptoms.asStateFlow()
    
    // UI State: Search query for filtering symptoms
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    // UI State: Filtered symptoms based on search
    private val _filteredSymptoms = MutableStateFlow<List<Symptom>>(emptyList())
    val filteredSymptoms: StateFlow<List<Symptom>> = _filteredSymptoms.asStateFlow()
    
    // UI State: Prediction results
    private val _predictions = MutableStateFlow<List<PredictionResult>>(emptyList())
    val predictions: StateFlow<List<PredictionResult>> = _predictions.asStateFlow()
    
    // UI State: Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // UI State: Error message
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    // UI State: Model training status
    private val _isModelTrained = MutableStateFlow(false)
    val isModelTrained: StateFlow<Boolean> = _isModelTrained.asStateFlow()
    
    // UI State: Number of selected symptoms
    private val _selectedSymptomsCount = MutableStateFlow(0)
    val selectedSymptomsCount: StateFlow<Int> = _selectedSymptomsCount.asStateFlow()
    
    init {
        // Initialize and train model when ViewModel is created
        trainModel()
    }
    
    /**
     * Train the Naive Bayes model with CSV data
     */
    fun trainModel() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                
                Log.d(TAG, "Starting model training...")
                
                withContext(Dispatchers.IO) {
                    // Validate CSV files
                    if (!csvParser.validateCSVFile("training.csv")) {
                        throw Exception("Invalid training.csv format")
                    }
                    
                    // Parse training data
                    val trainingData = csvParser.parseTrainingData()
                    
                    if (trainingData.isEmpty()) {
                        throw Exception("No training data found")
                    }
                    
                    Log.d(TAG, "Loaded ${trainingData.size} training samples")
                    
                    // Train classifier
                    classifier.train(trainingData)
                    
                    // Load all symptoms
                    val symptomNames = classifier.getAllSymptoms()
                    _symptoms.value = symptomNames.map { Symptom(it, false) }
                    _filteredSymptoms.value = _symptoms.value
                    
                    Log.d(TAG, "Model trained with ${symptomNames.size} symptoms")
                    Log.d(TAG, classifier.getModelStats())
                }
                
                _isModelTrained.value = true
                _isLoading.value = false
                
            } catch (e: Exception) {
                Log.e(TAG, "Error training model: ${e.message}")
                _errorMessage.value = "Failed to train model: ${e.message}"
                _isLoading.value = false
                _isModelTrained.value = false
            }
        }
    }
    
    /**
     * Toggle symptom selection
     */
    fun toggleSymptom(symptomName: String) {
        _symptoms.value = _symptoms.value.map { symptom ->
            if (symptom.name == symptomName) {
                symptom.copy(isSelected = !symptom.isSelected)
            } else {
                symptom
            }
        }
        
        // Update filtered symptoms
        updateFilteredSymptoms()
        
        // Update selected count
        _selectedSymptomsCount.value = _symptoms.value.count { it.isSelected }
    }
    
    /**
     * Update search query and filter symptoms
     */
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        updateFilteredSymptoms()
    }
    
    /**
     * Filter symptoms based on search query
     */
    private fun updateFilteredSymptoms() {
        val query = _searchQuery.value.lowercase()
        _filteredSymptoms.value = if (query.isEmpty()) {
            _symptoms.value
        } else {
            _symptoms.value.filter { symptom ->
                symptom.name.lowercase().contains(query) ||
                symptom.getDisplayName().lowercase().contains(query)
            }
        }
    }
    
    /**
     * Predict disease based on selected symptoms
     */
    fun predictDisease() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                
                val selectedSymptoms = _symptoms.value
                    .filter { it.isSelected }
                    .map { it.name }
                
                if (selectedSymptoms.isEmpty()) {
                    _errorMessage.value = "Please select at least one symptom"
                    _isLoading.value = false
                    return@launch
                }
                
                Log.d(TAG, "Predicting with ${selectedSymptoms.size} symptoms")
                
                val results = withContext(Dispatchers.Default) {
                    classifier.predict(selectedSymptoms, topN = 5)
                }
                
                _predictions.value = results
                _isLoading.value = false
                
                Log.d(TAG, "Prediction complete: ${results.size} results")
                results.take(3).forEach {
                    Log.d(TAG, "${it.disease}: ${it.getProbabilityPercentage()}")
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Error predicting disease: ${e.message}")
                _errorMessage.value = "Prediction failed: ${e.message}"
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Clear all selected symptoms
     */
    fun clearSelectedSymptoms() {
        _symptoms.value = _symptoms.value.map { it.copy(isSelected = false) }
        _filteredSymptoms.value = _symptoms.value
        _selectedSymptomsCount.value = 0
        _predictions.value = emptyList()
        _searchQuery.value = ""
    }
    
    /**
     * Clear error message
     */
    fun clearError() {
        _errorMessage.value = null
    }
    
    /**
     * Get selected symptoms
     */
    fun getSelectedSymptoms(): List<Symptom> {
        return _symptoms.value.filter { it.isSelected }
    }
    
    /**
     * Reset predictions
     */
    fun resetPredictions() {
        _predictions.value = emptyList()
    }
    
    /**
     * Get model statistics for debugging
     */
    fun getModelStatistics(): String {
        return if (_isModelTrained.value) {
            classifier.getModelStats() + "\n" +
            csvParser.getFileStats("training.csv")
        } else {
            "Model not trained yet"
        }
    }
}
