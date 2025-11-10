package com.irspace.diseaseprediction.ml


import android.content.Context
import android.util.Log
import com.irspace.diseaseprediction.model.TrainingData
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

/**
 * CSV Parser for loading training and testing data from assets folder
 * Parses CSV files with symptom columns (binary 0/1) and disease label
 */
class CSVParser(private val context: Context) {

    companion object {
        private const val TAG = "CSVParser"
        private const val TRAINING_FILE = "training.csv"
        private const val TESTING_FILE = "testing.csv"
    }

    /**
     * Parse training CSV file from assets
     *
     * Expected format:
     * symptom1,symptom2,...,symptomN,prognosis
     * 0,1,0,...,1,Disease Name
     *
     * @return List of TrainingData objects
     */
    fun parseTrainingData(): List<TrainingData> {
        return parseCSVFile(TRAINING_FILE)
    }

    /**
     * Parse testing CSV file from assets
     *
     * @return List of TrainingData objects (can be used for validation)
     */
    fun parseTestingData(): List<TrainingData> {
        return parseCSVFile(TESTING_FILE)
    }

    /**
     * Parse a CSV file from assets folder
     *
     * @param fileName Name of the CSV file in assets folder
     * @return List of TrainingData objects
     */
    private fun parseCSVFile(fileName: String): List<TrainingData> {
        val trainingDataList = mutableListOf<TrainingData>()
        var symptomHeaders: List<String>? = null
        var lineCount = 0

        try {
            val inputStream = context.assets.open(fileName)
            val reader = BufferedReader(InputStreamReader(inputStream))

            reader.useLines { lines ->
                lines.forEachIndexed { index, line ->
                    if (line.isBlank()) return@forEachIndexed

                    lineCount++

                    if (index == 0) {
                        // First line contains headers (symptom names + "prognosis")
                        symptomHeaders = line.split(",").map { it.trim() }
                        Log.d(TAG, "Found ${symptomHeaders!!.size} columns in $fileName")
                    } else {
                        // Data lines
                        try {
                            val values = line.split(",").map { it.trim() }

                            if (values.size != symptomHeaders!!.size) {
                                Log.w(TAG, "Line $lineCount: Column count mismatch. Expected ${symptomHeaders!!.size}, got ${values.size}")
                                return@forEachIndexed
                            }

                            // Last column is the disease name (prognosis)
                            val disease = values.last()

                            // All other columns are symptoms (0 or 1)
                            val symptoms = mutableMapOf<String, Int>()

                            for (i in 0 until values.size - 1) {
                                val symptomName = symptomHeaders!![i]
                                val symptomValue = values[i].toIntOrNull() ?: 0
                                symptoms[symptomName] = symptomValue
                            }

                            trainingDataList.add(
                                TrainingData(
                                    symptoms = symptoms,
                                    disease = disease
                                )
                            )
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing line $lineCount: ${e.message}")
                        }
                    }
                }
            }

            Log.d(TAG, "Successfully parsed $fileName: ${trainingDataList.size} records")

        } catch (e: IOException) {
            Log.e(TAG, "Error reading file $fileName: ${e.message}")
            e.printStackTrace()
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error parsing $fileName: ${e.message}")
            e.printStackTrace()
        }

        return trainingDataList
    }

    /**
     * Get all unique symptom names from the training data
     *
     * @return Sorted list of symptom names
     */
    fun getSymptomNames(): List<String> {
        try {
            val inputStream = context.assets.open(TRAINING_FILE)
            val reader = BufferedReader(InputStreamReader(inputStream))

            val firstLine = reader.readLine()
            reader.close()

            if (firstLine != null) {
                // First line contains headers, last column is "prognosis"
                val headers = firstLine.split(",").map { it.trim() }
                // Remove the last column (prognosis) to get only symptoms
                return headers.dropLast(1).sorted()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error reading symptom names: ${e.message}")
        }

        return emptyList()
    }

    /**
     * Get all unique disease names from the training data
     *
     * @return Sorted list of disease names
     */
    fun getDiseaseNames(): List<String> {
        val diseases = mutableSetOf<String>()

        try {
            val inputStream = context.assets.open(TRAINING_FILE)
            val reader = BufferedReader(InputStreamReader(inputStream))

            reader.useLines { lines ->
                lines.drop(1).forEach { line -> // Skip header
                    if (line.isNotBlank()) {
                        val values = line.split(",")
                        if (values.isNotEmpty()) {
                            diseases.add(values.last().trim())
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error reading disease names: ${e.message}")
        }

        return diseases.sorted()
    }

    /**
     * Validate CSV file format
     *
     * @param fileName Name of CSV file to validate
     * @return true if file is valid, false otherwise
     */
    fun validateCSVFile(fileName: String): Boolean {
        try {
            val inputStream = context.assets.open(fileName)
            val reader = BufferedReader(InputStreamReader(inputStream))

            val firstLine = reader.readLine()
            val secondLine = reader.readLine()

            reader.close()

            if (firstLine == null || secondLine == null) {
                Log.e(TAG, "$fileName: File is empty or has insufficient data")
                return false
            }

            val headerCount = firstLine.split(",").size
            val dataCount = secondLine.split(",").size

            if (headerCount != dataCount) {
                Log.e(TAG, "$fileName: Header and data column count mismatch")
                return false
            }

            // Check if last header is "prognosis"
            val headers = firstLine.split(",").map { it.trim() }
            if (headers.last().lowercase() != "prognosis") {
                Log.w(TAG, "$fileName: Last column should be 'prognosis', found '${headers.last()}'")
            }

            Log.d(TAG, "$fileName: Validation passed")
            return true

        } catch (e: Exception) {
            Log.e(TAG, "Error validating $fileName: ${e.message}")
            return false
        }
    }

    /**
     * Get CSV file statistics
     */
    fun getFileStats(fileName: String): String {
        try {
            val inputStream = context.assets.open(fileName)
            val reader = BufferedReader(InputStreamReader(inputStream))

            var lineCount = 0
            var columnCount = 0

            reader.useLines { lines ->
                lines.forEachIndexed { index, line ->
                    if (line.isNotBlank()) {
                        lineCount++
                        if (index == 0) {
                            columnCount = line.split(",").size
                        }
                    }
                }
            }

            return """
                File: $fileName
                Total Lines: $lineCount
                Data Records: ${lineCount - 1}
                Columns: $columnCount
                Symptoms: ${columnCount - 1}
            """.trimIndent()

        } catch (e: Exception) {
            return "Error reading file: ${e.message}"
        }
    }
}
