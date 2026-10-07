# WanderSpectra

WanderSpectra is an Android caregiver app with a Wear OS companion for autistic elopement. A caregiver can sign in, save a safe zone, and confirm that a child is missing. Confirming writes an incident. Child is Safe closes that incident.

The app is for a parent or caregiver who needs a clear missing-child flow, not a general family tracker.

## Alpha Features

By the end of October the alpha should include:

- Caregiver login and account creation
- Child profile
- Safe zone save (name, radius, monitoring, location)
- Home button for Child is Missing, confirm screen, and Elopement Mode
- Incident write and Child is Safe close
- Wear OS connection and last known location
- Caregiver notification when Elopement Mode starts
- AI check on the incident path: compare the latest watch point to the saved safe zone and flag a likely elopement before the caregiver confirms. This is required for the AI concentration and is not in the current build yet.

Already working on the feature branch: login, safe zone save, Child is Missing confirm, incident write, and Child is Safe.

## Technologies

- Kotlin
- Jetpack Compose
- Android app module and Wear OS module
- Firebase Authentication
- Cloud Firestore
- Git and GitHub, feature branch off development

No map SDK is hooked up yet. Location is still a test coordinate.

## Ethics, Privacy, and Security

The app stores caregiver accounts, child profiles, safe zones, and incident locations. That is sensitive. A missing-child alert should not be visible to random users.

Passwords are handled by Firebase Authentication. The app does not store the password itself. Firestore rules require a signed-in user for safe zones and incidents. Caregiver documents are limited to that user's id.

`google-services.json` is a client config file. API keys and signing passwords do not belong in the README or in a public commit. The AI check, when added, should run on data the caregiver already saved and should not send a child's live location to a public model.

## Installation

There is no Play Store build yet. A tester needs a debug build from the team.

1. Get the debug APK from the project Drive, or build it with the steps below.
2. Enable install from unknown sources on the Android device.
3. Install the APK and open WanderSpectra.
4. Create an account or sign in.
5. Open Safe Zone Settings to save a zone, or Home, then Child Is Missing, to record an incident.

The Wear OS app is not required for the caregiver demo.

## Development Setup

1. Install Android Studio and an Android phone emulator.
2. Clone the repo and check out `development`, then the feature branch you are working on.
3. Do not commit straight to `main`.
4. Open the repo root in Android Studio and let Gradle sync.
5. Run the `app` configuration, not `wear`, for the phone demo.
6. Firebase access has to be granted by the project owner. A new Firebase project will not match the checked-in `google-services.json`.

## License

MIT

## Contributors

- Destin Champlin: project lead, app shell, login, onboarding, Figma
- George Irby: safe zone model and save, Child is Missing flow, incident write and close
- Logan Martinelli: Wear OS
- Rafael Ramos: Safety Circle and community alerts
- Brandon Thoreau: team contributor

Destin maintains the GitHub repo and Firebase project.

## Project Status

Alpha. The caregiver missing flow works on a feature branch and is not merged to development yet, because AppShell and Home had to change. Map picker, phone notification, and the AI zone check are still open.