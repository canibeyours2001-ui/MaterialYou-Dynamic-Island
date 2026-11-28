# Encrypted internal Android CI artifacts

The public source repository does not upload plaintext APKs to GitHub Actions.
Each successful pull-request check produces `encrypted-android-testing`
containing `debug.apk.p7m` and `qaRelease.apk.p7m`. These are AES-256-CBC
CMS envelopes encrypted for the holder of the matching RSA private key; they
are **not installable APK files**. This encryption recipient expires in
November 2027 and must be rotated before then.

The recipient certificate is public: `.github/test-apk-recipient.crt`.
The private key is not stored in this repository, in GitHub secrets, or in
CI. Losing the private key means losing access to the encrypted test artifacts.

On a trusted local machine after downloading the archive:
```bash
openssl cms -decrypt -binary -inform DER \
  -in debug.apk.p7m -recip .github/test-apk-recipient.crt \
  -inkey MaterialYou-Dev-Artifact-private-key.pem -out app-debug.apk
sha256sum app-debug.apk
```
Compare the SHA-256 hash against the build job log. Repeat for
`qaRelease.apk.p7m` to test the R8-minified variant. Do not distribute
decrypted APKs or upload the private key publicly. Previous plaintext
artifacts from runs before the privacy gate need manual deletion by an
administrator. Encryption protects access, not copyright permissions.
