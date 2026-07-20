# Lustia-new

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
1. Visit https://aistudio.google.com/app/apikey
2. Click **Get API Key**
3. Create a new API key
4. Copy the key

#### 2. Add API Key Privately (Required)
Create or edit `local.properties` in the project root and add exactly one key:

```properties
GEMINI_API_KEY=AIzaSy_your_actual_key_here
```

> Keep this file private. Do not commit API keys to Git.

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

## 🔐 Security Notes
- The app reads your key from `BuildConfig.GEMINI_API_KEY`.
- Do not hardcode API keys in source files.
- Do not paste API keys into docs, screenshots, issues, or commit messages.

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
