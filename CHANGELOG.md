# Changelog

## 1.1.0-rc.1 — Development branch (NOT published)
- Add GitHub Actions CI and gated signed-release workflow, including lint reports and build artifacts.
- Harden notification dispatch using unique system keys; stop logging notification contents.
- Reduce lifecycle leaks: plugin registration, media callbacks, Compose recomposer and window teardown.
- Handle invalid battery levels, missing app labels, inactive system notifications and malformed settings.
- Add configurable spring motion, motion reduction, idle-state behavior, and simultaneous activity switching.
- Migrate notification dismissal toward Material 3 and remove unnecessary Firebase, Coil, Gson and Accompanist dependencies.
- Improve settings persistence, accessibility navigation, and malformed input handling.
- Remove redundant SYSTEM_ALERT_WINDOW permission for accessibility overlay; use launcher package visibility instead of broad QUERY_ALL_PACKAGES.
- Add optional camera cutout alignment, haptic feedback, and metadata-backed notification progress.
- Add Android 15 emulator install/startup smoke-test job, APK debug-signature checks and SHA-256 in CI.
- Add JUnit tests for notification progress formatting and enhanced permission/lifecycle cleanup.
- Update AndroidX dependencies, themed launcher icons, and remove unused template resources.
- Add pure Kotlin unit tests for battery readings and activity cycling.

**Not a release:** build/CI validation and real-device QA must pass, licensing must be confirmed,
and signed installable APKs must be verified before a release tag.
