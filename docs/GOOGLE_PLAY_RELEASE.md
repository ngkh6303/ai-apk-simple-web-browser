# Google Play release guide

This document covers the complete release path for **Simple Web Browser**: package identity, upload-key creation, local signing, Android App Bundle generation, Play App Signing, testing, Play Console release, updates, and GitHub Actions.

> **Security rule:** never commit a `.jks`, `.keystore`, private key, password, service-account JSON, or signing certificate private material to GitHub. This repository ignores those files by default.

## 1. Before publishing

### 1.1 Choose a permanent application ID

The starter project currently uses `com.example.simplebrowser`. Before creating the Play Console app, replace it with an ID that belongs to you, for example:

```groovy
namespace 'com.yourcompany.simplebrowser'
applicationId 'com.yourcompany.simplebrowser'
```

The application ID is the permanent identity of the Play app. Do not change it after publishing, or Google Play will treat the build as a different app.

### 1.2 Check current Play requirements

Google Play requires new apps and updates to target the current required Android API level. As of **31 August 2026**, the official requirement is Android 16 / API 36 or higher for new apps and app updates (with documented exceptions). The project currently targets API 35, so check the current policy and update `compileSdk`, `targetSdk`, and the installed SDK platform before release if required:

```groovy
compileSdk 36

...

targetSdk 36
```

Official policy: <https://developer.android.com/google/play/requirements/target-sdk>

### 1.3 Prepare store listing material

Prepare the following in Play Console:

- App name and short description
- Full description
- App icon (512 × 512 PNG)
- Feature graphic (1024 × 500 PNG)
- Phone screenshots (and tablet screenshots if applicable)
- Category and contact email
- Privacy policy URL, if required by the data-safety answers or app functionality
- Content rating questionnaire answers
- Data safety form answers
- Countries/regions, pricing and distribution choices

## 2. Create an upload key

Create this once on a secure computer. The upload key is different from the app-signing key managed by Google Play.

```bash
keytool -genkeypair \
  -alias simplebrowser-upload \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000 \
  -keystore "$HOME/simplebrowser-upload.jks"
```

Keep these four values in a password manager:

- Keystore path
- Keystore password
- Key alias: `simplebrowser-upload`
- Key password

Protect the files:

```bash
chmod 600 "$HOME/simplebrowser-upload.jks"
```

Back up the keystore securely. If the upload key is lost, Play Console can reset the upload key after verification. Do not email or commit the keystore.

To inspect the upload certificate fingerprint:

```bash
keytool -list -v \
  -keystore "$HOME/simplebrowser-upload.jks" \
  -alias simplebrowser-upload
```

## 3. Configure local signing

Create `~/.gradle/gradle.properties` on your computer. Do not create this file inside the repository:

```properties
SIMPLEBROWSER_STORE_FILE=/absolute/path/to/simplebrowser-upload.jks
SIMPLEBROWSER_STORE_PASSWORD=your_keystore_password
SIMPLEBROWSER_KEY_ALIAS=simplebrowser-upload
SIMPLEBROWSER_KEY_PASSWORD=your_key_password
```

Set restrictive permissions:

```bash
chmod 600 ~/.gradle/gradle.properties
```

The project `app/build.gradle` reads these values only when they are present. A release build without these values is intentionally unsigned and must not be uploaded to Play.

## 4. Build and verify a signed release

From the repository root:

```bash
export ANDROID_SDK_ROOT="$HOME/Android/Sdk"
./gradlew clean
./gradlew bundleRelease
```

The signed bundle is created at:

```text
app/build/outputs/bundle/release/app-release.aab
```

Verify the bundle before uploading:

```bash
jarsigner -verify \
  -verbose \
  app/build/outputs/bundle/release/app-release.aab
```

You can also inspect the certificate with:

```bash
keytool -printcert -jarfile \
  app/build/outputs/bundle/release/app-release.aab
```

For a signed APK verification (useful for a locally generated APK):

```bash
$ANDROID_SDK_ROOT/build-tools/35.0.0/apksigner verify --verbose \
  app/build/outputs/apk/release/app-release.apk
```

## 5. Create the Play Console app and enable Play App Signing

1. Open <https://play.google.com/console/> and create a new app.
2. Use the exact permanent application ID from `app/build.gradle`.
3. Complete the developer account and app details.
4. Open **Test and release > App integrity** or the current **App signing** page.
5. Enrol in **Google Play App Signing**.
6. For a new app, allow Google Play to generate and protect the app-signing key unless you have a specific cross-store key requirement.
7. Keep your local key as the separate upload key.

