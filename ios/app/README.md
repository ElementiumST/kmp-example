# iOS shell

Этот каталог содержит SwiftUI-оболочку над `SharedCore.xcframework`.

## Как собрать на macOS

1. Выполнить `./gradlew :kmp:core:assembleSharedCoreXCFramework`.
2. Установить `xcodegen`, если его еще нет.
3. В каталоге `ios/app` выполнить `xcodegen generate`.
4. Открыть сгенерированный `iosApp.xcodeproj` и запустить приложение.

SwiftUI слой работает напрямую с `RootComponent` и `AuthComponent` из `SharedCore.xcframework`.
