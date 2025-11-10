package com.irspace.diseaseprediction.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
//import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.irspace.diseaseprediction.model.PredictionResult
import com.irspace.diseaseprediction.viewmodel.DiseasePredictionViewModel
import kotlin.collections.joinToString
import kotlin.collections.take
import androidx.compose.runtime.getValue
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.collectAsState
//import kotlin.collections.emptyList


/**
 * Results Screen - Shows prediction results
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultsScreen(
    viewModel: DiseasePredictionViewModel,
    onNavigateBack: () -> Unit,
    onTryAgain: () -> Unit
) {
    val predictions by viewModel.predictions.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val selectedSymptoms = viewModel.getSelectedSymptoms()
//    val selectedSymptoms by viewModel.selectedSymptoms.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Prediction Results") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Analyzing symptoms...",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp)
            ) {
                // Selected Symptoms Summary
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Selected Symptoms (${selectedSymptoms.size})",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
//                                text = selectedSymptoms.joinToString(", ") { it.getDisplayName() },
                                text = selectedSymptoms.joinToString(", ") { symptom -> symptom.name },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
                
                item { Spacer(modifier = Modifier.height(16.dp)) }
                
                // Results Header
                item {
                    Text(
                        text = "Top Predictions",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                
                // Prediction Results
                if (predictions.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "No predictions available",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                } else {
                    itemsIndexed(predictions.take(5)) { index, prediction ->
                        PredictionResultCard(
                            prediction = prediction,
                            rank = index + 1
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
                
                item { Spacer(modifier = Modifier.height(16.dp)) }
                
                // Disclaimer
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Important Notice",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "These predictions are based on machine learning and should NOT be used for self-diagnosis. " +
                                       "Please consult a qualified healthcare professional for proper medical advice.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }
                
                item { Spacer(modifier = Modifier.height(16.dp)) }
                
                // Action Buttons
                item {
                    Column {
                        Button(
                            onClick = onTryAgain,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "New Diagnosis",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        OutlinedButton(
                            onClick = onNavigateBack,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Back to Home",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Individual prediction result card
 */
@Composable
fun PredictionResultCard(
    prediction: PredictionResult,
    rank: Int
) {
    val cardColor = when (rank) {
        1 -> MaterialTheme.colorScheme.primaryContainer
        2 -> MaterialTheme.colorScheme.secondaryContainer
        3 -> MaterialTheme.colorScheme.tertiaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    
    val badgeColor = when (rank) {
        1 -> MaterialTheme.colorScheme.primary
        2 -> MaterialTheme.colorScheme.secondary
        3 -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.outline
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (rank <= 3) 4.dp else 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Rank Badge
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = badgeColor
                ) {
                    Text(
                        text = "#$rank",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
                
                // Confidence Badge
                AssistChip(
                    onClick = { },
                    label = {
                        Text(
                            text = prediction.getConfidenceLevel(),
                            style = MaterialTheme.typography.labelMedium
                        )
                    },
                    enabled = false
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Disease Name
            Text(
                text = prediction.getDisplayName(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Probability
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Confidence: ",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = prediction.getProbabilityPercentage(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = badgeColor
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Progress Bar
            LinearProgressIndicator(
                progress = (prediction.probability / 100.0).toFloat(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = badgeColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}
