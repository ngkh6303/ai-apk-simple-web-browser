---
name: android-google-play-release
description: Android application signing and Google Play release workflow. Use when creating or modifying an Android app that must be signed, packaged as an AAB, tested, uploaded to Google Play, or released through GitHub Actions; also use when auditing release-key safety and Play publishing readiness.
---

# Android Google Play Release

Use this skill to take an Android project from a local debug build to a secure Google Play release. Prefer Android App Bundle (`.aab`) plus Play App Signing for new Play apps. Keep the app-signing key in Google Play and keep only a separate upload key locally or in encrypted CI secrets.

## Workflow

1. Inspect the project before editing. Identify `applicationId`, `namespace`, `compileSdk`, `targetSdk`, `versionCode`, Gradle/AGP versions, existing signing configuration, and whether a keystore or secret has already been committed.
2. Check the current official Google Play target API policy using web search. Do not hard-code a historical API requirement; update `compileSdk`, `targetSdk`, and installed SDK packages when the current policy requires it.
3. Confirm a permanent, unique `applicationId` before the first Play upload. Treat it as immutable after publishing.
4. Add release signing configuration that reads values from `~/.gradle/gradle.properties` or CI `-P` properties. Never place passwords or private keys in source files.
5. Ensure `.gitignore` excludes `.jks`, `.keystore`, `.p12`, `gradle.properties`, service-account JSON, APKs, AABs, `.gradle/`, and build directories.
6. Create an upload key only when the user is ready to control the credentials. Use RSA 2048 or stronger:

   ```bash
   keytool -genkeypair \
     -alias <upload-alias> \
     -keyalg RSA -keysize 2048 -validity 10000 \
     -keystore "$HOME/<app>-upload.jks"
   ```

   Tell the user to save the keystore password, key password, alias, and a secure backup. Never ask the user to paste private credentials into chat.

7. Configure local properties outside the repository:

   ```properties
   SIMPLEBROWSER_STORE_FILE=/absolute/path/to/upload.jks
   SIMPLEBROWSER_STORE_PASSWORD=...
   SIMPLEBROWSER_KEY_ALIAS=...
   SIMPLEBROWSER_KEY_PASSWORD=...
   ```

   Use project-specific property names when appropriate. Set restrictive file permissions.

8. Build a release AAB:

   ```bash
   ./gradlew clean bundleRelease
   ```

   Confirm `app/build/outputs/bundle/release/*.aab` exists and is non-empty. Never present a debug APK as a Play release.

9. Verify the artifact and certificate. Use `jarsigner -verify -verbose <bundle.aab>` for an AAB and the Android SDK `apksigner verify --verbose <release.apk>` for an APK. Record fingerprints only when needed; never expose private key material.

10. Configure Google Play Console. Create the app with the exact application ID, enroll in Play App Signing, and use a separate upload key. Explain that Google signs the APKs delivered to users with the app-signing key while the developer signs uploads with the upload key.

11. Test through Internal testing before production. Check install, launch, navigation, network loss, HTTPS/HTTP behavior where permitted, rotation, JavaScript/WebView behavior, back navigation, and the Play pre-launch report. Complete store listing, app content, data safety, content rating, screenshots, icon, feature graphic, and privacy-policy declarations as applicable.

12. Create the production release in Play Console with the signed AAB and release notes. Treat this as an external release action; do not publish or roll out a production release unless the user has explicitly authorized it.

13. For updates, preserve `applicationId`, increase `versionCode`, update `versionName`, rebuild the AAB, test on an internal track, and then promote it.

## GitHub Actions

Offer a manual workflow that builds a signed AAB and uploads it as an artifact. Require these encrypted repository secrets:

- `ANDROID_KEYSTORE_BASE64`
- `ANDROID_KEYSTORE_PASSWORD`
- `ANDROID_KEY_ALIAS`
- `ANDROID_KEY_PASSWORD`

Decode the keystore only into the ephemeral runner directory, pass Gradle properties on the command line or through a temporary properties file, and delete temporary material after the job. Do not commit a service-account JSON or automatically publish to Play by default. Direct Play publishing requires a separately scoped service account and explicit user authorization.

When adding a workflow, use maintained major versions of checkout, setup-java, Android SDK setup, and artifact upload actions. Install the exact compile SDK and build-tools versions required by the project. Make the workflow manual (`workflow_dispatch`) unless the user specifically requests automatic release behavior.

## Release hygiene

Run the bundled checker before committing:

```bash
bash /home/ubuntu/skills/android-google-play-release/scripts/check_release_hygiene.sh <project-root>
```

Then run `git diff --check`, inspect `git status`, and scan tracked files for keystores, passwords, service-account JSON, APKs, and AABs. If a secret is found in Git history, stop and instruct the user to rotate/revoke it; deleting the working-tree file is not sufficient.

## Decision points

- **New Play app:** prefer Google-generated app-signing key plus a developer-held upload key.
- **Existing Play app:** preserve the existing application ID and signing relationship; do not replace the app-signing key casually.
- **Cross-store distribution:** discuss whether the same app-signing key must be supplied to Google Play instead of letting Google generate one.
- **Local-only release:** build and verify a signed APK/AAB, but do not claim it is Play-ready until target API, policy forms, and Play App Signing requirements are checked.
- **CI release:** build and publish an artifact to GitHub Actions by default; ask separately before enabling direct Play Console publishing.

## References

For detailed user-facing instructions, consult the project's release guide if present, or create one covering permanent package identity and current target API policy, upload-key creation and backup, local Gradle signing properties, signed AAB generation and verification, Play App Signing and certificate fingerprints, internal testing and production release, versioning, GitHub Actions secrets, and secret hygiene.

Use official sources for policy-sensitive facts:

- <https://developer.android.com/studio/publish/app-signing>
- <https://support.google.com/googleplay/android-developer/answer/9842756>
- <https://developer.android.com/google/play/requirements/target-sdk>
- <https://support.google.com/googleplay/android-developer/answer/9859348>
