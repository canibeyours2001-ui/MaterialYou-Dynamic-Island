# Secure signed release workflow

## Requirements
- Document explicit upstream redistribution rights/license; public code is not automatically open-source licensed.
- Create a private persistent Android upload/signing keystore. Never commit it or its passwords.
- Configure GitHub Actions secrets: `ANDROID_KEYSTORE_BASE64` (base64 bytes of keystore file), `ANDROID_KEYSTORE_PASSWORD`, `ANDROID_KEY_ALIAS`, `ANDROID_KEY_PASSWORD`.
- Configure repository variable `RELEASE_RIGHTS_CONFIRMED=true` only after legal rights verified.
- For *stable* (no `-rc`/`-beta`) tags also set `STABLE_RELEASE_APPROVED=true` only after real device testing and explicit approval.

## CI
Every branch push/PR runs Android debug/release assembly, unit tests and lint. PR artifacts are **unsigned/debug builds**, never production APKs. Releases only run on `v*` tags, matching `app/build.gradle` versionName.

## Tagging after approval
```bash
git tag -a v1.1.0-rc.1 -m "Release candidate 1.1.0"
git push origin v1.1.0-rc.1
```

GitHub Actions refuses missing secrets, version mismatch or unapproved stable tags. It verifies APK signing, generates SHA256SUMS.txt and publishes the release only after successful checks.

## Signature continuity
New keystore **cannot update** an APK signed with the upstream/original developer's key. Users of the original APK may have to uninstall before installing a new-signed build, losing local data unless backed up. Maintain the same signing key for every subsequent release.

## Manual device QA before stable
Verify installation, startup, notification access, accessibility disclosure and overlay, denied permissions, media controls, battery charging, rapid notification bursts, rotation, lock screen, Xiaomi HyperOS background process behavior, memory and ANR reports, and update-over-previous-release.
