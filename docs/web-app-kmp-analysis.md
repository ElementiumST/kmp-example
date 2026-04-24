# Анализ связки `web-app` и KMP

## Что это за связка

`web-app` — это Angular-оболочка над общей KMP-логикой. Бизнес-правила, состояние экранов и навигационное дерево живут в KMP, а web-слой отвечает за запуск, отображение UI и проксирование пользовательских действий.

Но важна актуальная граница модулей:

- bundle для web-оболочки собирается из `kmp/bridge-web`
- generated route table и TS-определения тоже приходят из `kmp/bridge-web`
- рантайм-реализация `WebBridgeFactory` и `WebRootBridge` находится в `kmp/core`

То есть для web правильнее думать так:

`web-app` -> `kmp/bridge-web` как точка входа для дистрибутива -> `kmp/core` как место, где живёт основной bridge-рантайм

## Актуальная цепочка интеграции

Поток интеграции выглядит так:

`web-app` -> `sync-shared-core.mjs` -> `web-app/public/shared-core` -> `KmpBridgeService` -> `WebBridgeFactory` -> `WebRootBridge` -> `RootComponent` -> общие feature-компоненты / use case'ы / data-слой

Ключевые точки:

- `web-app/scripts/sync-shared-core.mjs`
  - запускает `:kmp:bridge-web:jsBrowserDevelopmentLibraryDistribution`
  - копирует JS-вывод из `kmp/bridge-web/build/dist/js/developmentLibrary`
  - генерирует `manifest.json` с порядком загрузки скриптов

- `kmp/bridge-web/build.gradle.kts`
  - собирает JS-библиотеку
  - генерирует TypeScript-определения
  - подключает `kmp/tools/bridge-codegen` через KSP

- `kmp/bridge-web/src/commonMain/.../WebBridgeFacade.kt`
  - даёт фасад над `kmp.core.bridge.web.WebBridgeFactory`
  - содержит `GeneratedRouteTable`

- `libs/src/lib/data-access-kmp-bridge/data-access-kmp-bridge.ts`
  - загружает `manifest.json`
  - последовательно подключает скрипты из `public/shared-core`
  - ищет KMP-модуль сначала в `globalThis['kmp-example.kmp:bridge-web']`, затем делает fallback на `globalThis['kmp-example.kmp:core']`
  - создаёт bridge и подписывается на общее состояние

- `kmp/core/src/commonMain/.../WebRootBridge.kt`
  - экспортирует `WebBridgeFactory` и `WebRootBridge` через `@JsExport`
  - создаёт общий root-компонент через `SharedApp.createRootComponent(...)`
  - отдаёт наружу текущие child kind, JSON-снимки и методы действий

- `web-app/src/app/app.ts`
  - инициализирует bridge в `ngOnInit()`
  - синхронизирует Angular router через `bridge.routePath()`
  - вызывает `bridge.destroy()` в `ngOnDestroy()`

## Как проходит запуск на web

1. Angular поднимает приложение.
2. Корневой `App` вызывает `KmpBridgeService.initialize('/api/rest')`.
3. `KmpBridgeService` читает `/shared-core/manifest.json`.
4. Сервис последовательно загружает все скрипты из `web-app/public/shared-core`.
5. После загрузки сервис ищет `WebBridgeFactory` в глобальном пространстве имён KMP.
6. `WebBridgeFactory.create(baseUrl)` создаёт `WebRootBridge`.
7. `WebRootBridge` внутри создаёт общий `RootComponent`.
8. `KmpBridgeService` подписывается на root child, contacts child и состояние фич.
9. Когда bridge готов, Angular начинает синхронизировать URL через `routePath`.

## Где находится источник истины

### Состояние экранов

Источником истины для состояния является KMP:

- `AuthState`
- `ContactsListState`
- `ContactInfoState`
- `ContactEditorState`

Web не хранит собственную независимую бизнес-модель. Он получает снимки состояния от общих компонентов и кладёт их в Angular `signal`.

### Навигация

Источником истины для навигации также является KMP, но с важным уточнением:

- в shared-контрактах больше нет публичных `watchChildKind()` / `currentChildKind()`
- для web kind-значения вычисляются приватными file-local функциями в `WebRootBridge.kt`
- `KmpBridgeService` преобразует пары `rootChildKind + contactsChildKind` в путь через generated route table

