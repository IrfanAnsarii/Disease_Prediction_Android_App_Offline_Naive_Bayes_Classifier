package com.irspace.diseaseprediction.ml


import android.util.Log
import com.irspace.diseaseprediction.model.PredictionResult
import com.irspace.diseaseprediction.model.TrainingData
import kotlin.math.ln

/**
 * Manual implementation of Naive Bayes Classifier for Disease Prediction
 *
 * Algorithm:
 * 1. Training Phase:
 *    - Calculate prior probabilities: P(Disease)
 *    - Calculate conditional probabilities: P(Symptom|Disease)
 *    - Apply Laplace smoothing to avoid zero probabilities
 *
 * 2. Prediction Phase:
 *    - For each disease, calculate: P(Disease|Symptoms) ∝ P(Disease) × ∏P(Symptom_i|Disease)
 *    - Use log probabilities to avoid numerical underflow
 *    - Return top predictions with confidence scores
 */
class NaiveBayesClassifier {

    companion object {
        private const val TAG = "NaiveBayesClassifier"
        private const val LAPLACE_SMOOTHING = 1.0
    }

    // Store prior probabilities: P(Disease)
    private val priorProbabilities = mutableMapOf<String, Double>()

    // Store conditional probabilities: P(Symptom|Disease)
    // Structure: Map<Disease, Map<Symptom, Probability>>
    private val conditionalProbabilities = mutableMapOf<String, MutableMap<String, Double>>()

    // Disease counts for calculating priors
    private val diseaseCounts = mutableMapOf<String, Int>()

    // Store all unique symptoms and diseases
    private val allSymptoms = mutableSetOf<String>()
    private val allDiseases = mutableSetOf<String>()

    // Total training samples
    private var totalSamples = 0

    /**
     * Train the Naive Bayes classifier with training data
     *
     * @param trainingData List of training samples containing symptoms and disease labels
     */
    fun train(trainingData: List<TrainingData>) {
        if (trainingData.isEmpty()) {
            Log.e(TAG, "Training data is empty!")
            return
        }

        Log.d(TAG, "Starting training with ${trainingData.size} samples")

        // Reset all data structures
        priorProbabilities.clear()
        conditionalProbabilities.clear()
        diseaseCounts.clear()
        allSymptoms.clear()
        allDiseases.clear()

        totalSamples = trainingData.size

        // Step 1: Collect all unique symptoms and diseases
        trainingData.forEach { sample ->
            allDiseases.add(sample.disease)
            allSymptoms.addAll(sample.symptoms.keys)
        }

        Log.d(TAG, "Found ${allDiseases.size} unique diseases")
        Log.d(TAG, "Found ${allSymptoms.size} unique symptoms")

        // Step 2: Calculate disease counts
        trainingData.forEach { sample ->
            diseaseCounts[sample.disease] = diseaseCounts.getOrDefault(sample.disease, 0) + 1
        }

        // Step 3: Calculate prior probabilities P(Disease)
        calculatePriorProbabilities()

        // Step 4: Calculate conditional probabilities P(Symptom|Disease)
        calculateConditionalProbabilities(trainingData)

        Log.d(TAG, "Training completed successfully!")
    }

    /**
     * Calculate prior probabilities: P(Disease) = Count(Disease) / Total Samples
     */
    private fun calculatePriorProbabilities() {
        allDiseases.forEach { disease ->
            val count = diseaseCounts[disease] ?: 0
            priorProbabilities[disease] = count.toDouble() / totalSamples
        }

        Log.d(TAG, "Prior probabilities calculated for ${priorProbabilities.size} diseases")
    }

    /**
     * Calculate conditional probabilities with Laplace smoothing
     * P(Symptom|Disease) = (Count(Symptom in Disease) + 1) / (Count(Disease) + 2)
     *
     * Laplace smoothing ensures no zero probabilities
     */
    private fun calculateConditionalProbabilities(trainingData: List<TrainingData>) {
        // Initialize conditional probability maps for each disease
        allDiseases.forEach { disease ->
            conditionalProbabilities[disease] = mutableMapOf()
        }

        // For each disease, calculate P(Symptom=1|Disease) and P(Symptom=0|Disease)
        allDiseases.forEach { disease ->
            val samplesWithDisease = trainingData.filter { it.disease == disease }
            val diseaseCount = samplesWithDisease.size.toDouble()

            allSymptoms.forEach { symptom ->
                // Count how many times this symptom appears with this disease
                val symptomPresentCount = samplesWithDisease.count { sample ->
                    sample.symptoms[symptom] == 1
                }.toDouble()

                // Apply Laplace smoothing
                // P(Symptom=1|Disease)
                val probabilityPresent = (symptomPresentCount + LAPLACE_SMOOTHING) /
                        (diseaseCount + 2 * LAPLACE_SMOOTHING)

                // Store the probability
                conditionalProbabilities[disease]!![symptom] = probabilityPresent
            }
        }

        Log.d(TAG, "Conditional probabilities calculated")
    }

