# Performance verification protocol

No performance gains have been numerically measured in this branch yet. The application
now uses event-driven system callbacks instead of background polling and hides the idle
overlay by default, but those are implementation properties, not benchmark results.

## Measurements required
- 60/90/120 Hz frame times: Android Studio System Trace / Macrobenchmark, including P90/P95/P99.
- Cold startup: Macrobenchmark startupTimingMetric on debug/profileable builds.
- RAM: `adb shell dumpsys meminfo fr.angel.dynamicisland` at idle, active media, and notification burst.
- Renderer: `adb shell dumpsys gfxinfo fr.angel.dynamicisland framestats` while animating.
- Battery: repeat A/B trials at fixed brightness/network load on the same phone; compare power over >= 30 minutes in each state.
- Regression tests: 100 rapid notifications, 30 media start/stop transitions, screen rotation, background process restarts.

Record device, Android build, refresh rate, test scenario, measurement count, mean/P95,
before/after commit SHA and observed variance. Compare like for like; no invented numbers.

## Verified baseline — CI run 46096704837 (2026-11-28)

The `checks` job passed compilation, unit tests, debug/release/R8 builds, lint and APK signature verification. The Android 15 `emulator-smoke` job passed the instrumented test, minified QA APK installation and startup smoke. Lint: **0 errors, 10 warnings**, primarily newer-SDK/dependency notices. The CI APK SHA-256 matched independently after download. No P95/P99 frame-pacing, thermal, battery or physical-device measurements are available; do not make numerical performance claims. Follow-up source changes must pass a new run before being marked verified.
