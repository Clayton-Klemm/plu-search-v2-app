# PLU Search App

A Kotlin-based Android app (built using Kotlin DSL) for searching Coborn's, Inc. PLUs. This project targets Android 14.0 (API level 34).

## Prerequisites

- **Android Studio**: [Download here](https://developer.android.com/studio)
- **Java Development Kit (JDK)**: Version 8 or later ([Download here](https://www.oracle.com/java/technologies/javase-downloads.html))

## Getting Started

### 1. Clone the Repository

- **HTTPS:**

  ```bash
  git clone https://github.com/Clayton-Klemm/plu-search-v2-app.git
  ```

### 2. Open the Project in Android Studio

1. Launch Android Studio.
2. Select **"Open an existing project"** and navigate to the cloned `plu-search-v2-app` folder.
3. Allow Android Studio to sync and download all necessary dependencies.

### 3. Build the Project

1. In Android Studio, go to **Build > Make Project**.
2. Ensure the build completes without errors.

### 4. Run the App

#### On a Physical Device

1. **Enable Developer Options:**
   - Go to **Settings > About phone**.
   - Tap **"Build number"** seven times to enable Developer Options.
2. **Enable USB Debugging:**
   - Navigate to **Settings > Developer options**.
   - Toggle **USB debugging** on.
3. Connect your device via USB. Confirm that Android Studio detects it.
4. Click the **Run** (green play icon) and select your device to install and launch the app.

#### Using an Emulator

1. Open the **AVD Manager** (phone icon in the top-right corner).
2. Click **"Create Virtual Device"**, choose a model, select a compatible system image, and finish setup.
3. Launch the emulator by clicking the **Play** button.
4. Click the **Run** button in Android Studio and select the emulator.

## Troubleshooting

- **Dependency Issues:**  
  Use **File > Sync Project with Gradle Files** to resolve missing dependency errors.

- **Build Errors:**  
  Ensure all SDK components are updated via the **SDK Manager**.

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.
