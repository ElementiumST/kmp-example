# iOS shell

Этот каталог содержит SwiftUI-оболочку над `SharedCore.framework`.

## Как собрать на macOS

1. Выполнить `./gradlew :kmp:core:assembleSharedCoreXCFramework`.
2. Установить `xcodegen`, если его еще нет.
3. В каталоге `ios/app` выполнить `xcodegen generate`.
4. Открыть сгенерированный `iosApp.xcodeproj` и запустить приложение.

SwiftUI слой подписывается на shared state через `AppleRootController` и отправляет действия обратно в KMP-часть через callback-методы.
