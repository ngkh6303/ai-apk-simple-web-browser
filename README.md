# Simple Web Browser

一個用原生 Android Java 寫成的簡易網頁瀏覽器，使用 WebView 顯示網站。

## 功能

- 輸入網址並前往
- 自動補上 `https://`
- 返回、前進及重新整理
- 支援 JavaScript、DOM Storage 及縮放

## 建置環境

- Android SDK Platform 35
- Android Build Tools 35.0.0
- JDK 21
- Gradle 8.10.2

## 建置 Debug APK

```bash
export ANDROID_SDK_ROOT="$HOME/Android/Sdk"
./gradlew assembleDebug
```

輸出位置：`app/build/outputs/apk/debug/app-debug.apk`

## 建置 Google Play 用 Release AAB

請先在 `~/.gradle/gradle.properties` 設定本機 Upload Key 資料，不要將 keystore 或密碼提交到 GitHub：

```properties
SIMPLEBROWSER_STORE_FILE=/absolute/path/to/simplebrowser-upload.jks
SIMPLEBROWSER_STORE_PASSWORD=your_keystore_password
SIMPLEBROWSER_KEY_ALIAS=simplebrowser-upload
SIMPLEBROWSER_KEY_PASSWORD=your_key_password
```

然後在 `app/build.gradle` 加入 release signing config，再執行：

```bash
./gradlew bundleRelease
```

輸出位置：`app/build/outputs/bundle/release/app-release.aab`

Google Play 新 app 應使用 Android App Bundle，並啟用 Play App Signing。Upload Key 必須安全保存；不要上載 `.jks`、密碼或任何 private key。
