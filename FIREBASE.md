# Firebase Setup - Anonymous Authentication and Firestore Database

Follow these steps to connect Firebase to the Android app.

---
### 1. Create Firebase Project
1. Go to https://console.firebase.google.com
2. Click “Create a new Firebase Project”
3. Enter your project name (e.g. Meeting App)
4. Continue → Disable Google Analytics (optional)
5. Click Create project

---
### 2. Add Android App
1. Click Add App → Android
2. Enter Android package name: 
•	Package name → must match your app (e.g. com.meetup.meetingapp)
3. Enter App nickname (e.g. Meeting App) (optional)
4. Click Register App

---
### 3. Download and then add Config File
1. Download google-services.json
2. Click next
3. Place it in the app folder:

app/google-services.json

---
### 4. Add Firebase SDK to Project

In root-level build.gradle.kts (project-level):

```kotlin
plugins {
  // ...

  // Add the dependency for the Google services Gradle plugin
  id("com.google.gms.google-services") version "4.4.4" apply false
}
```

In build.gradle (Module: app-level):

```kotlin
plugins {
   // Add the Google services Gradle plugin
   id("com.google.gms.google-services")
}

dependencies {
   // Import the Firebase BoM
   implementation(platform(libs.firebase.bom))

   // When using the BoM, don't specify versions in Firebase dependencies
   implementation(libs.firebase.analytics)

   // Firebase Auth and Firestore
   implementation(libs.firebase.auth)
   implementation(libs.firebase.firestore)
   // Add the dependencies for any other desired Firebase products
   // https://firebase.google.com/docs/android/setup#available-libraries
}
```

In gradle folder in libs.versions:

```kotlin
[versions]
// ...
// Add the Firebase BoM
firebaseBom = "34.11.0"
```

After adding dependencies to build.gradle.kts and gradle folder in libs.versions, sync project.

Next steps:

Continue to console

---
### 5. Enable Authentication (Anonymous)
1. Go to Security -> Authentication
2. Click Get started
3. Open Sign-in method
4. Enable Anonymous
5. Click Save

---
### 6. Setup Firestore Database
1. Go to Databases & Storage -> Firestore
2. Click Create database
3. Select edition -> Standard edition
4. Click Next
5. Database ID & location
6. Click Next
7. Select Start in test mode

---
### 7. Firestore Rules (for development)
```shell
rules_version = '2';

service cloud.firestore {
   match /databases/{database}/documents {
      match /{document=**} {
         allow read, write: if request.time < timestamp.date(2026, 5, 31);
      }
   }
}
```

---
### 8. Test Authentication

```kotlin
class HomeViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
   
    /**
     * Sign in anonymously to the Firebase Realtime Database
     */
    fun signInAnonymously() {
        if (auth.currentUser != null) return

        auth.signInAnonymously()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    println("SUCCESS UID: ${user?.uid}")
                } else {
                    println("ERROR: ${task.exception}")
                }
            }
    }
}

```
```kotlin
fun HomeScreen(
    viewModel: HomeViewModel = viewModel()
) {
    /**
     * Sign in anonymously to the Firebase Realtime Database
     */
    LaunchedEffect(Unit) {
        viewModel.signInAnonymously()
        Log.d("HomeScreen", "Signed in anonymously")
    }
}

```

---

**Notes**
-	Works on emulator and real device
-	Internet connection required
-	Test mode is OK for school project
-	Remember to update Firestore rules before expiration
