SmartFridge

A simple Android app demonstrating a smart fridge prototype (Firebase-backed). This repository contains the Android app module and related Gradle configuration.

## Features
- Firebase Realtime Database integration
- User authentication (Firebase Auth)
- Basic UI using Material components

## Prerequisites
- JDK 11
- Android Studio (Electric Eel or later recommended)
- Android SDK for API level 36
- A Firebase project with google-services.json placed in app/

## Quick setup
1. Clone the repo:
   git clone <repo-url>

2. Open the project in Android Studio.

3. Place your Firebase config:
   - Add your `google-services.json` into `app/`.

4. Update dependencies if you use a version catalog (libs.versions.toml) or replace aliases with explicit versions in `app/build.gradle.kts`.

## Firebase notes
- This project uses the Firebase BoM for version management. Ensure your `app/build.gradle.kts` includes:
  - `implementation(platform("com.google.firebase:firebase-bom:<version>"))`
  - `implementation("com.google.firebase:firebase-auth-ktx")`
  - `implementation("com.google.firebase:firebase-database-ktx")`
- Configure Authentication and Realtime Database rules in the Firebase console as needed.

## Common commands
- Build: ./gradlew assembleDebug
- Run unit tests: ./gradlew test
- Run instrumentation tests: ./gradlew connectedAndroidTest
- Clean: ./gradlew clean

## Troubleshooting
- compileSdk errors: ensure `compileSdk = 36` is set in `app/build.gradle.kts`.
- Dependency issues: verify your version catalog (`gradle/libs.versions.toml`) or replace `libs.*` aliases with explicit coordinates.

## Contributing
Feel free to open issues or pull requests. Keep changes small and focused.

## License
Specify your preferred license here (e.g., MIT). If none, add one or replace this section.


