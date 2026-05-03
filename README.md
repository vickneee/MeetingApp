# Meeting App - MeetUp

Meeting App is a collaborative tool designed to simplify group decision-making. Whether you're planning a casual hangout or a formal gathering, our app helps groups agree on the best time and place with ease.

## App Description

This app is designed for creating plans for events and gatherings.

Users can create event plans for a group, where participants can:
- Vote for the best time
- Vote for the best place

The goal is to make group decision-making simple and fast, so everyone can agree on when and where to meet.

## Screenshots
| Home Screen |              Event Create              |             Event Created             |
| :---: |:--------------------------------------:|:-------------------------------------:|
| ![Home](docs/screenshots/home.png) | ![Create](docs/screenshots/create.png) | ![Vote](docs/screenshots/created.png) |

|            Participant Screen             |             Host Screen              | Voting |
|:-----------------------------------------:|:------------------------------------:| :---: |
| ![Home](docs/screenshots/participant.png) | ![Create](docs/screenshots/host.png) | ![Vote](docs/screenshots/vote.png) |

## Features
- **Event Creation**: Host can create events and invite others via unique codes.
- **Management Dashboards**: Dedicated Host and Participant views to track event status, voting progress, and final results.
- **Collaborative Voting**: Participants vote for their preferred time slots and locations.
- **Google Places Integration**: Search and view details for restaurants and venues, including photos and opening hours.
- **Location Awareness**: Automatic distance calculation from your current location to venues using GPS.
- **Real-time Sync**: Powered by Firebase Firestore for seamless multi-user collaboration.
- **Offline Support**: Local caching with Room database.

## Tech Stack
- **UI**: Jetpack Compose (100% Kotlin)
- **Navigation**: Jetpack Compose Navigation
- **Networking**: Retrofit & OkHttp
- **Database**: Room (Local), Firebase Firestore (Cloud)
- **Authentication**: Firebase Anonymous Auth
- **Static Analysis**: Detekt & Ktlint
- **Documentation**: Dokka (V2)

## Setup

### Prerequisites
- Android Studio 
- JDK 17
- Android emulator or device running API 26+

1. **Firebase Setup**:
    - Follow the detailed [Firebase Setup Guide](./FIREBASE.md) to add your `google-services.json`.
    - **Note:** Ensure `app/google-services.json` is ignored by git to keep your project keys private.

2. **Google Places API**:
    - Go to the [Google Cloud Console](https://console.cloud.google.com/).
    - Enable the **Places API (New)** for your project.
    - Create an API Key.
    - Copy `app/src/main/assets/secret.properties.example` to `app/src/main/assets/secret.properties`.
    - Replace `KEY_HERE` with your actual API key:
      ```properties
      PLACES_API_KEY=YOUR_ACTUAL_API_KEY_HERE
      ```

### Building from Source

To build the project from the command line:
```bash
./gradlew assembleDebug
```
To run unit tests:
```bash
./gradlew test
```
To run static analysis and formatting:
```bash
./gradlew detekt

./gradlew ktlintFormat

./gradlew ktlintCheck
```

## Sensors & Permissions
The app uses the following device capabilities:
- **Location (GPS)**: Core sensor used to calculate the distance between you and the proposed meeting places.
- **Notifications**: Used to notify host and participants when an event is finalized or when updates occur.
- **Internet**: Required for Firebase real-time sync and Google Places data.

Ensure you grant **Location** and **Notification** permissions when prompted to enable the full collaborative experience.

## Documentation & Code Style
- **Code Style**: [KTLINT.md](./KTLINT.md)
- **Static Analysis**: [DETEKT.md](./DETEKT.md)
- **Kotlin Dokka Documentation**: To generate the API documentation using Dokka V2, run:
  ```bash
  ./gradlew dokkaGenerateHtml
  ```
  The documentation will be generated in `app/build/dokka/html`.

  Open the link:
  [Dokka Documentation - Coming]()

## License
This project is developed for academic purposes as part of mobile application development project course.