# Modernization audit and implementation plan

Baseline: fork of Angel-Studio/MaterialYou-Dynamic-Island, main commit `91dc9a1673283f8c8712fe5f0d5f515634ace5a7` (2026-01-28).

## Observed architecture
- One Android app module; Kotlin and Jetpack Compose; Android minSdk 31 and target/compile SDK 36.
- Accessibility overlay service; notification listener; plugin system with notification, media, and charging activities.
- Gradle wrapper 8.13, AGP 8.13.2, Kotlin/Compose plugin 2.3.0.
- Basic example unit/instrumentation tests only; no existing CI or GitHub release in fork.
- Existing APK is checked into `app/release`; its original signing identity is **not verified**.

## Confirmed audit findings
- **P0 notification confidentiality:** Notification content is logged, exported dynamic receivers consume application-defined actions, and notification IDs are treated as globally unique (they are not).
- **P0 lifecycle:** Plugin initialization runs repeatedly; callbacks may be registered multiple times. Overlay Compose recomposer/window lifecycle lacks full teardown.
- **P0 robustness:** Battery charge level calculation divides by reported maximum without validating it. A single-page disclosure divides by zero.
- **P1 usability:** Android 16 One UI 8 resize issue reported upstream (issue 31); device validation needed.
- **P1 compatibility:** Android 12+ runtime behavior, dynamic cutouts, Xiaomi background restrictions, and denied permissions require device testing.
- **P1 dependencies:** AndroidX/Firebase/Accompanist packages mix generations. Upgrade only after code-level compatibility checks.
- **P1 legal:** There is no root `LICENSE` file and GitHub reports `license: null` on the fork. The upstream repository also lacks an explicit license in its inspected tree. **A public download must not be published without confirming redistribution rights.**
- **P2 measurements:** No baseline frame-pacing, CPU, battery, RAM or startup benchmark supplied; improvements cannot be claimed as measured.

## Implementation order
1. P0: Secure broadcast dispatch, reduce sensitive logs, fix notification key handling, and ensure overlay/listener cleanup.
2. P1: Normalize Gradle and ensure clean CI builds on current SDK; remove genuinely unused components after dependency audit.
3. P2: Add instrumented performance benchmarks and lifecycle regression tests.
4. P3: Build an interruptible spring-based island state machine, configurable positioning and transitions.
5. P4: Introduce more Android-API-supported activities (media, timers, navigation, connections) and refined Material You settings.
6. P5: Run device/emulator compatibility matrix, issue beta APK, then gated stable release.

## Release criteria
A release is NOT complete until source builds, lint and unit tests pass; R8 minification succeeds; licensing is confirmed; a persistent private signing key is configured; APK signature is verified; and physical/emulator install plus core interaction checks are documented.

Avoid assuming successful functionality on Xiaomi HyperOS 2 without testing.


## Dependency compatibility finding (November 2026)
November 2026 AndroidX artifacts (Compose BOM 2026.11, Material 3 1.5,
Navigation 2.10.2, Core 1.19) require **compileSdk 37 and AGP 9.1+**.
Using those on compileSdk 36/AGP 8.13.2 caused 38 AAR metadata errors in CI.
The branch therefore pins tested-for-compilation API 36 versions rather than
forcing an unverified AGP 9 and API 37 migration. Android Studio's newer-version
warnings for those intentionally pinned libraries are documented, not suppressed.

## Release blocker (legal and signing)
The original upstream has no explicit license in its Git tree or GitHub metadata.
Public availability and the phrase "open-sourced" in README do not by themselves
grant permission to distribute derivative binary APKs. Confirmation is required
before any public release. The prior signing key is also unavailable.
