# Sensify: AI-Driven Sensor Toolkit for Android 📱

[![GitHub Sponsors](https://img.shields.io/badge/Sponsor--Me-red?style=for-the-badge&logo=github)](https://github.com/sponsors/yoosh0203)
[![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com/)
[![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![License](https://img.shields.io/badge/License-GPLv3-blue.svg)](LICENSE)

> [!NOTE]
> **📢 Localized Version:** [한국어 버전(Korean Version)은 여기서 확인하실 수 있습니다.](./README_ko.md)

**Sensify** is an all-in-one sensor toolkit that maximizes the potential of your Android device's hardware. It goes beyond simple utilities by providing a visually stunning **Neo-Glassmorphism** UI and a robust architectural foundation built with modern Android trends.

---

## 💎 The Core Pillars (Project Philosophy)

1.  **Aesthetic Excellence**: Designed with a premium "Neo-Glassmorphism" language, ensuring a modern look that feels like a flagship experience.
2.  **Hardware Optimization**: Intelligently polls device sensors and implements **Graceful Degradation** for devices lacking specific hardware.
3.  **Real-time Interaction**: High-performance data visualization at near 60fps with haptic feedback for a tactile, physical user experience.

---

## ✨ Feature Deep Dive

### 🧭 Navigation & Precision
-   **Compass & Altimeter**: Uses `TYPE_ROTATION_VECTOR` for high-precision orientation and `TYPE_PRESSURE` for accurate sea-level altitude calculations.
-   **Bubble Level & Protractor**: Leverages the accelerometer for precise leveling. The **Protractor** features a camera overlay with circular guides and a 'Zeroing' function for relative angle measurement.

### 🔊 Environment & Sound Analysis
-   **Sound Meter (Decibel)**: Real-time analysis of microphone input. Displays a **time-axis based dB waveform** instead of just basic FFT graphs. Includes a vibration alert when noise exceeds 85dB.
-   **Metal Detector**: Utilizes the magnetometer to detect electromagnetic fields. Features real-time graph visualization and haptic intensity feedback.
-   **Light & Color Meter**: Measures ambient lux and uses CameraX to analyze real-time RGB values and color codes from the environment.

### ❤️ Wellness & Health
-   **Heart Rate Monitor**: Uses **CameraX** to detect blood flow changes (PPG) in your fingertip. Unlike many apps, it features a **Continuous Measurement Mode** and records periodic BPM snapshots into history.
-   **Pedometer**: Efficiently tracks steps using `TYPE_STEP_COUNTER` (system-level) ensuring minimal battery drain even in the background.

### 📶 Wireless & Connectivity
-   **WiFi/Bluetooth Analyzer**: Scans and visualizes signal strength (RSSI), security protocols (WPA3/BLE), and detailed device information.
-   **NFC Reader**: Supports various standards including ISO-14443A and interprets NDEF messages in real-time.

### 🖥️ System hardware
-   **Deep Info**: Detailed monitoring of battery health, hardware specifications, and sensor availability status.

---

## 🛠️ Key System Implementations

### 1. SensorAvailabilityManager
Upon the first launch, Sensify performs a full scan of the device's hardware sensors via `SensorManager`.
-   Results are cached in `SharedPreferences` for subsequent lightning-fast startups.
-   The UI automatically filters out tools that require sensors not present in the device.

### 2. Real-time Reactive Streaming
The back-end consists of a `SensorDataManager` that wraps standard Android sensor listeners into **Coroutine Flow**.
-   `ViewModel`s collect these flows, perform unit conversions/filtering, and emit a clean `UI State`.
-   This ensures thread safety and high performance across all tool screens.

### 3. Mesh-Gradient Design System
-   **Dynamic Mesh Background**: Driven by a custom `Canvas` implementation that animates circles and blurs dynamically without creating unnecessary objects every frame.
-   **Unified Gauges**: Consistent use of `NeonBarGauge` and `NeonCircularGauge` for professional-grade visualization.

---

## 🛠️ Technical Stack

-   **UI**: Jetpack Compose (Functional UI, Declarative Animations)
-   **Architecture**: MVVM + Clean Architecture
-   **D.I.**: Dagger Hilt
-   **Asynchrony**: Kotlin Coroutines & Flow
-   **Storage**: Jetpack DataStore (Settings) & SharedPrefs (Cache)
-   **Camera**: CameraX API

---

## ☕ Support the Developer

This project is passionately developed by a student preparing for the Korean CSAT (Suneung). If Sensify has helped you, please consider supporting my journey and studies with a cup of coffee!

[![Sponsor via GitHub](https://img.shields.io/badge/Sponsor_via_GitHub-ea4aaa?style=for-the-badge&logo=github&logoColor=white)](https://github.com/sponsors/yoosh0203)

---

## 🚀 Getting Started

1. Clone this repository.
2. Open in Android Studio (Ladybug or later recommended).
3. Build and run on a physical device for the best sensor experience.

---

## ⚖️ License

Distributed under the **GNU General Public License v3.0**. See [LICENSE](LICENSE) for more details.

Copyright © 2026 Yoo Seung Hyeok. All rights reserved.
