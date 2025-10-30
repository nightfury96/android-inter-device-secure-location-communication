# **Inter-Device Secure Communication – Location & Internet Apps**

### 📱 Overview

This project simulates a **secure communication system between two Android applications**:

- **Location App** — collects and stores user location securely in the background.
- **Internet App** — sends commands to the Location App and displays received responses.

The focus is on **Clean Architecture**, **security**, **reliability**, **testability**, and *
*scalable modular design** — rather than complex UI.

---

## 🧩 Architecture Overview

Both applications follow **Clean Architecture** principles with strict layer separation.

### **🗺 Location App (MVVM)**

Implements multi-module Clean Architecture:
location-app/

├ location-domain/ # Business rules, interfaces, use cases

├ location-data/ # Repository implementations, encrypted Room DB, DataStore, Tink

├ location-presentation/ # ViewModels and UI state management (MVVM)

├ location-app/ # Application module (Activity, ForegroundService, Worker, DI setup)

├ shared-logger/ # Shared module for structured logging

└ shared-models/ # Shared module for data models (LocationRecord, Result, etc.)

- **Domain layer** defines `LocationRepository`, `SecureStorage`, and use cases.
- **Data layer** implements these interfaces using Room, SQLCipher, and Tink for encryption.
- **Presentation layer** contains only ViewModels and testable UI logic.
- **App module** hosts:
    - `LocationForegroundService` for background tracking
    - `BootWorker` for auto-restart and network recovery
    - `ServiceSchedulerImpl` (Android-aware service starter)
    - DI setup using **Hilt**

### **🌐 Internet App**

Implements **MVI** in a single module with clear separation of packages:
internet-app/

├ domain/ # Commands and repository interfaces

├ data/ # Command repository implementation, ContentProvider access

└ presentation/ # MVI ViewModel, State, and UI

Uses **Koin** for dependency injection.

---

## 🧠 Design & Reasoning

| Design Aspect                         | Reasoning                                                                                                         |
|---------------------------------------|-------------------------------------------------------------------------------------------------------------------|
| **Clean Architecture (multi-module)** | Enforces separation of concerns, improves testability and modularity                                              |
| **Hilt / Koin**                       | Hilt for Location App to simplify Worker & Service injection; Koin for Internet App to demonstrate DI versatility |
| **SQLCipher + Tink + KeyStore**       | Encrypts all user location data and persistent keys using secure industry standards                               |
| **DataStore over SharedPreferences**  | Asynchronous, type-safe, and modern key-value storage                                                             |
| **BootWorker + WorkManager**          | Ensures background service resilience across reboot, connectivity changes, or process kills                       |
| **ContentProvider (signature-level)** | Secure inter-app communication restricted to apps signed with the same certificate                                |
| **Coroutines + Flow**                 | Clean, reactive asynchronous design ensuring testable and non-blocking updates                                    |
| **Structured Logging**                | Shared logger module provides unified event tracking across both apps                                             |

---

## 🔐 Security Overview

| Component                   | Security Mechanism                                                                                                                                                  |
|-----------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Room Database**           | Encrypted using SQLCipher (`SupportFactory(passphrase)`)                                                                                                            |
| **Passphrase Storage**      | Generated randomly, encrypted via **Tink AEAD**, and stored in **DataStore** protected by Android KeyStore                                                          |
| **Service Status**          | Encrypted boolean in DataStore using same Tink AEAD key                                                                                                             |
| **Key Management**          | `SecureKeyManager` uses `AndroidKeysetManager` with master key URI `android-keystore://datastore_master_key`                                                        |
| **Inter-App Communication** | Custom `ContentProvider` secured with `<permission android:protectionLevel="signature" />` and custom permission `me.nightfury.permission.ACCESS_LOCATION_PROVIDER` |
| **Foreground Service**      | Declared with `android:foregroundServiceType="location"` and requires runtime-granted permissions                                                                   |
| **Runtime Permissions**     | Foreground + background + notifications managed dynamically (Android 10–15 compatible)                                                                              |

---

## ⚙️ Background Service Lifecycle

The `LocationForegroundService` runs continuously to collect location every minute and persists
encrypted data in the local DB.

### **Service Recovery**

- **Reboot / OS Kill:**  
  `BootWorker` (triggered by `WorkManager`) checks service status from encrypted DataStore and
  restarts it automatically.
- **Network Loss / Reconnect:**  
  The same worker has a `NetworkType.CONNECTED` constraint and re-triggers when connectivity is
  restored.
- **Manual Restart:**  
  User can start/stop service from either app (Location or Internet).

---

## 🔄 Inter-App Communication

### Mechanism

The **Internet App** communicates with the **Location App** through a **ContentProvider** secured by
a signature-level permission.

**Location App Provider:**

```xml

<provider android:name=".provider.LocationProvider"
    android:authorities="me.nightfury.locationapp.provider.LocationProvider" android:exported="true"
    android:permission="me.nightfury.permission.ACCESS_LOCATION_PROVIDER" />

    <!--Permission (Location App Manifest):-->
<permission android:name="me.nightfury.permission.ACCESS_LOCATION_PROVIDER"
android:protectionLevel="signature" />

    <!--Internet App Manifest:-->
<uses-permission android:name="me.nightfury.permission.ACCESS_LOCATION_PROVIDER" />
```

## Commands Supported:

	•	Start Service
	•	Stop Service
	•	Retrieve All Locations
	•	Retrieve Latest Location

Each command returns structured results or acknowledgments serialized from shared model classes.

