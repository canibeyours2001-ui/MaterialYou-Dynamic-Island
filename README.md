# Material You Dynamic Island — Next Generation (development)

This fork modernizes an experimental Android Dynamic Island overlay. **It is not a stable release.**
The existing code remains on a review branch; build, on-device compatibility, redistribution rights,
and release signing must be verified before public APK distribution.

**[Development pull request](https://github.com/canibeyours2001-ui/MaterialYou-Dynamic-Island/pull/1)** ·
**[Build / QA artifacts](https://github.com/canibeyours2001-ui/MaterialYou-Dynamic-Island/actions/workflows/android-ci.yml)** ·
**[Fork releases (none yet)](https://github.com/canibeyours2001-ui/MaterialYou-Dynamic-Island/releases)**

Android 12+ (API 31) is the declared minimum. Uses notification listener and accessibility overlay with explicit user grants. No root, special draw-over-other-apps permission, or bootloader unlock is required.

## Features currently implemented in the development branch

- Notifications with app action / inline reply, with per-app filtering.
- Media playback cards, album art, playback actions and charging status.
- Activity prioritization with swipe navigation when multiple activities are active.
- Material You, OLED-friendly black theme, and position / size / gravity settings.
- Interruptible spring-based pill expansion and collapse, with reduced-motion and speed controls.
- Hide island when idle (default), or keep a small idle island visible.
- Auto-center against the actual display cutout when provided by Android, or use manual offsets.
- Configurable haptic feedback on long-press and activity navigation.
- Notification-provided progress percentages for downloads and other ongoing tasks.
- Internal, on-device notification handling; no Firebase or analytics SDK required.

## Builds and installation

The PR CI job publishes QA artifacts if compilation succeeds. Debug builds use **Android's debug
signing key** and must not be confused with a production release. Download from the linked
GitHub Actions run's **Artifacts** section while signed in. The original app's signature may
be different and incompatible with upgrades. Confirm compatibility, back up settings, and use
an emulator or expendable test device before trying any development APK.

Stable distribution is blocked until permission to redistribute the original upstream code
is confirmed. **No public APK download link is advertised as existing.**

## Maintenance and safety

See [feature compatibility](docs/FEATURE_MATRIX.md), [performance testing](docs/PERFORMANCE.md),
[audit](docs/AUDIT.md), [changelog](CHANGELOG.md), and [secure release procedure](docs/RELEASING.md).

## Attribution and licensing

Based on [Angel-Studio / MaterialYou-Dynamic-Island](https://github.com/Angel-Studio/MaterialYou-Dynamic-Island).
The upstream repository currently has **no declared open-source LICENSE**. Its public availability
does not by itself authorize distribution of modified compiled APKs. All original attribution
and notices are retained in Git history; obtain explicit permission or a compatible upstream
license before publishing a public release. Do not invent a license.
