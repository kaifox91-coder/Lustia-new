# Complete Setup Guide: Build Android APK for Lustia-new

## What You'll Need
- **Computer**: Windows, Mac, or Linux
- **Android Studio**: [Download here](https://developer.android.com/studio)
- **Android SDK**: API 26+ (installed with Android Studio)
- **Gemini API Key**: [Get free here](https://aistudio.google.com/app/apikey)
- **USB Cable**: Optional, for direct phone install

---

## STEP 1: Install Android Studio

### Windows & Mac
1. Download from [developer.android.com/studio](https://developer.android.com/studio)
2. Run the installer
3. Follow the wizard and accept defaults
4. Launch Android Studio
5. Complete initial SDK setup

### Linux
```bash
sudo apt-get update
sudo apt-get install android-studio
```

**Expected time**: 10–15 minutes

---

## STEP 2: Clone the Repository

### Using Git (Recommended)
```bash
git clone https://github.com/kaifox91-coder/Lustia-new.git
cd Lustia-new
```

### Or Download as ZIP
1. Visit: https://github.com/kaifox91-coder/Lustia-new
2. Click **Code** → **Download ZIP**
3. Extract the ZIP file
4. Open folder in terminal

---

## STEP 3: Open Project in Android Studio

1. Launch Android Studio
2. Click **File** → **Open**
3. Select the `Lustia-new` folder
4. Wait for Gradle sync to complete

---

## STEP 4: Create Your Gemini API Key

1. Open https://aistudio.google.com/app/apikey
2. Sign in with your Google account
3. Click **Create API Key**
4. Copy the key

⚠️ Keep this key private.

---

## STEP 5: Add API Key Privately (Single Entry)

Create or update `local.properties` in the project root:

```properties
GEMINI_API_KEY=AIzaSy_your_actual_key_here
```

That is the only key entry required for this project.

---

## STEP 6: Build the APK

### Option A: Android Studio
1. **Build** → **Build Bundle(s)/APK(s)** → **Build APK(s)**
2. Wait for success message

### Option B: Terminal
```bash
./gradlew assembleDebug
```

APK output path:
```text
app/build/outputs/apk/debug/app-debug.apk
```

---

## STEP 7: Install on Phone

### USB
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

### Manual
Transfer APK to phone and tap to install.

---

## Troubleshooting

### "GEMINI_API_KEY is missing"
- Confirm `local.properties` exists at repo root
- Confirm line format exactly:
  `GEMINI_API_KEY=AIzaSy_...`
- Re-sync Gradle and rebuild

### Build fails after key changes
- Android Studio: **File → Sync Project with Gradle Files**
- Then run clean build:
```bash
./gradlew clean assembleDebug
```

### API intermittently unavailable / high demand
- Retry after a short delay
- Keep requests moderate
- Check your API quota and key status in AI Studio
