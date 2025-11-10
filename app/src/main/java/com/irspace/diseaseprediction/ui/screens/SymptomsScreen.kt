package com.irspace.diseaseprediction.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.irspace.diseaseprediction.model.Symptom
import com.irspace.diseaseprediction.viewmodel.DiseasePredictionViewModel

/**
 * Symptoms Selection Screen
 * Allows users to search and select symptoms
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SymptomsScreen(
    viewModel: DiseasePredictionViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToResults: () -> Unit
) {
    val filteredSymptoms by viewModel.filteredSymptoms.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCount by viewModel.selectedSymptomsCount.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Select Symptoms")
                        if (selectedCount > 0) {
                            Text(
                                text = "$selectedCount selected",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    if (selectedCount > 0) {
                        IconButton(onClick = { viewModel.clearSelectedSymptoms() }) {
                            Icon(Icons.Default.Clear, "Clear All")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            if (selectedCount > 0) {
                BottomAppBar(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Button(
                        onClick = {
                            viewModel.predictDisease()
                            onNavigateToResults()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .height(56.dp),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Icon(Icons.Default.Search, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Predict Disease",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Search symptoms...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                            Icon(Icons.Default.Clear, "Clear search")
                        }
                    }
                },
                singleLine = true
            )
            
            // Info Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Select all symptoms you're experiencing",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
            
            // Symptoms List
            if (filteredSymptoms.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No symptoms found",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    items(filteredSymptoms) { symptom ->
                        SymptomItem(
                            symptom = symptom,
                            onToggle = { viewModel.toggleSymptom(symptom.name) }
                        )
                    }
                    
                    // Bottom spacing for the predict button
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }
}

/**
 * Individual symptom item with checkbox
 */
@OptIn(ExperimentalMaterial3Api::class) // <-- ADD THIS LINE
@Composable
fun SymptomItem(
    symptom: Symptom,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = onToggle,
        colors = CardDefaults.cardColors(
            containerColor = if (symptom.isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = symptom.isSelected,
                onCheckedChange = { onToggle() }
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = symptom.getDisplayName(),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (symptom.isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}
