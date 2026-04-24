# Текущее состояние модулей и bridge-слоя

## Архитектура

- В составе проекта используются `kmp/app`, `kmp/navigation`, `kmp/bridge-web` и `kmp/bridge-apple`.
- Для bridge-tooling используются модули `kmp/tools/bridge-annotations` и `kmp/tools/bridge-codegen`.
- Активный набор Gradle-модулей определяется через `settings.gradle.kts`.

## Рантайм

- `kmp/feature/base` включает зависимости FlowMVI.
- Общая база рантайма для feature-компонентов — `FeatureStoreComponent`.
- В рантайме доступны plugin hooks (`RecoverPlugin`, `LoggingPlugin`, `RetryPlugin`, `SavedStatePlugin`).

## Lifecycle

- `RootComponent` экспортирует `destroy()`.
- Android `MainActivity` вызывает `rootComponent.destroy()` в `onDestroy()`.
- iOS `RootViewModel` вызывает `rootComponent.destroy()` в `deinit`.
- Web `App` реализует `OnDestroy` и вызывает `KmpBridgeService.destroy()`.

## Web bridge

- Сгенерированный файл bridge-типов расположен в `libs/src/lib/data-access-kmp-bridge/generated/bridge-types.ts`.
- `KmpBridgeService` использует `routePath`, полученный из generated route table.
- Маршрутизация опирается на bridge-слой, а не на локальный `mapRoute()` в `web-app/src/app/app.ts`.

## Устройство bridge-слоя

- Общие интерфейсы (`RootComponent`, `ContactsComponent`, `AuthComponent`, `ContactInfoComponent`, `ContactEditorComponent`) не содержат bridge-специфичные хелперы или аннотации.
- Общие enum'ы `RootChildKind` и `ContactsChildKind` отсутствуют в shared API.
- `kmp/feature/auth`, `kmp/feature/contacts`, `kmp/core` не зависят от `kmp/tools/bridge-annotations`.
- На стороне Web `WebRootBridge.kt` содержит приватные file-local хелперы, которые напрямую читают Decompose `stack`/`childStack`.
- На стороне Apple классы `AppleRootAccessor` и `AppleContactsAccessor` в `kmp/bridge-apple` предоставляют эквивалентные хелперы для Swift.
- `SharedCore` XCFramework собирается модулем `:kmp:bridge-apple` командой `./gradlew :kmp:bridge-apple:assembleSharedCoreXCFramework`. Пути проекта Xcode (`project.yml`, `project.pbxproj`) настроены на это расположение.
