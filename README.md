# Simple Web Browser

一個用原生 Android Java 寫成的簡易網頁瀏覽器，使用 WebView 顯示網站。

## 功能

- 輸入網址並前往
- 自動補上 `https://`
- 返回、前進及重新整理
- 支援 JavaScript、DOM Storage 及縮放

## 建置環境

- Android SDK Platform 36
- Android Build Tools 35.0.0
- JDK 21
- Gradle 8.10.2（已附 Gradle Wrapper）

## 建置 Debug APK

```bash
export ANDROID_SDK_ROOT="$HOME/Android/Sdk"
./gradlew assembleDebug
```

輸出位置：`app/build/outputs/apk/debug/app-debug.apk`

## Google Play 上架

完整流程請參閱：

- [完整 Google Play release guide](docs/GOOGLE_PLAY_RELEASE.md)
- [GitHub Actions release workflow](.github/workflows/android-release.yml)

文件包括：

- 設定永久 application ID
- 建立及保護 Upload Key
- 本機 release signing
- 建立及驗證 signed AAB
- Google Play App Signing
- Internal testing、production release 及版本更新
- GitHub Actions encrypted secrets 及手動 release build
- 常見簽名、version code 及 fingerprint 問題

## 安全提示

不要將以下內容提交到 GitHub：

- `.jks` / `.keystore`
- private key
- keystore 或 key password
- Google Play service-account JSON
- `~/.gradle/gradle.properties`

這些檔案已經由 `.gitignore` 排除。