## Как обновляется route

Сейчас маршрут собирается так:

1. `WebRootBridge` отдаёт `currentRootChildKind()`, `contactsChildKind()` и подписки `watchRootChildKind(...)`, `watchContactsChildKind(...)`.
2. `KmpBridgeService` обновляет `rootChildKind` и `contactsChildKind`.
3. Сервис ищет подходящий путь в `generatedRouteTable`.
4. Значение сохраняется в `routePath`.
5. `App` через `effect(...)` вызывает `router.navigateByUrl(...)`, если URL отличается.

Итог: Angular не содержит локальной логики сопоставления экранов с route path. Эта ответственность централизована в bridge-слое.

## Как состояние попадает в Angular

Поток обновления идёт целыми snapshot-объектами:

1. общий компонент меняет состояние
2. `watchState(...)` в KMP получает новое значение
3. `WebRootBridge` сериализует его в JSON
4. `KmpBridgeService` получает строку
5. TypeScript делает `JSON.parse(...)`
6. Angular `signal` получает новый объект

Это даёт простую и рабочую схему: bridge работает не через протокол на основе патчей, а через полные JSON-снимки.

## Ключевые свойства интеграции

### 1. Маршрутизация централизована в bridge-слое

Путь централизован в `KmpBridgeService` и опирается на generated route table.

### 2. Граница с TypeScript типизирована

Web использует generated-слой типов. Это снижает риск дрейфа между Kotlin и TypeScript на этапе компиляции.

### 3. Lifecycle bridge-слоя замкнут

- `WebRootBridge.destroy()` вызывает `rootComponent.destroy()`
- `KmpBridgeService.destroy()` отменяет все подписки и разрушает bridge
- `App.ngOnDestroy()` вызывает `bridge.destroy()`

## Сильные стороны текущей схемы

### 1. Web-оболочка действительно тонкая

Angular не тащит в себя доменную логику и не переизобретает экранные машины состояний.

### 2. Поведение централизовано в общем коде

Auth, contacts, корневая навигация и часть realtime/data поведения остаются общими для платформ.

### 3. Граница web-интеграции выражена явно

`kmp/bridge-web` выступает понятной web-ориентированной точкой входа: через него проходят сборка JS bundle, генерация маршрутов и TS-определений.

### 4. Lifecycle bridge-слоя стал управляемым

У приложения появился нормальный путь teardown, что особенно важно для Decompose-подписок, наблюдений `StateFlow` и долгоживущих общих scope'ов.

## Ограничения и компромиссы

### 1. Рантайм bridge-слоя строковый

Даже при generated TS types обмен между Kotlin и TypeScript идёт через JSON-строки. Это значит, что безопасность на этапе компиляции выше, чем на рантайм-границе.

### 2. `kmp/bridge-web` не полностью владеет bridge-рантаймом

Это важный архитектурный нюанс: web bundle собирается из `kmp/bridge-web`, но основная реализация `WebRootBridge` физически живёт в `kmp/core`. Из-за этого легко перепутать точку входа дистрибутива и фактическое место реализации.

### 3. Запуск зависит от динамической загрузки скриптов

Текущая схема с `manifest.json` и последовательной подгрузкой скриптов понятна и контролируема, но:

- увеличивает сложность запуска
- чуть сложнее в дебаге, чем обычный `import`
- зависит от корректного глобального пространства имён

### 4. Обновления состояния крупнозернистые

Angular не перерисовывает всю страницу целиком, но данные приходят полными объектами состояния. Это практично, но не является самым экономным вариантом обмена.

### 5. Нужна дисциплина вокруг обработки ошибок

Большая часть пользовательских сценариев переводит ошибки в UI-state, но для долгоживущих coroutine pipelines и bridge/runtime boundaries важна аккуратная обработка исключений, чтобы не получать "тихие" частичные поломки.

## Практический вывод

Связка `web-app` + KMP строится вокруг тонкой Angular-оболочки, централизованного bridge-слоя и generated tooling для маршрутов и типов. Главный архитектурный компромисс находится в форме bridge-рантайма: он опирается на JSON-снимки и на bridge-код, который логически относится к web-границе, но физически частично расположен в `kmp/core`.
