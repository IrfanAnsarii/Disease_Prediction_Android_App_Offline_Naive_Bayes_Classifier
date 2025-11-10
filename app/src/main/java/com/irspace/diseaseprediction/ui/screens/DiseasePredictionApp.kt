package com.irspace.diseaseprediction.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.irspace.diseaseprediction.viewmodel.DiseasePredictionViewModel

/**
 * Main app composable with navigation
 */
@Composable
fun DiseasePredictionApp(
    viewModel: DiseasePredictionViewModel,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(
                viewModel = viewModel,
                onNavigateToSymptoms = {
                    navController.navigate("symptoms")
                }
            )
        }
        
        composable("symptoms") {
            SymptomsScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToResults = {
                    navController.navigate("results")
                }
            )
        }
        
        composable("results") {
            ResultsScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onTryAgain = {
                    viewModel.resetPredictions()
                    navController.popBackStack()
                }
            )
        }
    }
}
