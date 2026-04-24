# iOS-оболочка

Этот каталог содержит SwiftUI-оболочку над `SharedCore.xcframework`.

## Как собрать на macOS

1. Выполнить `./gradlew :kmp:bridge-apple:assembleSharedCoreXCFramework`.
2. Установить `xcodegen`, если его еще нет.
3. В каталоге `ios/app` выполнить `xcodegen generate`.
4. Открыть сгенерированный `iosApp.xcodeproj` и запустить приложение.

SwiftUI слой работает с:

- `AppleAppFactory` — фабрика из `kmp:bridge-apple`, создаёт `RootComponent` и `AppleRootAccessor`.
- `AppleRootAccessor` / `AppleContactsAccessor` — Apple-специфичные хелперы для навигации (`currentKind`, `watchKind`, `authComponent()`, `contactsAccessor()` и т.п.).
- `RootComponent`, `AuthComponent`, `ContactsComponent` и т.д. из `SharedCore.xcframework` для прямого вызова MVI-интентов.

Bridge-логики (pattern-matching по активному дочернему компоненту, типизация kind-ов) в мультиплатформенной части **нет** — она живёт в `kmp:bridge-apple`.
