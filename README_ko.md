# Sensify: Android를 위한 AI 기반 센서 툴킷 📱

[![GitHub Sponsors](https://img.shields.io/badge/Sponsor--Me-red?style=for-the-badge&logo=github)](https://github.com/sponsors/yoosh0203)
[![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com/)
[![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![License](https://img.shields.io/badge/License-GPLv3-blue.svg)](LICENSE)

> [!NOTE]
> **📢 English Version:** [You can check the English version here.](./README.md)

**Sensify**는 Android 기기의 하드웨어 센서 잠재력을 극대화하는 올인원 툴킷입니다. 단순한 유틸리티를 넘어, **Neo-Glassmorphism** 디자인 언어와 현대적인 Android 개발 아키텍처를 결합하여 시각적으로 아름답고 기술적으로 견고하게 설계되었습니다.

---

## 💎 프로젝트 핵심 철학 (The Core Pillars)

1.  **시각적 탁월함 (Aesthetic Excellence)**: 프리미엄 "네온-글래스모피즘" 디자인을 채택하여 플래그십 기기에 걸맞은 현대적이고 세련된 UI/UX를 제공합니다.
2.  **하드웨어 최적화 (Hardware Optimization)**: 기기의 각 센서를 지능적으로 조사하며, 특정 하드웨어가 없는 기기에서도 크래시 없이 우아하게 동작(Graceful Degradation)하도록 설계되었습니다.
3.  **실시간 상호작용 (Real-time Interaction)**: 60fps에 가까운 실시간 데이터 시각화와 햅틱 피드백을 통해 물리적인 도구를 사용하는 것과 같은 실제적인 조작감을 제공합니다.

---

## ✨ 기능 상세 가이드

### 🧭 네비게이션 및 정밀 측정
-   **나침반 & 고도계**: `TYPE_ROTATION_VECTOR`를 사용한 정밀 방위 측정과 `TYPE_PRESSURE` 센서를 활용한 해발 고도 계산 기능을 제공합니다.
    -   **수평계 & 각도기**: 가속도 센서를 활용한 정밀 수평 기능을 제공합니다. **각도기**는 카메라 프리뷰 위에 원형 가이드를 오버레이하며, '영점 설정' 기능으로 상대적인 각도 측정이 가능합니다.

### 🔊 환경 및 소음 분석
-   **소음 측정기 (데시벨)**: 마이크 입력을 실시간으로 분석합니다. 단순한 수치 표시를 넘어 **시간축 기반 실시간 dB 파형**을 출력하며, 85dB 초과 시 진동 알림을 제공합니다.
-   **금속 탐지기**: 자계 센서(Magnetometer)를 활용해 주변 전자기장을 감지합니다. 실시간 그래프 시각화와 자계 강도에 따른 실시간 햅틱 반응을 포함합니다.
-   **조도 & 색상 측정기**: 주변 밝기(Lux) 측정뿐만 아니라 CameraX를 통해 실시간 RGB 값과 컬러 코드를 추출합니다.

### ❤️ 웰니스 (Wellness)
-   **심박수 측정기**: **CameraX**를 통해 손가락 끝의 혈류 변화(PPG)를 감지합니다. 일반적인 앱과 달리 **연속 측정 모드**를 지원하며, 주기적으로 BPM 스냅샷을 히스토리에 기록합니다.
-   **만보기**: 시스템 레벨의 `TYPE_STEP_COUNTER` 센서를 활용하여 백그라운드에서도 최소한의 배터리로 걸음 수를 정확히 추적합니다.

### 📶 와이어리스 (Wireless)
-   **WiFi/Bluetooth 분석기**: 주변의 신호 강도(RSSI), 보안 프로토콜(WPA3/BLE), 기기 상세 정보를 스캔하고 시각화합니다.
-   **NFC 리더**: ISO-14443A를 포함한 다양한 표준을 지원하며 NDEF 메시지를 실시간으로 해석합니다.

### 🖥️ 시스템 하드웨어
-   **상세 정보**: 배터리 수명, 하드웨어 사양, 센서 가용 상태 등을 한눈에 모니터링할 수 있습니다.

---

## 🛠️ 핵심 시스템 구현 방식

### 1. 센서 가용성 관리 (`SensorAvailabilityManager`)
앱 최초 실행 시 `SensorManager`를 통해 기기의 모든 하드웨어 센서를 전수 조사합니다.
-   결과를 `SharedPreferences`에 캐시하여 이후 실행 속도를 비약적으로 높였습니다.
-   지원되지 않는 하드웨어가 필요한 도구는 목록에서 자동으로 고스트 처리되거나 필터링됩니다.

### 2. 실시간 반응형 스트리밍 (Reactive Streaming)
`SensorDataManager`를 통해 안드로이드 표준 센서 리스너를 **Coroutine Flow**로 래핑하여 관리합니다.
-   `ViewModel`은 이 Flow를 구독하고 단위 변환 및 필터링을 거쳐 정제된 `UI State`를 방출합니다.
-   이를 통해 모든 화면에서 Thread-safe하고 높은 퍼포먼스를 유지합니다.

### 3. 메쉬 그라디언트 디자인 시스템
-   **동적 메쉬 배경**: 커스텀 `Canvas` 구현을 통해 원과 블러 처리를 동적으로 애니메이션화하며, 매 프레임 객체 생성을 지양하여 성능을 최적화했습니다.
-   **통일된 게이지**: `NeonBarGauge`와 `NeonCircularGauge`를 전반에 사용하여 전문적인 시각화 도구로서의 일관성을 유지합니다.

---

## 🛠️ 기술 스택 (Technical Stack)

-   **UI**: Jetpack Compose (선언적 애니메이션 및 함수형 UI)
-   **Architecture**: MVVM + Clean Architecture
-   **D.I.**: Dagger Hilt
-   **비동기 처리**: Kotlin Coroutines & Flow
-   **저장소**: Jetpack DataStore (설정) 및 SharedPrefs (캐시)
-   **카메라**: CameraX API

---

## ☕ 개발자 응원하기

이 프로젝트는 수능 공부와 병행하며 열정으로 개발하고 있는 학생의 결과물입니다. Sensify가 도움이 되셨다면, 커피 한 잔으로 저의 도전과 공부를 응원해 주세요!

[![Sponsor via GitHub](https://img.shields.io/badge/Sponsor_via_GitHub-ea4aaa?style=for-the-badge&logo=github&logoColor=white)](https://github.com/sponsors/yoosh0203)

---

## 🚀 시작하기

1. 이 저장소를 로컬로 클론합니다.
2. Android Studio (Ladybug 이상 권장)에서 프로젝트를 엽니다.
3. 센서 기능을 온전히 테스트하기 위해 가급적 실물 기기에서 실행해 보세요.

---

## ⚖️ 라이선스

본 프로젝트는 **GNU General Public License v3.0**을 따릅니다. 자세한 내용은 [LICENSE](LICENSE) 파일을 참조하세요.

Copyright © 2026 Yoo Seung Hyeok. All rights reserved.
