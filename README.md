# 🗿 I FELL IN LOVE WITH A DUNGEON BOSS - Android Chatbot

A local Android chatbot powered by Google Gemini AI, fully playable on your phone. Create your Dungeon Boss character and chat with an ancient, sentient dungeon system.

## 📋 Features

✅ **7-Step Character Creation** - Define your boss, powers, skills, techniques, and stats  
✅ **Persistent Game State** - Your boss and story are saved locally  
✅ **Gemini AI Integration** - Chat with the Dungeon's voice (Chronicle, Advisor, Witness, Fondly Tired)  
✅ **Dark Themed UI** - Beautiful Jetpack Compose interface  
✅ **No Server Needed** - Runs entirely on your phone (Internet only for Gemini API calls)

## 🚀 Quick Start

### Prerequisites
- Android Studio (latest)
- Android SDK 26+ 
- Gemini API key from [Google AI Studio](https://aistudio.google.com/app/apikey)

### Setup Steps

#### 1. Get Your Gemini API Key
```bash
1. Visit https://aistudio.google.com/app/apikey
2. Click "Get API Key"
3. Create a new API key
4. Copy the key
```

#### 2. Store API Key for Local Development

**Option A — project-local (recommended):**
Copy `secrets.properties.example` to `secrets.properties` (git-ignored) and fill in your key:
```properties
geminiApiKey=AIzaSy_YOUR_ACTUAL_KEY_HERE
```

**Option B — machine-wide:**
Add to `~/.gradle/gradle.properties` (macOS/Linux) or `%USERPROFILE%\.gradle\gradle.properties` (Windows):
```properties
geminiApiKey=AIzaSy_YOUR_ACTUAL_KEY_HERE
```

The build reads the key via the Gradle property `geminiApiKey` and exposes it as `BuildConfig.GEMINI_KEY`.

> Never commit your real key to source files.

#### 3. Build the APK
```bash
# From the project root:
./gradlew assembleDebug

# Or from Android Studio:
# Build → Build Bundle(s)/APK(s) → Build APK(s)
```

#### 4. Install on Your Phone
```bash
# Find the APK:
# app/build/outputs/apk/debug/app-debug.apk

# Install via USB:
adb install app/build/outputs/apk/debug/app-debug.apk

# Or manually transfer the APK and tap to install
```

## 🎮 How to Play

### First Launch
1. **Create Your Boss** - Answer the 7-step wizard:
   - Step 1: Who are you? (Name, race, age, height, gender, appearance)
   - Step 2: Setting & Floor Theme
   - Step 3: Your Boss Power & Skills
   - Step 4: Stats (tags, not numbers)
   - Step 5: Dungeon Voice (Chronicle, Advisor, Witness, Fondly Tired)
   - Step 6: NSFW Options (optional)
   - Step 7: Floor Configuration

2. **Chat with the Dungeon** - Speak to the ancient stone:
   - Type your boss's actions or dialogue
   - Watch the Dungeon respond with narrative
   - Your story is saved automatically

### Commands (Type these in chat)
- `#status` - See your boss's current state
- `#coins` - Check your Dungeon Coins
- `#infamy` - View your Infamy level
- `#bonds` - See relationships with NPCs
- `#story` - Review the last 7 days

## 📁 Project Structure

```
app/src/main/java/com/dungeonboss/app/
├── MainActivity.kt              # Entry point, navigation
├── GameState.kt                 # Local persistence (SharedPreferences)
├── GeminiService.kt             # Gemini API integration
├── ChatScreen.kt                # Chat UI (Jetpack Compose)
└── CharacterCreationScreen.kt   # 7-step character wizard

app/build.gradle                 # Dependencies & Compose setup
app/src/main/AndroidManifest.xml # Internet permissions
```

## 🔧 Configuration

### API Key Management

**Single source of truth:** the Gradle property `geminiApiKey`.

| Context | How to supply the key |
|---------|----------------------|
| Local (project) | Copy `secrets.properties.example` → `secrets.properties`, set `geminiApiKey=YOUR_KEY` |
| Local (machine-wide) | Add `geminiApiKey=YOUR_KEY` to `~/.gradle/gradle.properties` |
| CI (GitHub Actions) | Add a repository secret named `GEMINI_API_KEY`; the workflow passes it as `-PgeminiApiKey=...` |

The key is baked into the APK as `BuildConfig.GEMINI_KEY` and accessed via `GeminiService(BuildConfig.GEMINI_KEY)`.
The build **fails immediately** if `geminiApiKey` is missing or empty.

### Customizing the Dungeon's Voice
Edit `GeminiService.kt` line 41-62 to change the system prompt and dungeon personality.

### Changing UI Colors
Edit `ChatScreen.kt` and `CharacterCreationScreen.kt` - search for `Color(0xFF...)` hex values.

## 📱 Device Requirements

- **Minimum API**: 26 (Android 8.0)
- **Target API**: 34 (Android 14)
- **RAM**: 2GB+ recommended
- **Storage**: ~50MB for app + local data
- **Internet**: Required for Gemini API calls only

## 🛠️ Building Release APK

```bash
# Build optimized release APK
./gradlew assembleRelease

# Or use Android Studio:
# Build → Build Bundle(s)/APK(s) → Build APK(s) → Release
```

Release APKs are smaller and faster. Located at:
```
app/build/outputs/apk/release/app-release.apk
```

## 🐛 Troubleshooting

### "API key is invalid"
- ✅ Verify the key from [AI Studio](https://aistudio.google.com/app/apikey)
- ✅ Ensure `geminiApiKey` is set in `secrets.properties` or `~/.gradle/gradle.properties`
- ✅ Sync Gradle / restart app after updating

### "Internet permission denied"
- ✅ Check `AndroidManifest.xml` has `<uses-permission android:name="android.permission.INTERNET" />`
- ✅ Grant internet permission when app starts

### "Failed to build"
```bash
# Clean and rebuild
./gradlew clean
./gradlew assembleDebug
```

### App crashes on character creation
- ✅ Ensure all required fields are filled
- ✅ Check Kotlin version matches `build.gradle` (1.9.10)
- ✅ Update Android Studio to latest version

## 📚 Game Data

### Saved Locally
- Boss stats and appearance
- Game state (HP, stamina, coins, infamy)
- Story log (last 7 days)
- NPC bonds and relationships
- Minion roster

**Storage**: `SharedPreferences` in `GameState.kt`  
**Location**: `/data/data/com.dungeonboss.app/shared_prefs/`  
**Format**: JSON serialized

### Clearing All Data
```kotlin
gameState.resetGame() // Clears everything
```

## 🔐 Privacy & Security

- ✅ **All game data stored locally** - Nothing sent to servers except Gemini API calls
- ✅ **No accounts needed** - Play offline after initial setup
- ✅ **Internet only for Gemini** - Chat responses require API calls
- ✅ **API key not stored in source** - Keep it in user-level Gradle properties

## 📖 Character Creation Guide

### Step 1: Who Are You?
Example:
```
Name: Vex the Shadowborn
Race: Half-dragon
Age: 347
Height: 7'6"
Gender: Non-binary
Appearance: Obsidian scales with silver accents, amber eyes
```

### Step 3: Your Power
Example:
```
Boss Power: Void Anchor - Can trap souls in stone for 1000 years
Skill 1: Soul Binding
Skill 2: Floor Sensing
Skill 3: Trap Crafting

Technique 1: The Eternal Prison
Technique 2: Stone Erosion
Technique 3: Shadow Emergence
```

### Step 4: Stats
Pick ONE from each row. Examples:
```
Size: Towering
Physique: Muscular
Resilience: Hardened
Willpower: Driven
Charisma: Magnetic
Deception: Masterful
```

## 🎨 Customization

Want to tweak the game? Key files:

- **Chat colors**: `ChatScreen.kt` lines 100-200
- **Character creation fields**: `CharacterCreationScreen.kt`
- **Dungeon personality**: `GeminiService.kt` lines 41-62
- **Game rules/mechanics**: `GameState.kt`

## 📝 Example Gameplay

```
YOU: I rise from my throne, stone cracking beneath my feet.

DUNGEON:
The floor trembles at your rising. Your domain answers—stone grinds 
against stone in a sound like mountains breathing. Around you, the 
torches flare brighter, as if the air itself recognizes your movement.

Ten thousand years it has held you here. Ten thousand years it waits 
for them to come again.

Today, there is a knock at the door.
```

## 🚢 Deployment Checklist

- [ ] Ensure `geminiApiKey` is set in `secrets.properties` or `~/.gradle/gradle.properties` (local) or `GEMINI_API_KEY` GitHub secret (CI)
- [ ] Test on multiple Android devices
- [ ] Verify internet permission is granted
- [ ] Test character creation through all 7 steps
- [ ] Verify game state persists after restart
- [ ] Check battery/data usage in extended play
- [ ] Build release APK with proguard enabled

## 📞 Support

If you encounter issues:
1. Check the **Troubleshooting** section above
2. Verify your Gemini API key is valid
3. Check Android Studio Logcat for error messages
4. Ensure you're on API 26+ device
5. Try: `./gradlew clean && ./gradlew assembleDebug`

## 📄 License

This project uses the "I Fell in Love with a Dungeon Boss" game system by [Original Creator].  
Android implementation with Gemini AI integration.

---

**The Dungeon is waiting.**  
*It has been waiting for ten thousand years.*  
*It can wait a little longer.*

> "Who are you?"
