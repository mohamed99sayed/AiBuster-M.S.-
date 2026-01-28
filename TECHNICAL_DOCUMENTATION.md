# AiBuster Technical Documentation

## 1. Project Overview
**AiBuster** is an Android application designed to detect AI-generated ("Deepfake") images. It utilizes on-device Machine Learning (ML) to analyze user-uploaded photos and determine their authenticity with a confidence score. The core innovation of this project is the use of an **Ensemble Learning** approach, aggregating predictions from 8 distinct PyTorch Lite models to improve accuracy and robustness.

## 2. Architecture
The application follows a single-activity architecture using Android's Navigation Component to manage UI transitions.

### 2.1 High-Level Structure
*   **Language**: Kotlin
*   **Min SDK**: 24 (Android 7.0)
*   **Target SDK**: 36 (Android 15)
*   **ML Logic**: PyTorch Mobile (Android Lite)
*   **UI Framework**: Android View System (XML) with ViewBinding

### 2.2 Core Components
| Component | Type | Responsibility |
| :--- | :--- | :--- |
| `MainActivity` | Activity | Host for the Navigation Graph. |
| `UploadFragment` | Fragment | Handles image selection (gallery), image preprocessing, model loading, and the inference loop. |
| `ResultFragment` | Fragment | Displays the classification result (REAL vs FAKE) and the confidence percentage. |
| `AboutFragment` | Fragment | Displays application information. |

## 3. Machine Learning Pipeline
The application runs entirely offline, ensuring user privacy by processing images directly on the device.

### 3.1 Model Ensemble
The application loads **8 separate PyTorch Lite models** (`model1.ptl` to `model8.ptl`) from the application assets.
*   **Format**: `.ptl` (PyTorch Lite optimized format).
*   **Input Shape**: `[1, 3, 224, 224]` (Batch Size, Channels, Height, Width).
*   **Data Type**: Float32.

### 3.2 Preprocessing
Before inference, images undergo the following transformations to match the models' training conditions (likely ImageNet-trained backbones):
1.  **Resizing**: Images are scaled to **224x224** pixels.
2.  **Normalization**:
    *   **Mean**: `[0.485, 0.456, 0.406]`
    *   **Std**: `[0.229, 0.224, 0.225]`

### 3.3 Inference Logic (`UploadFragment.kt`)
The prediction process follows these steps:
1.  **Loading**: On view creation, the app iterates from 1 to 8 to load all models into memory using `LiteModuleLoader.load()`.
2.  **Forward Pass**: When the user clicks "Classify", the preprocessed image tensor is passed through *each* of the 8 models sequentially.
3.  **Aggregation**:
    *   The raw output score (float) is collected from each model.
    *   The app calculates the **arithmetic mean** of these scores.
4.  **Classification**:
    *   **Threshold**: `0.5`
    *   **Score < 0.5**: Classified as **REAL**. (Confidence = `1 - score`)
    *   **Score >= 0.5**: Classified as **FAKE (AI)**. (Confidence = `score`)

## 4. Dependencies
Key libraries used in `app/build.gradle.kts`:

### 4.1 Machine Learning
```kotlin
implementation("org.pytorch:pytorch_android_lite:1.13.1")
implementation("org.pytorch:pytorch_android_torchvision_lite:1.13.1")
```
*Note: TensorFlow Lite libraries are present in the build file but are not currently used in the primary inference path.*

### 4.2 Android Core
```kotlin
implementation("androidx.core:core-ktx:1.17.0")
implementation("androidx.appcompat:appcompat:1.7.1")
implementation("androidx.constraintlayout:constraintlayout:2.2.1")
implementation("androidx.navigation:navigation-fragment-ktx:2.9.4")
implementation("androidx.navigation:navigation-ui-ktx:2.9.4")
```

## 5. Security & Privacy
*   **Local Processing**: All image analysis happens on the device; no images are uploaded to external servers.
*   **Permissions**:
    *   `READ_EXTERNAL_STORAGE` (implied via modern Activity Result API logic for image picking).

## 6. Future Improvements
*   **Asynchronous Inference**: The inference models currently run on the main thread. Moving this to a background thread (using Kotlin Coroutines) is recommended to prevent UI freezing during analysis.
*   **Model Optimization**: Quantizing models to INT8 could reduce binary size.