With Play App Signing:

- You keep the upload key and use it to sign the `.aab` you upload.
- Google keeps the app-signing key and signs the optimized APKs delivered to users.
- If the upload key is lost or compromised, request an upload-key reset in Play Console.
- The app-signing key fingerprint, not only the upload-key fingerprint, is needed by services such as OAuth or Google APIs.

Official documentation: <https://support.google.com/googleplay/android-developer/answer/9842756>

## 6. Test before production

Use this sequence:

1. Upload `app-release.aab` to **Internal testing**.
2. Add tester email addresses or a tester Google Group.
3. Install from the Play testing link on a real device.
4. Test HTTPS and HTTP pages, JavaScript pages, back navigation, forward navigation, reload, rotation, network loss, and external links.
5. Review the pre-launch report.
6. Fix all crashes and policy warnings.
7. Promote the tested release to **Closed testing** or **Production**.

## 7. Create a production release

In Play Console:

1. Open **Test and release > Production**.
2. Create a new release.
3. Upload `app-release.aab`.
4. Add release notes.
5. Complete the App content, Data safety, Content rating, and store-listing sections.
6. Review the generated warnings and declarations.
7. Submit for review and roll out when ready.

Google Play requires new apps to use Android App Bundle (`.aab`). Do not upload the debug APK. Do not upload an unsigned release.

## 8. Release updates

For every update, increase `versionCode` and update `versionName`:

```groovy
defaultConfig {
    applicationId 'com.yourcompany.simplebrowser'
    minSdk 23
    targetSdk 36
    versionCode 2
    versionName '1.1'
}
```

Build again with the same application ID:

```bash
./gradlew clean bundleRelease
```

Upload the new `app-release.aab` to a testing track first. Never reset `versionCode` to an earlier value.

## 9. GitHub Actions release build

The workflow at `.github/workflows/android-release.yml` builds a signed AAB when manually triggered. Configure these **GitHub Actions secrets** first:

- `ANDROID_KEYSTORE_BASE64`: base64 contents of the upload keystore
- `ANDROID_KEYSTORE_PASSWORD`
- `ANDROID_KEY_ALIAS`
- `ANDROID_KEY_PASSWORD`

Create the base64 value locally without committing the keystore:

```bash
base64 -w 0 "$HOME/simplebrowser-upload.jks" > /tmp/simplebrowser-upload.jks.base64
```

Copy the contents of that temporary file into the GitHub secret. Then run the workflow from **Actions > Android release > Run workflow**. It uploads the AAB as a GitHub Actions artifact; download and upload that AAB to Play Console after testing.

The workflow deliberately does not publish directly to Google Play. Direct publishing requires a separate Google Play service-account setup and grants the GitHub repository permission to create releases in your Play Console account.

## 10. Optional direct Play publishing

If direct publishing is later required, create a restricted Google Play service account, grant only the required Play Console permissions, store its JSON as an encrypted GitHub secret, and use a maintained Play Publisher action. Never commit the JSON file. Manual Play Console upload is the safer default for this small project.

## 11. Troubleshooting

### `App Bundle is signed with the wrong key`

The upload certificate registered in Play Console does not match the local upload key. Check the SHA-256 fingerprint in Play Console and compare it with:

```bash
keytool -list -v -keystore "$HOME/simplebrowser-upload.jks" \
  -alias simplebrowser-upload
```

### `Version code already used`

Increase `versionCode` in `app/build.gradle`.

### `Release bundle is unsigned`

Check that all four `SIMPLEBROWSER_*` values are available in `~/.gradle/gradle.properties` or the GitHub Actions secrets. Do not upload an unsigned artifact.

### Lost upload key

Use Play Console's **App integrity / App signing** page to request an upload-key reset. The Play app-signing key remains protected by Google Play.

### API login or App Links fingerprint mismatch

Register the Google Play **app-signing certificate** fingerprint with the API provider. The local upload-key fingerprint alone is not enough for the APK installed by users.

## Official references

- [Sign your app](https://developer.android.com/studio/publish/app-signing)
- [Use Play App Signing](https://support.google.com/googleplay/android-developer/answer/9842756)
- [Target API level requirements](https://developer.android.com/google/play/requirements/target-sdk)
- [Prepare and roll out a release](https://support.google.com/googleplay/android-developer/answer/9859348)
