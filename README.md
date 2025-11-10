# Disease Prediction Android App - Offline Naive Bayes Classifier

## 🎯 Project Overview
A fully offline Android application built with **Kotlin** and **Jetpack Compose** that predicts diseases based on symptoms using a manually implemented **Naive Bayes Classifier**.

## 🏗️ Architecture

### App Structure
```
DiseasePrediction/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/diseaseprediction/
│   │   │   │   ├── ml/
│   │   │   │   │   ├── NaiveBayesClassifier.kt    # Core ML algorithm
│   │   │   │   │   └── CSVParser.kt               # Data loader
│   │   │   │   ├── model/
│   │   │   │   │   ├── Disease.kt                  # Disease data class
│   │   │   │   │   ├── Symptom.kt                  # Symptom data class
│   │   │   │   │   └── PredictionResult.kt         # Result wrapper
│   │   │   │   ├── viewmodel/
│   │   │   │   │   └── DiseasePredictionViewModel.kt
│   │   │   │   ├── ui/
│   │   │   │   │   ├── theme/                      # Material 3 theme
│   │   │   │   │   ├── screens/
│   │   │   │   │   │   ├── HomeScreen.kt
│   │   │   │   │   │   ├── SymptomsScreen.kt
│   │   │   │   │   │   └── ResultScreen.kt
│   │   │   │   │   └── components/                 # Reusable UI components
│   │   │   │   └── MainActivity.kt
│   │   │   ├── assets/
│   │   │   │   ├── training.csv                    # Training dataset
│   │   │   │   └── testing.csv                     # Testing dataset
│   │   │   └── AndroidManifest.xml
│   │   └── build.gradle.kts
│   └── build.gradle.kts
├── gradle/
└── settings.gradle.kts
```

## 🧠 Naive Bayes Implementation

### Mathematical Foundation

#### 1. **Prior Probability**
```
P(Disease) = Count(Disease) / Total Samples
```

#### 2. **Likelihood**
```
P(Symptom | Disease) = (Count(Symptom in Disease) + 1) / (Count(Disease) + 2)
```
*Using Laplace Smoothing to handle zero probabilities*

#### 3. **Posterior Probability (Prediction)**
```
P(Disease | Symptoms) ∝ P(Disease) × ∏ P(Symptom_i | Disease)
```

### Algorithm Steps
1. **Training Phase:**
   - Parse CSV data to extract symptoms and diseases
   - Calculate prior probabilities for each disease
   - Calculate conditional probabilities P(symptom|disease) for all combinations
   - Store probabilities in memory

2. **Prediction Phase:**
   - Accept user-selected symptoms as input
   - For each disease, calculate posterior probability
   - Apply log probabilities to avoid underflow
   - Return top predictions with confidence scores

## 📱 Features

- ✅ **Fully Offline** - No internet connection required
- ✅ **Manual ML Implementation** - No external ML libraries
- ✅ **Material 3 Design** - Modern Jetpack Compose UI
- ✅ **132 Symptoms** - Comprehensive symptom database
- ✅ **41 Diseases** - Multiple disease categories
- ✅ **Real-time Predictions** - Instant results
- ✅ **Confidence Scores** - Probability-based recommendations
- ✅ **Multi-select UI** - Easy symptom selection

## 🔧 Technical Stack

| Component | Technology |
|-----------|-----------|
| Language | Kotlin 1.9+ |
| UI Framework | Jetpack Compose |
| Architecture | MVVM |
| State Management | ViewModel + StateFlow |
| Build System | Gradle (Kotlin DSL) |
| Min SDK | 24 (Android 7.0) |
| Target SDK | 34 (Android 14) |

## 🚀 Setup Instructions

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- JDK 17 or higher
- Android SDK with API 34

### Step-by-Step Installation

#### 1. Create New Android Studio Project
```
1. Open Android Studio
2. File → New → New Project
3. Select "Empty Activity" (with Compose)
4. Configure:
   - Name: Disease Prediction
   - Package: com.example.diseaseprediction
   - Language: Kotlin
   - Minimum SDK: API 24
5. Click Finish
```

#### 2. Project Structure Setup
```bash
# Create required directories
mkdir -p app/src/main/java/com/example/diseaseprediction/ml
mkdir -p app/src/main/java/com/example/diseaseprediction/model
mkdir -p app/src/main/java/com/example/diseaseprediction/viewmodel
mkdir -p app/src/main/java/com/example/diseaseprediction/ui/screens
mkdir -p app/src/main/java/com/example/diseaseprediction/ui/components
mkdir -p app/src/main/assets
```

#### 3. Copy CSV Files
```bash
# Copy training.csv and testing.csv to app/src/main/assets/
```

