package com.irspace.diseaseprediction.model

/**
 * Data class representing a prediction result
 * 
 * @property disease The predicted disease name
 * @property probability The confidence/probability score (0-100)
 */
data class PredictionResult(
    val disease: String,
    val probability: Double
) {
    /**
     * Get formatted probability as percentage string
     */
    fun getProbabilityPercentage(): String {
        return String.format("%.2f%%", probability)
    }
    
    /**
     * Get confidence level as text
     */
    fun getConfidenceLevel(): String {
        return when {
            probability >= 70.0 -> "High Confidence"
            probability >= 40.0 -> "Moderate Confidence"
            probability >= 20.0 -> "Low Confidence"
            else -> "Very Low Confidence"
        }
    }
    
    /**
     * Get display name for the disease
     */
    fun getDisplayName(): String {
        return disease.split(' ')
            .joinToString(" ") { word ->
                word.replaceFirstChar { it.uppercase() }
            }
    }
}
