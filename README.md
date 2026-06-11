# eXcap - Network Capture & Analysis

**Built by eXU CODER**

[![eXcap CI/CD](https://github.com/exucoder/excap/actions/workflows/excap-build.yml/badge.svg)](https://github.com/exucoder/excap/actions/workflows/excap-build.yml)

---

## Overview

**eXcap** is a modern, open-source network capture and monitoring tool for Android. Forked from the powerful [PCAPdroid](https://github.com/emanuele-f/PCAPdroid) project, eXcap delivers a complete UI/UX overhaul with Jetpack Compose while preserving 100% of the original network capture, TLS decryption, and traffic analysis capabilities.

Capture network traffic without root privileges, inspect connections in real-time, decrypt TLS traffic, and export PCAP files — all wrapped in a beautiful, modern interface.

---

## Features

### Network Capture
- **VPN-based capture** without requiring root access
- **Real-time connection tracking** with live updates
- **App-level filtering** — capture only specific app traffic
- **Protocol filtering** — filter by HTTP, HTTPS, DNS, TCP, UDP, QUIC
- **PCAP export** in standard format for Wireshark analysis

### Traffic Analysis
- **Connection inspector** with full metadata
- **Payload viewer** in text and hex modes
- **HTTP request/response** reconstruction
- **Protocol distribution** statistics
- **Bandwidth monitoring** with real-time graphs

### TLS Decryption
- **TLS traffic decryption** via user-installed CA certificate
- **mitmproxy integration** for advanced interception
- **Setup wizard** for easy certificate installation

### Modern UI (Jetpack Compose)
- **Dark-first design** with Material Design 3
- **Dynamic color theming** on Android 12+
- **Bottom navigation** with Capture, Connections, Stats, Settings
- **Animated waveform** visualization during capture
- **Searchable connection list** with filter chips
- **Beautiful charts** for protocol and bandwidth analytics

---

## Screenshots

| Capture Screen | Connections | Stats | Settings |
|:---:|:---:|:---:|:---:|
| Animated toggle with waveform | Searchable connection list | Protocol distribution | Grouped preferences |

---

## Download

### Latest Release
Download the latest APK from the [Releases](https://github.com/exucoder/excap/releases) page.

### CI Artifacts
Every successful build produces APK artifacts — click the latest green build [here](https://github.com/exucoder/excap/actions).

---

## Build Instructions

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17
- Android SDK API 36
- Android NDK r28b
- CMake 3.22.1

### Clone and Build

```bash
# Clone with submodules
git clone --recursive https://github.com/exucoder/excap.git
cd excap

# Build debug APK
./gradlew assembleStandardDebug

# Build release APK
./gradlew assembleStandardRelease

# Run tests
./gradlew testStandardDebugUnitTest
```

### Build with GitHub Actions

The project includes a complete CI/CD pipeline that:
- Builds on every push to `main` and pull requests
- Caches Gradle dependencies for fast builds
- Runs unit tests automatically
- Produces signed APK artifacts
- Creates draft releases with changelogs

---

## Architecture

```
eXcap
├── Native Layer (Preserved)
│   ├── zdtun tunnel library
│   ├── nDPI deep packet inspection
│   ├── libpcap integration
│   └── JNI bindings to CaptureService
├── Service Layer (Preserved)
│   ├── CaptureService (VPN service)
│   ├── ConnectionsRegister
│   ├── MitmReceiver
│   └── PCAP export engines
├── Model Layer (Preserved)
│   ├── ConnectionDescriptor
│   ├── CaptureStats
│   ├── PayloadChunk
│   └── AppDescriptor
└── UI Layer (Jetpack Compose - New)
    ├── ComposeMainActivity
    ├── CaptureScreen (waveform toggle)
    ├── ConnectionsScreen (search/filter)
    ├── StatsScreen (charts/graphs)
    ├── SettingsScreen (preferences)
    └── ExcapTheme (Material 3)
```

---

## Permissions

| Permission | Purpose |
|-----------|---------|
| `VPN_SERVICE` | Capture network traffic |
| `INTERNET` | HTTP server for PCAP download |
| `FOREGROUND_SERVICE` | Run capture in background |
| `POST_NOTIFICATIONS` | Capture status notification |
| `QUERY_ALL_PACKAGES` | App filtering |

---

## Credits

### Built by eXU CODER
Complete rebranding, Jetpack Compose UI rewrite, and CI/CD pipeline.

### Original Project
[eXcap](https://github.com/exucoder/excap) is based on [PCAPdroid](https://github.com/emanuele-f/PCAPdroid) by [Emanuele Faranda](https://github.com/emanuele-f).

### Third-Party Libraries
- **zdtun** - VPN tunnel library (LGPL-3.0)
- **nDPI** - Deep packet inspection (LGPL-3.0)
- **libpcap** - Packet capture library (BSD)
- **zstd** - Compression library (GPL-2.0)
- **mitmproxy** - TLS interception (MIT)
- **MaxMind DB** - Geolocation database (Apache-2.0)
- **Jetpack Compose** - UI toolkit (Apache-2.0)

See [COPYING](COPYING) for the full GPL v3 license.

---

## License

eXcap is licensed under the **GNU General Public License v3.0 (GPL v3)**.

```
eXcap - Network Capture & Analysis
Copyright (C) 2024 eXU CODER

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.
```

The original PCAPdroid code remains under its original GPL v3 license by Emanuele Faranda.

---

**Built with ❤️ by eXU CODER**
