# IntentOS

An Android application built with Kotlin and a Python backend. The project name — **IntentOS** — suggests a task/goal/intent-management system, and the architecture reflects a full-stack mobile app: a native Android client communicating with a Python-powered API server.

---

## Repository Structure

```
IntentOS-App/
├── app/                    # Android application (Kotlin)
├── backend/                # Python backend/API server
├── gradle/                 # Gradle version catalog (libs.versions.toml)
├── build.gradle.kts        # Root Gradle build file
├── settings.gradle.kts     # Project settings — root project: "IntentOS"
├── gradle.properties
├── gradlew / gradlew.bat
└── .gitignore
```

---

## Tech Stack

### Android App (`app/`)
- **Language:** Kotlin (~78% of the codebase)
- **Build System:** Gradle with Kotlin Script (`.kts`) and Version Catalog (`libs.versions.toml`)
- **Dependency Injection:** Hilt (`com.google.dagger:hilt-android`)
- **Code Generation:** KSP (Kotlin Symbol Processing)

### Backend (`backend/`)
- **Language:** Python (~22% of the codebase)
- Likely a REST/HTTP API server consumed by the Android client

---

## Prerequisites

### Android
- Android Studio (Hedgehog or newer recommended)
- Android SDK
- JDK 11+

### Backend
- Python 3.8+
- `pip`

---

## Getting Started

### 1. Clone the repository
```bash
git clone https://github.com/Bhargav-SL69/IntentOS-App.git
cd IntentOS-App
```

### 2. Set up and run the backend
```bash
cd backend
pip install -r requirements.txt   # if present
python main.py                     # or app.py / server.py
```

### 3. Open the Android project
Open the root `IntentOS-App/` folder in Android Studio. Gradle sync will download all dependencies automatically.

### 4. Configure the backend URL
Update the base URL in the Android app to point to your backend server (local IP or deployed URL) before building.

### 5. Build and run
Run the app on an emulator or connected device via Android Studio, or from the command line:
```bash
./gradlew assembleDebug
```

---

## Gradle Plugins Used

| Plugin | Purpose |
|---|---|
| `android.application` | Android app module |
| `kotlin.android` | Kotlin support for Android |
| `hilt` | Hilt dependency injection |
| `ksp` | Kotlin Symbol Processing (for Hilt, Room, etc.) |

---

## Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/your-feature`
3. Commit your changes: `git commit -m "Add your feature"`
4. Push to the branch: `git push origin feature/your-feature`
5. Open a Pull Request

---

## License

No license file is currently included in this repository.
