# 🛠️ SETUP GUIDE - Dungeon Boss Chatbot APK

## Complete Step-by-Step Build Instructions

### What You'll Need
- **Computer**: Windows, Mac, or Linux
- **Android Studio**: [Download here](https://developer.android.com/studio)
- **Android SDK**: API 26+ (installed with Android Studio)
- **Gemini API Key**: [Get free here](https://aistudio.google.com/app/apikey)
- **USB Cable**: To connect your Android phone (optional - you can sideload APK)

---

## STEP 1: Install Android Studio

### Windows & Mac
1. Download from [developer.android.com/studio](https://developer.android.com/studio)
2. Run the installer
3. Follow the wizard, accept defaults
4. Launch Android Studio
5. Go through initial setup (downloads SDK components)

### Linux
```bash
sudo apt-get update
sudo apt-get install android-studio
```

**Expected time**: 10-15 minutes

---

## STEP 2: Clone the Repository

### Using Git (Recommended)
```bash
# Open terminal/command prompt
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

1. **Launch Android Studio**
2. Click **File** → **Open**
3. Navigate to the `Lustia-new` folder
4. Click **Open**
5. Wait for Gradle sync (bottom right progress bar)
   - This downloads all dependencies (~5-10 minutes)
   - You'll see: ✅ "Gradle build finished"

---

## STEP 4: Get Your Gemini API Key

### Free API Key (Recommended for Development)

1. Open browser → https://aistudio.google.com/app/apikey
2. Sign in with Google account (create free account if needed)
3. Click **Create API Key** → **Create API key in new project**
4. **Copy the key** (looks like: `AIzaSy...`)
5. Save it in a text file

⚠️ **IMPORTANT**: Keep this key private! It's like a password.

---

## STEP 5: Add API Key Securely (Recommended)

1. Create or edit your Gradle user properties file:
   - macOS/Linux: `~/.gradle/gradle.properties`
   - Windows: `%USERPROFILE%\\.gradle\\gradle.properties`

2. Add your key:
   ```properties
   GEMINI_API_KEY=AIzaSy_YOUR_ACTUAL_KEY_HERE
   ```

3. In `app/build.gradle`, expose it in BuildConfig:
   ```gradle
   android {
       defaultConfig {
           buildConfigField "String", "GEMINI_API_KEY", "\"${project.findProperty("GEMINI_API_KEY") ?: ""}\""
       }
   }
   ```

4. In app code, initialize Gemini with BuildConfig:
   ```kotlin
   val geminiService = GeminiService(BuildConfig.GEMINI_API_KEY)
   ```

5. Sync Gradle and rebuild.

> Do **not** place API keys directly in Kotlin source files.

---

## STEP 6: Connect Your Phone (Optional)

### Enable USB Debugging
1. Go to phone **Settings** → **About Phone**
2. Tap **Build Number** 7 times
3. Back to **Settings** → **Developer Options**
4. Enable **USB Debugging**
5. Plug phone into computer with USB cable
6. Allow USB debugging permission on phone

### Verify Connection
In Android Studio terminal:
```bash
adb devices
```

You should see your phone listed.

---

## STEP 7: Build the APK

### Option A: Android Studio GUI (Easiest)

1. Click **Build** in top menu
2. Select **Build Bundle(s) / APK(s)** → **Build APK(s)**
3. Wait for completion (bottom right: ✅ "Build successful")
4. Click **Locate** to find your APK

**Location**: `app/build/outputs/apk/debug/app-debug.apk`

### Option B: Command Line

```bash
# From project root
./gradlew assembleDebug

# On Windows
gradlew.bat assembleDebug
```

**Wait for**: ✅ "BUILD SUCCESSFUL"

---

## STEP 8: Install on Phone

### Method A: USB Install (Recommended)

```bash
# With phone connected via USB
adb install app/build/outputs/apk/debug/app-debug.apk

# Expected output:
# Success
```

### Method B: Manual Transfer

1. Find APK: `app/build/outputs/apk/debug/app-debug.apk`
2. Copy to USB drive or email to yourself
3. Transfer to phone
4. Open file manager on phone
5. Tap the APK file
6. **Install**

### Method C: Android Studio Install

1. Connect phone via USB
2. Click **Run** → **Run 'app'** (green play button)
3. Select your phone from device list
4. App installs and launches automatically

---

## STEP 9: Launch & Create Your Boss

1. Open **Dungeons Boss** app on your phone
2. Complete the **7-step character creation**:
   - Step 1: Name, race, appearance
   - Step 2: Dungeon setting & floor theme
   - Step 3: Boss power & skills
   - Step 4: Stats (pick tags, not numbers)
   - Step 5: Dungeon voice (Chronicle, Advisor, etc.)
   - Step 6: NSFW options (skip if you want)
   - Step 7: Floor configuration

3. Click **COMPLETE**
4. Chat with the Dungeon! 🗿

---

## Troubleshooting

### "Gradle sync failed"
```bash
# Clean and retry
./gradlew clean
./gradlew build
```

### "API key not found / Invalid"
- ✅ Verify key from https://aistudio.google.com/app/apikey
- ✅ Ensure `GEMINI_API_KEY` exists in `~/.gradle/gradle.properties`
- ✅ Restart Android Studio after editing
- ✅ Rebuild APK

### "Build failed - Kotlin error"
```bash
./gradlew clean
./gradlew build
```

If still fails, check Android Studio version is latest (Help → Check for Updates)

### "Device not recognized"
```bash
# Restart ADB
adb kill-server
adb start-server
adb devices
```

Then reconnect phone.

### "Permission denied on install"
```bash
adb uninstall com.dungeonboss.app
adb install app/build/outputs/apk/debug/app-debug.apk
```

### App crashes on startup
1. Check Logcat (Android Studio bottom panel)
2. Look for red error messages
3. Common cause: Missing API key or invalid key format
4. Rebuild and reinstall

---

## Building a Release APK (For Distribution)

For sharing with friends or production deployment:

```bash
./gradlew assembleRelease
```

Location: `app/build/outputs/apk/release/app-release.apk`

**Note**: Release builds are smaller and faster, but require a signing key. This is for advanced users.

---

## File Structure

After setup, your project looks like:

```
Lustia-new/
├── app/
│   ├── build.gradle                    ← Dependencies
│   ├── src/main/
│   │   ├── AndroidManifest.xml         ← Permissions
│   │   └── java/com/dungeonboss/app/
│   │       ├── MainActivity.kt          ← Entry point
│   │       ├── GameState.kt             ← Save/load
│   │       ├── GeminiService.kt         ← AI chat
│   │       ├── ChatScreen.kt            ← Chat UI
│   │       └── CharacterCreationScreen.kt ← Setup wizard
│   └── build/
│       └── outputs/apk/
│           └── debug/app-debug.apk     ← Final APK ✨
├── build.gradle                        ← Project config
├── README.md                           ← Full documentation
├── SETUP.md                            ← This file
└── local.properties                    ← Local machine config (git-ignored)
```

---

## Quick Reference Commands

```bash
# Clean build
./gradlew clean

# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Run on connected phone
./gradlew installDebug

# Check connected devices
adb devices

# View app logs
adb logcat

# Uninstall app
adb uninstall com.dungeonboss.app
```

---

## Next Steps

After successful installation:

1. **Create your first boss** - Take time with the character creation
2. **Explore the chat** - Test different actions and dialogue
3. **Check your stats** - Type `#status` in chat
4. **Save your story** - Game autosaves after each message
5. **Share with friends** - Send them your APK file

---

## FAQ

**Q: Do I need internet to play?**  
A: Only for Gemini API calls (the chat responses). Character creation and game state are all local.

**Q: Is my data safe?**  
A: Yes! Everything saves locally on your phone. Nothing except chat messages go to Google's servers.

**Q: Can I use this on iOS?**  
A: Not without modifications. This is Android-specific. You'd need to rewrite in Swift/SwiftUI.

**Q: Will this work on older phones?**  
A: Minimum Android 8.0 (API 26). Older devices won't work.

**Q: How much data does it use?**  
A: ~1MB per hour of chatting (API calls only). Game saves are ~100KB.

**Q: Can I run multiple bosses?**  
A: Currently one boss at a time. To switch bosses, uninstall and rebuild with different character data, or modify `GameState.kt` to support multiple profiles.

---

## Getting Help

1. **Check the README.md** - Full documentation
2. **Review Logcat** - Android Studio bottom panel shows detailed errors
3. **Clean and rebuild** - Fixes 80% of issues
4. **Verify API key** - Most common problem
5. **Update Android Studio** - Helps → Check for Updates

---

**You're all set! The Dungeon awaits.** 🗿

> *The torches have been burning for ten thousand years.*  
> *It has been waiting for you.*
