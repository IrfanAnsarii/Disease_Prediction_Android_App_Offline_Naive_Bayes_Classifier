package com.irspace.diseaseprediction.model

/**
 * Data class representing training data for the Naive Bayes classifier
 * 
 * @property symptoms Map of symptom names to binary values (0 or 1)
 * @property disease The disease label/name
 */
data class TrainingData(
    val symptoms: Map<String, Int>,
    val disease: String
)
