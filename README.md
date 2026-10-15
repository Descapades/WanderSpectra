# WanderSpectra

## Introduction

WanderSpectra is an Android caregiver application with a Wear OS companion designed to help families respond when an autistic child wanders away from a designated safe area.

The application provides caregivers with access to their child's profile, wearable location information, safe zone settings, emergency contacts, and incident history.

## Features

### Implemented or Integrated
- Caregiver login and account creation
- Caregiver Home Dashboard
- Child profile management
- Safe zone settings and storage
- Child Is Missing confirmation screen
- Elopement Mode and Child Is Safe functionality
- Incident creation and closure using Firebase Firestore
- Incident History interface
- Safety Circle management interface
- Emergency Profile interface
- Community Alert configuration and recipient interfaces
- Wearable Location Map with test location data
- Wear OS companion development

### In Progress
- Full wearable location synchronization
- Community Alert integration and notifications
- Emergency notification functionality
- AI-based safe zone monitoring and elopement detection
- Additional testing and feature integration

## Technologies

- Kotlin
- Jetpack Compose
- Android and Wear OS
- Firebase Authentication
- Cloud Firestore
- Google Maps
- Git and GitHub
- Android Studio

## Installation

WanderSpectra is currently in alpha development.

To install a current Android development build:

1. Obtain the current WanderSpectra APK provided by the project team.
2. Open the APK on a compatible Android device.
3. Follow the Android installation prompts.
4. Launch WanderSpectra after installation.
5. Create a caregiver account or sign in with an existing account.

## Privacy and Security

WanderSpectra handles sensitive caregiver, child, and location information.

Firebase Authentication manages account credentials, while Firestore security rules restrict access to protected data.

The application is designed to prioritize caregiver control, child safety, and responsible handling of location information.

## Development Setup

1. Install Android Studio.
2. Clone the WanderSpectra repository.
3. Check out the `development` branch.
4. Open the project in Android Studio.
5. Allow Gradle to sync.
6. Run the `app` configuration on an Android emulator or device.

Firebase access must be configured for the project.

To create a debug build from the repository on Windows, run:

```powershell
.\gradlew.bat :app:assembleDebug
```

A successful debug build creates the APK at:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Contributors

- **Destin Champlin:** Project lead, application integration, caregiver dashboard, wearable location map, authentication, and UI/UX design
- **George Irby:** Safe zone functionality, missing child workflow, and incident management
- **Logan Martinelli:** Wear OS development
- **Rafael Ramos:** Safety Circle, Emergency Profile, Incident History, and Community Alert interfaces
- **Brandon Thoreau:** Team contributor

## Project Status

**Alpha Development | October 2026**

Core caregiver functionality and multiple team features have been integrated into the `development` branch.

The application builds successfully, with additional work remaining on wearable synchronization, notifications, community alerts, and AI-based elopement detection.

## License

MIT