## 📈 Architecture & Communication Flow Diagram

              ┌───────────────────────────────────────────────────┐
              │                    INTERNET APP                   │
              │───────────────────────────────────────────────────│
              │  MVI Architecture                                 │
              │  • View (XML/UI)                                  │
              │  • ViewModel (State Reducer)                      │
              │  • Repository (ContentProvider client)            │
              │  • Koin DI Container                              │
              │                                                   │
              │  ┌────────────────────────────┐                   │
              │  │ Send Commands:             │                   │
              │  │  • START_SERVICE           │                   │
              │  │  • STOP_SERVICE            │                   │
              │  │  • GET_LOCATIONS           │                   │
              │  │  • GET_LATEST_LOCATION     │                   │
              │  └──────────────┬─────────────┘                   │
              └────────────────┬┴─────────────────────────────────┘
                               │   (Signature-secured ContentProvider)
                               ▼
              ┌───────────────────────────────────────────────────┐
              │                    LOCATION APP                   │
              │───────────────────────────────────────────────────│
              │  MVVM Architecture                                │
              │  • ForegroundService + BootWorker                 │
              │  • Repository (SQLCipher+ ROOM + DataStore + Tink)│
              │  • SecureKeyManager (KeyStore)                    │
              │  • Hilt DI Container                              │
              │  • Shared Logger + Models                         │
              └───────────────────────────────────────────────────┘

## 🧪 Testing

### **Location App**

• LocationViewModelTest
• ✅ Verifies Flow collection updates UI state
• ✅ Verifies startLocationService() updates repository and state
• ✅ Verifies stopLocationService() updates repository and state
• ✅ Verifies error handling during start/stop operations

Testing is focused on the **presentation layer (ViewModel)** and its interaction with domain use
cases.  
The `LocationViewModelTest` ensures correct state updates, service management, and error handling
using coroutine and flow-based tests.

#### **🧩 Testing Frameworks and Tools**

| Tool / Library              | Purpose                                                  |
|-----------------------------|----------------------------------------------------------|
| **JUnit 4**                 | Core unit testing framework                              |
| **MockK**                   | Mocking and stubbing of use cases                        |
| **app.cash.turbine**        | Reactive testing of Kotlin Flows                         |
| **kotlinx-coroutines-test** | Deterministic coroutine and dispatcher control           |
| **NoOpLogger**              | Simplifies log verification without Android dependencies |

---

#### **🧠 Test Design Philosophy**

The tests isolate the `LocationViewModel` from Android components and real repositories.  
Mocked use cases (`LocationDataUseCase` and `ManageLocationWorkerUseCase`) simulate domain behavior,
ensuring ViewModel logic is validated independently from frameworks.

All tests use a `StandardTestDispatcher` to control coroutine scheduling and avoid race
conditions.  
Each test follows a **Given → When → Then** pattern.

---

### **✅ Test Cases Summary**

| Test ID    | Scenario                                                        | Expected Behavior                                                                                         |
|------------|-----------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------|
| **TEST 1** | `uiState updates when new locations emitted`                    | Verifies that the ViewModel correctly collects and reflects `Flow` emissions from the repository.         |
| **TEST 2** | `startLocationService starts service and updates state`         | Ensures that starting the location service invokes the correct use case and updates UI state accordingly. |
| **TEST 3** | `stopLocationService stops service and updates state`           | Confirms service stop behavior and UI state updates.                                                      |
| **TEST 4** | `startLocationService sets error message when exception thrown` | Validates robust error handling and fallback message updates when use cases throw exceptions.             |

### **Internet App**

• Tested manually for inter-app communication and state rendering (MVI reducer behavior).

---

## 🧰 CI/CD (Planned Configuration)

A GitHub Actions workflow will:

1. Checkout code
2. Setup JDK 17 + Gradle
3. Build both apps
4. Run all tests (./gradlew test)
5. Generate test reports (build/reports/tests/test/index.html)
6. Fail pipeline if any test or build fails

## 🏗️ Building & Running

### 1️⃣ Prerequisites

	•	Android Studio Ladybug (or newer)
	•	JDK 17
	•	Gradle 8+
	•	Min SDK 21
	•	Target SDK 36
	•	Both apps signed with the same debug or release key (required for signature permission)

### 2️⃣ Build

Run from root project:

```shell
./gradlew clean assembleDebug
```

### 3️⃣ Install

Install both APKs:

```shell
adb install location-app/build/outputs/apk/debug/location-app-debug.apk
adb install internet-app/build/outputs/apk/debug/internet-app-debug.apk
```

⚠️ The Location App must be installed before the Internet App to allow provider access.

### 4️⃣ Run

Location App UI:
• Start / Stop / Clear location collection
• Displays stored (encrypted) locations

Internet App UI:
• Send commands:
• ▶ Start Service
• ⏹ Stop Service
• 📜 Get All Locations
• 📍 Get Latest Location

Responses are displayed in the Internet App and logged via AppLogger.
⚠️ The Location App must be run for the first time before the Internet App to allow permissions.

### 📜 Logging

Both apps share a common logger (shared-logger module):

```kotlin
AppLogger.i("LocationRepositoryImpl", "Location saved successfully")
AppLogger.e("BootWorker", "Service restarted after reboot")
```

Logs are viewable in Logcat with unified tag formatting.

### 🧩 Future Improvements

• Implement CI/CD with test reporting and static analysis
• Add integration tests simulating real inter-app communication
• Enhance metrics tracking and analytics (mock instrumentation)
• Add Jetpack Compose UI for modern presentation layer

Author: Farzad Mirdamadi
Target Role: Senior Android Developer
Goal: Demonstrate secure, testable, and modular Android app architecture with real inter-app
communication.

