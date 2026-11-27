# Android feature compatibility

These are **implemented source behaviors**, not verified device-testing claims.
The app declares API 31+ (Android 12 and later), with target SDK 36.

| Feature | Implementation | Limitation / test needed |
| --- | --- | --- |
| Persistent pill and charging event | Accessibility overlay + battery broadcasts | Xiaomi/One UI process restrictions; OLED power not benchmarked |
| Notifications and inline reply | NotificationListenerService + notification actions | User must grant access; private notification text is kept on device |
| Music art, play/pause/skip | MediaSessionManager | Depends on other apps exposing media sessions |
| Multiple simultaneous activities | Priority queue by plugin + horizontal swipes | UI gestures need physical verification; not system-level Live Activities |
| Spring morphing | Compose animation state for width, height, corner radius | No measured 60/90/120-Hz frame-time benchmark yet |
| Accessibility | Reduced motion and explicit service disclosure | TalkBack and animation interruption need manual QA |
| Themes | Wallpaper-based Material You, OLED black, light/dark | Color contrast testing needed |
| Island positioning | User-adjustable size, gravity and X/Y offsets | Camera cutout automatic pixel alignment not yet guaranteed |
| Timers / navigation | Generic notification cards | Dedicated time-to-arrival/timer APIs not implemented |
| Earbud battery / Bluetooth | Not implemented | Requires runtime BLUETOOTH_CONNECT and hardware support |
| Screen recording indicator | Not implemented | Other apps' recording state is not generally exposed to normal apps |
| Calls | Notification-based where apps provide notifications | No privileged telecom integration |
| Downloads / hotspot | Generic notifications only | Native hotspot telemetry is restricted |
| Locked screen | Optional toggle | OEM-specific behavior varies |
| Root | Not required | Deliberately avoids hidden/privileged APIs |

## Device QA matrix
- [ ] Pixel (Android 14, 15, 16): startup, notification access, media playback, rotation
- [ ] Xiaomi HyperOS 2 (Android 14): permission management and battery restrictions
- [ ] Samsung One UI: overlay, cutouts, background service restart
- [ ] OnePlus/other OEM: window dimensions, notification listener lifecycle
- [ ] Screen on/off, landscape, low-memory, spam notifications and concurrent media
- [ ] Upgrade/uninstall behavior for APKs signed with a different key

Results must be attached to a real test run; no unexecuted tests should be marked passed.
