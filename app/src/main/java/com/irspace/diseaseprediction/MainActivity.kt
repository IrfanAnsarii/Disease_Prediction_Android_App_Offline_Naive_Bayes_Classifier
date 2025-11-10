package com.irspace.diseaseprediction

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.irspace.diseaseprediction.ui.screens.DiseasePredictionApp
import com.irspace.diseaseprediction.ui.theme.DiseasePredictionTheme
import com.irspace.diseaseprediction.viewmodel.DiseasePredictionViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DiseasePredictionTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel: DiseasePredictionViewModel = viewModel()
                    DiseasePredictionApp(viewModel = viewModel)
                }
            }
        }
    }
}