    /**
     * Predict disease based on selected symptoms using Naive Bayes
     *
     * @param selectedSymptoms List of symptom names that the user has
     * @param topN Number of top predictions to return (default: 5)
     * @return List of prediction results sorted by probability (descending)
     */
    fun predict(selectedSymptoms: List<String>, topN: Int = 5): List<PredictionResult> {
        if (selectedSymptoms.isEmpty()) {
            Log.w(TAG, "No symptoms selected for prediction")
            return emptyList()
        }

        if (priorProbabilities.isEmpty()) {
            Log.e(TAG, "Model not trained yet!")
            return emptyList()
        }

        Log.d(TAG, "Predicting with ${selectedSymptoms.size} symptoms")

        val predictions = mutableListOf<PredictionResult>()

        // For each disease, calculate the posterior probability
        allDiseases.forEach { disease ->
            val logProbability = calculateLogPosterior(disease, selectedSymptoms)
            predictions.add(PredictionResult(disease, logProbability))
        }

        // Sort by probability (descending) and normalize to percentages
        val sortedPredictions = predictions.sortedByDescending { it.probability }

        // Convert log probabilities to actual probabilities
        val normalizedPredictions = normalizeProbabilities(sortedPredictions)

        // Return top N predictions
        return normalizedPredictions.take(topN)
    }

    /**
     * Calculate log posterior probability for a disease given symptoms
     * Using log probabilities to avoid numerical underflow
     *
     * log P(Disease|Symptoms) = log P(Disease) + Σ log P(Symptom_i|Disease)
     */
    private fun calculateLogPosterior(disease: String, selectedSymptoms: List<String>): Double {
        // Start with log of prior probability
        var logProb = ln(priorProbabilities[disease] ?: 0.0001)

        // Add log probabilities for each symptom
        allSymptoms.forEach { symptom ->
            val symptomPresent = selectedSymptoms.contains(symptom)
            val conditionalProb = conditionalProbabilities[disease]?.get(symptom) ?: 0.5

            // P(Symptom=1|Disease) if symptom is present, else P(Symptom=0|Disease)
            val prob = if (symptomPresent) {
                conditionalProb
            } else {
                1.0 - conditionalProb
            }

            // Add log probability (avoid log(0))
            logProb += ln(prob.coerceAtLeast(0.0001))
        }

        return logProb
    }

    /**
     * Normalize log probabilities to percentages (0-100)
     * Uses softmax-like normalization
     */
    private fun normalizeProbabilities(predictions: List<PredictionResult>): List<PredictionResult> {
        if (predictions.isEmpty()) return emptyList()

        // Find the maximum log probability for numerical stability
        val maxLogProb = predictions.maxOf { it.probability }

        // Calculate exp(log_prob - max_log_prob) for each prediction
        val expProbs = predictions.map {
            kotlin.math.exp(it.probability - maxLogProb)
        }

        // Calculate sum for normalization
        val sumExpProbs = expProbs.sum()

        // Normalize to percentages
        return predictions.mapIndexed { index, prediction ->
            val normalizedProb = (expProbs[index] / sumExpProbs) * 100.0
            PredictionResult(
                disease = prediction.disease,
                probability = normalizedProb
            )
        }
    }

    /**
     * Get all available symptoms for UI display
     */
    fun getAllSymptoms(): List<String> {
        return allSymptoms.sorted()
    }

    /**
     * Get all diseases in the training data
     */
    fun getAllDiseases(): List<String> {
        return allDiseases.sorted()
    }

    /**
     * Check if the model is trained
     */
    fun isTrained(): Boolean {
        return priorProbabilities.isNotEmpty() && conditionalProbabilities.isNotEmpty()
    }

    /**
     * Get model statistics for debugging
     */
    fun getModelStats(): String {
        return """
            Model Statistics:
            - Total Training Samples: $totalSamples
            - Unique Diseases: ${allDiseases.size}
            - Unique Symptoms: ${allSymptoms.size}
            - Trained: ${isTrained()}
        """.trimIndent()
    }
}
