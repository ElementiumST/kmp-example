# Руководство по bridge-codegen

## Структура модулей

| Модуль | Роль |
| --- | --- |
| `kmp/tools/bridge-annotations` | Контракт аннотаций (`@ExposeToWeb`, `@BridgeAction`, `@BridgeChild`). Используется только bridge-модулями. |
| `kmp/tools/bridge-codegen` | KSP-процессор и утилиты для генерации TypeScript-типов. |
| `kmp/bridge-web` | Поверхность `@JsExport` (`WebBridgeFactory`, `WebRootBridge`), JSON-адаптеры состояния и мапперы строковых kind-значений. Подключает KSP-плагин. |
| `kmp/bridge-apple` | Apple-ориентированные фасады (`AppleAppFactory`, `AppleRootAccessor`, `AppleContactsAccessor`) и XCFramework `SharedCore`. |

## Золотое правило: bridge остаётся в bridge-модулях

Общие MPP-интерфейсы (`RootComponent`, `ContactsComponent`, `AuthComponent`, ...) являются **MVI-контрактами бизнес-уровня**:

- MVI `state` / `onAction` / `actions` (из `FeatureStoreComponent`).
- Хелперы в стиле intent (`refresh()`, `updateQuery(value)`, `openInfo(index)`).
- Опциональный `Value<ChildStack<*, Child>>` для вложенной навигации.

Весь bridge-специфичный дополнительный слой должен реализовываться в bridge-модулях:

- Pattern matching (`val c = stack.value.active.instance as? RootComponent.Child.Contacts`).
- Наблюдение за строковыми kind-значениями (`currentChildKind(): String`, `watchChildKind { ... }`).
- JSON-сериализация состояния для JS-потребителей.
- Platform-friendly объекты доступа.

Это обеспечивается соглашением: ни общие интерфейсы, ни их классы `Default*` не импортируют `kmp/tools/bridge-annotations`.

## Как открыть новый shared API для Web

1. Добавьте или расширьте чистый MVI-метод на общем feature-компоненте.
2. Откройте `kmp/core/src/commonMain/.../bridge/web/WebRootBridge.kt` (пока он живёт в `kmp/core`, позже переедет в `kmp/bridge-web`, когда перенос модуля будет завершён).
3. Добавьте метод в `WebRootBridge`, который:
   - использует один из существующих приватных accessor-ов (`authComponentOrNull`, `contactsComponentOrNull`, `infoComponentOrNull`, `activeEditorComponentOrNull`);
   - либо проксирует вызов, либо возвращает JSON-снимок.
4. Расширьте `libs/src/lib/data-access-kmp-bridge/generated/bridge-types.ts` и `data-access-kmp-bridge.ts`, добавив соответствующую TS-сигнатуру.
5. Снова синхронизируйте JS bundle: `./gradlew :kmp:bridge-web:jsBrowserDevelopmentLibraryDistribution && npm run sync:shared-core`.

## Как открыть новый shared API для iOS

1. Добавьте или расширьте чистый MVI-метод на общем feature-компоненте.
2. Откройте `kmp/bridge-apple/src/commonMain/.../AppleRootAccessor.kt` (или `AppleContactsAccessor`).
3. Добавьте accessor-метод, использующий pattern matching по `stack`/`childStack`.
4. Используйте его из Swift через `AppleRootAccessor` — не обращайтесь напрямую к `RootComponent` для задач навигации и определения активных child-компонентов.
5. Пересоберите framework: `./gradlew :kmp:bridge-apple:assembleSharedCoreXCFramework`.

## Команды проверки

- `./gradlew :kmp:tools:bridge-codegen:test`
- `./gradlew :kmp:bridge-web:compileCommonMainKotlinMetadata`
- `./gradlew :kmp:bridge-apple:compileCommonMainKotlinMetadata`
- `npm run test` (из web-workspace, если менялись bridge-адаптеры)