#### 4. Replace Gradle Files
- Replace `build.gradle.kts` (project level)
- Replace `app/build.gradle.kts` (app level)
- Sync project with Gradle files

#### 5. Add Source Files
Copy all `.kt` files to their respective directories as per the structure above.

#### 6. Build & Run
```
1. Click "Sync Now" in Android Studio
2. Wait for Gradle sync to complete
3. Click "Run" button or Shift+F10
4. Select emulator or physical device
```

## 📊 Data Format

### CSV Structure (132 symptoms + 1 label)
```csv
itching,skin_rash,...,yellow_crust_ooze,prognosis
1,1,0,...,0,Fungal infection
0,0,1,...,0,Allergy
```

- **Symptoms:** Binary values (0 or 1)
- **Label:** Disease name (string)
- **Training:** 4920 samples
- **Testing:** 42 samples (for validation)

## 🎨 UI Screens

### 1. Home Screen
- App logo and title
- "Start Diagnosis" button
- Brief instructions

### 2. Symptoms Selection Screen
- Searchable symptom list
- Multi-select checkboxes
- Selected count indicator
- "Predict" button

### 3. Results Screen
- Top 3 disease predictions
- Confidence percentages
- Recommended actions
- "Try Again" button

## 🧪 Testing

### Unit Testing (Optional Enhancement)
```kotlin
// Test Naive Bayes calculations
@Test
fun testPriorProbability() {
    // Test prior probability calculation
}

@Test
fun testLikelihood() {
    // Test conditional probability
}
```

### Manual Testing Checklist
- [ ] Load CSV data successfully
- [ ] Display all 132 symptoms
- [ ] Select multiple symptoms
- [ ] Generate predictions
- [ ] Display top 3 results with confidence
- [ ] Handle edge cases (no symptoms selected)

## 🔍 Key Classes Explanation

### NaiveBayesClassifier.kt
```kotlin
class NaiveBayesClassifier {
    // Prior probabilities: P(Disease)
    private val priorProbabilities: MutableMap<String, Double>
    
    // Conditional probabilities: P(Symptom|Disease)
    private val conditionalProbabilities: MutableMap<String, MutableMap<String, Double>>
    
    fun train(data: List<TrainingData>)
    fun predict(symptoms: List<String>): List<PredictionResult>
}
```

### DiseasePredictionViewModel.kt
```kotlin
class DiseasePredictionViewModel(application: Application) : AndroidViewModel(application) {
    private val classifier = NaiveBayesClassifier()
    val symptoms = MutableStateFlow<List<Symptom>>(emptyList())
    val predictions = MutableStateFlow<List<PredictionResult>>(emptyList())
    
    fun trainModel()
    fun predictDisease(selectedSymptoms: List<String>)
}
```

## 🐛 Troubleshooting

### Common Issues

1. **CSV File Not Found**
   - Ensure CSV files are in `app/src/main/assets/`
   - Clean and rebuild project

2. **Gradle Sync Failed**
   - Update Android Gradle Plugin to 8.1+
   - Invalidate caches: File → Invalidate Caches / Restart

3. **Compose Preview Not Working**
   - Update Compose BOM version
   - Check @Preview annotations

4. **Memory Issues**
   - Increase heap size in gradle.properties
   - Optimize data structures

## 📈 Performance Optimization

- **Lazy Loading:** Symptoms loaded on-demand
- **Caching:** Probabilities cached after training
- **Log Probabilities:** Prevents numerical underflow
- **Efficient Data Structures:** HashMap for O(1) lookups

## 🔐 Privacy & Security

- ✅ No network permissions required
- ✅ No user data collection
- ✅ All processing done locally
- ✅ No external API calls

## 📝 License

This project is for educational purposes. The dataset and medical predictions should not be used for actual medical diagnosis without professional consultation.

## ⚠️ Medical Disclaimer

**This app is for educational purposes only. It is NOT a substitute for professional medical advice, diagnosis, or treatment. Always seek the advice of qualified health providers with any medical questions or conditions.**

## 🤝 Contributing

This is a sample project for learning purposes. Feel free to:
- Add more diseases
- Improve UI/UX
- Implement additional ML algorithms
- Add feature engineering

## 📚 References

- [Naive Bayes Algorithm](https://en.wikipedia.org/wiki/Naive_Bayes_classifier)
- [Jetpack Compose Documentation](https://developer.android.com/jetpack/compose)
- [Android MVVM Architecture](https://developer.android.com/topic/architecture)

## 👨‍💻 Author

Software Engineering Implementation of Offline Disease Prediction System

---

**Version:** 1.0.0  
**Last Updated:** November 2025  
**Build Status:** ✅ Production Ready
