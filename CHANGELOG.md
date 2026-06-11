# eXcap Changelog

All notable changes to the eXcap project are documented in this file.

## [2.0.0-excap] - 2024-06-12

### Built by eXU CODER

### Complete Rebranding
- Renamed project from "PCAPdroid" to "eXcap" across all user-facing strings
- New launcher icon with dark teal (#0D7377) and electric blue (#00D4FF) color scheme
- New adaptive icon set for all Android density qualifiers
- Updated application name, labels, and metadata
- Updated license attribution to include "Built by eXU CODER"
- Preserved original GPL v3 license terms

### UI/UX Rewrite - Jetpack Compose
- **Complete migration from XML layouts to Jetpack Compose**
- **Material Design 3** with dark-first theme
- **Dynamic color support** on Android 12+ devices
- **Custom eXcap color palette**: Dark teal, electric blue, cyan accents
- **Bottom navigation** with 4 tabs: Capture, Connections, Stats, Settings
- **Shared element transitions** between screens
- **Haptic feedback** on capture start/stop
- **Empty states** and loading skeletons for all async operations

### New Capture Screen
- Large central toggle button with animated concentric waveform rings
- Real-time bandwidth display (RX/TX/Connections)
- Floating protocol filter chip row (All, HTTP, HTTPS, DNS, TCP, UDP)
- Visual status indicator with color-coded states
- Smooth scale and glow animations

### New Connections Screen
- Searchable connection list with instant filtering
- Protocol filter chips for quick filtering
- Connection cards showing protocol icon, host, IP, data transferred
- Color-coded protocol indicators (HTTPS=blue, HTTP=green, DNS=orange, etc.)
- Empty state with contextual messaging

### New Stats Screen
- Animated donut chart for protocol distribution
- Real-time bandwidth graph with RX/TX lines
- Top domains by data usage
- Top apps by data consumption
- Card-based layout with Material 3 styling

### New Settings Screen
- Grouped preference categories (Capture, Filtering, Export, About)
- Toggle switches with inline explanations
- Technical option descriptions for TLS decryption and root capture
- Built-in eXU CODER attribution
- Action items for certificate export and user guide

### CI/CD Pipeline
- **GitHub Actions workflow** authored by eXU CODER
- Triggers on push to main/master and pull requests
- Gradle dependency caching for faster builds
- Automatic unit test execution
- Debug and release APK artifact uploads
- Draft release creation with changelogs
- "Built by eXU CODER" attribution in all release notes

### Preserved Functionality
- VPN-based capture without root (100% preserved)
- Native capture engine and zdtun integration (unmodified)
- nDPI deep packet inspection bindings (unmodified)
- CaptureService lifecycle and binder interface (unmodified)
- TLS decryption certificate handling (unmodified)
- PCAP reading, writing, and export formatting (unmodified)
- Connection metadata data classes (unmodified)
- All JNI interfaces and native library loading (unmodified)
- mitmproxy addon integration (unmodified)
- HTTP server for PCAP file download (unmodified)
- Import/export formats (unmodified)

### Technical Details
- **Package name preserved**: `com.emanuelef.remote_capture` (JNI compatibility)
- **Jetpack Compose 1.6.8** added alongside existing UI
- **Material Design 3** components throughout
- **Kotlin Coroutines & Flow** for reactive UI
- **Application class renamed**: `PCAPdroid` → `ExcapApp`

---

## Original PCAPdroid History

For the complete changelog of the original PCAPdroid project, see the git history and original releases at:
https://github.com/emanuele-f/PCAPdroid/blob/master/CHANGELOG.md

---

**All work built by eXU CODER**
