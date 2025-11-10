package com.irspace.diseaseprediction.model

/**
 * Data class representing a disease
 *
 * @property name The disease name
 * @property description Brief description of the disease (optional)
 */
data class Disease(
    val name: String,
    val description: String = ""
) {
    /**
     * Get display name with proper formatting
     */
    fun getDisplayName(): String {
        return name.split(' ')
            .joinToString(" ") { word ->
                word.replaceFirstChar { it.uppercase() }
            }
    }
}
