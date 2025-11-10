package com.irspace.diseaseprediction.model

/**
 * Data class representing a symptom
 * 
 * @property name The symptom name (e.g., "itching", "fever")
 * @property isSelected Whether this symptom is selected by the user
 */
data class Symptom(
    val name: String,
    val isSelected: Boolean = false
) {
    /**
     * Get display name with proper formatting
     * Converts underscore to spaces and capitalizes words
     */
    fun getDisplayName(): String {
        return name.replace('_', ' ')
            .split(' ')
            .joinToString(" ") { word ->
                word.replaceFirstChar { it.uppercase() }
            }
    }
}
