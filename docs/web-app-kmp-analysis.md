# Анализ связки `web-app` и KMP

## Что это за связка

В этом проекте `web-app` выступает как web-shell над shared-логикой из `kmp/core`.
Angular не содержит основную бизнес-логику приложения, а:

- поднимает собственное приложение;
- динамически загружает JS-артефакт, собранный из `kmp/core`;
- получает exported bridge через `WebBridgeFactory`;
- подписывается на shared state;
- проксирует пользовательские действия обратно в KMP.

Ключевая цепочка выглядит так:

`web-app` -> `data-access-kmp-bridge.ts` -> `WebBridgeFactory` -> `WebRootBridge` -> `RootComponent` -> shared feature components / use cases / data layer.

## Где проходит интеграция

Основные точки интеграции:

- `web-app/scripts/sync-shared-core.mjs`  
  Скрипт вызывает Gradle target `:kmp:core:jsBrowserDevelopmentLibraryDistribution`, копирует JS output в `web-app/public/shared-core` и генерирует `manifest.json`.

- `kmp/core/build.gradle.kts`  
  Включает `js(IR)`, `browser()`, `binaries.library()` и `generateTypeScriptDefinitions()`. Это означает, что `kmp/core` собирается как JS library для web-shell.

- `libs/src/lib/data-access-kmp-bridge/data-access-kmp-bridge.ts`  
  Главный мост со стороны TypeScript. Он:
  - загружает скрипты из `manifest.json`;
  - достает KMP module из `globalThis['kmp-example.kmp:core']`;
  - находит `WebBridgeFactory`;
  - создает bridge через `create(baseUrl)`;
  - подписывается на root state, feature state и child navigation state.

- `kmp/core/src/commonMain/kotlin/com/example/kmpexample/kmp/core/bridge/web/WebRootBridge.kt`  
  Kotlin bridge, экспортируемый в JS через `@JsExport`. Он оборачивает shared `RootComponent` и отдает наружу:
  - текущие child kind;
  - полные JSON-снимки состояний;
  - методы действий для auth / contacts / info / editor.

- `web-app/src/app/app.ts`  
  Инициализирует bridge и синхронизирует Angular router с текущим состоянием KMP.

## Как проходит инициализация на web

1. `web-app/src/main.ts` вызывает `bootstrapApplication(...)`.
2. Рендерится корневой `App`.
3. В `web-app/src/app/app.ts` в `ngOnInit()` вызывается `bridge.initialize('/api/rest')`.
4. `KmpBridgeService.initialize()` загружает `manifest.json` и затем последовательно грузит все JS-файлы из `web-app/public/shared-core`.
5. После загрузки service достает `WebBridgeFactory` из глобального namespace KMP.
6. `WebBridgeFactory.create(baseUrl)` создает `WebRootBridge`, а тот внутри поднимает shared `RootComponent` через `SharedApp.createRootComponent(...)`.
7. После этого Angular подписывается на root child, contacts child и feature state.
8. Когда bridge готов, `App` начинает синхронизировать URL браузера с текущим shared-navigation state.

## Сильные стороны

### 1. Тонкий web-shell

`web-app` в основном не дублирует бизнес-логику. Компоненты Angular проксируют действия в `KmpBridgeService`, а само состояние и правила переходов живут в KMP.

Плюсы:

- меньше риска расхождения поведения между платформами;
- общая логика auth и contacts остается в shared-слое;
- web легче поддерживать как адаптер UI.

### 2. Единый source of truth для состояния

Основной state живет в KMP-компонентах, а web читает уже готовое состояние.

Это полезно потому, что:

- web не принимает архитектурные решения сам;
- Angular UI является отображением shared-state;
- изменения бизнес-логики происходят в одном месте.

### 3. Навигация управляется из shared-слоя

`DefaultRootComponent` и `DefaultContactsComponent` используют Decompose stack navigation, а web лишь отражает текущее child-состояние в URL.

Это делает поведение экранов более единым между платформами.

### 4. Shared JS bundle собирается явно и предсказуемо

Связка с `sync-shared-core.mjs` и `manifest.json` делает процесс загрузки bridge прозрачным:

- KMP собирается отдельно;
- web-shell потребляет готовый артефакт;
- зависимости AMD-скриптов раскладываются в правильном порядке.

## Слабые стороны и нюансы

### 1. Граница между Kotlin и web построена на JSON, а не на типобезопасном runtime API

`WebRootBridge` сериализует state в JSON-строки, а `data-access-kmp-bridge.ts` затем делает `JSON.parse`.

Последствия:

- между Kotlin-моделями и TypeScript-моделями нет жесткой runtime-гарантии;
- изменение поля в Kotlin требует синхронного обновления bridge и TS-типов;
- ошибки сериализации/рассинхронизации могут проявляться только во время выполнения.

### 2. На web есть ручное дублирование контрактов

В `data-access-kmp-bridge.ts` вручную описаны типы:

- `AuthState`
- `ContactsListState`
- `ContactInfoState`
- `ContactEditorState`
- `RootChildKind`
- `ContactsChildKind`

Это создает риск расхождения с Kotlin-структурами и enum-значениями.

### 3. Навигация частично продублирована

С одной стороны, реальная активная навигация живет в KMP через Decompose.
С другой стороны, в `web-app/src/app/app.ts` есть `mapRoute(...)`, который вручную переводит `RootChildKind` и `ContactsChildKind` в Angular routes.

Это значит:

- URL в Angular не является первичным источником истины;
- есть второй слой mapping-логики;
- при изменении shared navigation можно забыть обновить web routing mapping.

### 4. Bootstrap web зависит от динамической загрузки множества скриптов

`KmpBridgeService` грузит файлы из `manifest.json` последовательно.

Это рабочая схема, но у нее есть ограничения:

- дольше startup;
- сложнее дебажить, чем стандартный import/npm dependency;
- web-shell зависит от корректного глобального namespace в `globalThis`.

### 5. Жизненный цикл компонентов и подписок не доведен до явного завершения

В проекте bridge и shared root создаются через standalone `ComponentContext`, но явного destroy lifecycle в найденном коде нет.
Также `KmpBridgeService` умеет отменять подписки при повторном bind, но не реализует отдельный teardown на уничтожение приложения.

Это не гарантированная утечка, но архитектурно это слабое место.

## Как обновляется state

### Источник state

Экранный state живет в `BaseMviComponent` через:

- `MutableStateFlow`
- `StateFlow`
- `watchState(observer)`

`watchState()`:

- сразу отдает текущее состояние;
- затем подписывается на `state.collectLatest { ... }`;
- возвращает `StateSubscription`, который может отменить coroutine job.

### Как state попадает в web

Путь обновления такой:

1. Kotlin-компонент меняет `MutableStateFlow`.
2. `watchState()` получает новое значение.
3. `WebRootBridge` сериализует полный state в JSON.
4. Колбэк в `KmpBridgeService` получает строку.
5. TypeScript делает `JSON.parse(...)`.
6. Результат кладется в Angular `signal`.

Примерно это выглядит как:

`MutableStateFlow` -> `collectLatest` -> `to*Json()` -> JS callback -> `parseJson()` -> `signal.set(...)`

### Как обновляется navigation state

Навигация обновляется отдельно от screen state:

- `DefaultRootComponent.watchChildKind(...)` подписывается на Decompose `stack.subscribe(...)`;
- `DefaultContactsComponent.watchChildKind(...)` подписывается на `childStack.subscribe(...)`;
- `KmpBridgeService` обновляет `rootChildKind` и `contactsChildKind`;
- `web-app/src/app/app.ts` через `effect(...)` синхронизирует Angular router с этим значением.

## Обновляется ли весь экран или только измененная часть

Ответ состоит из двух уровней.

### На границе KMP -> JS

Обновляется не patch и не отдельное поле, а полный snapshot состояния.

Например, при каждом обновлении contacts list в JSON уходит весь объект:

- `query`
- `total`
- `isLoading`
- `items`
- `presence`
- `addOverlay`
- и другие поля

То есть на мосте между KMP и web обновление идет целиком по всему state объекта.

### На уровне Angular UI

Angular не делает полный reload страницы.
После `signal.set(...)` он пересчитывает зависимости и обновляет только те части шаблона, которые зависят от этого сигнала.

Но есть важный нюанс:

- так как в `signal` кладется новый объект целиком, все вычисления, читающие этот signal, будут вычислены заново;
- при этом DOM-обновление уже делает Angular максимально локально.

### Что это означает на практике

- В браузере не происходит "перерисовка всего приложения с нуля" в грубом смысле.
- Но с точки зрения данных почти всегда передается и заменяется целый state object.
- Эффективность UI зависит уже от механики Angular signals и шаблонов.

### Есть ли локальная оптимизация

Да. В списке контактов используется `@for (...; track ...)`, поэтому Angular может сохранять DOM-элементы списка между обновлениями, если ключ контакта не изменился.

Это улучшает рендер списка, но не меняет того факта, что bridge передает полный state snapshot.

## Как работают корутины на web-части

### Где создаются coroutine scope

В проекте несколько ключевых мест создают свои `CoroutineScope(SupervisorJob() + Dispatchers.Default)`:

- `BaseMviComponent`
- `DefaultRootComponent`
- `DefaultAuthComponent`
- `DefaultContactsComponent`
- `DefaultContactInfoComponent`
- `DefaultContactEditorComponent`
- `ContactsWebSocketClient`

### Что это означает в Kotlin/JS

На web здесь нет обычных JVM threads.
Корутины Kotlin/JS планируются поверх JS runtime / event loop.

Практический смысл:

- `launch`, `collect`, `delay` работают как асинхронные задачи;
- сетевые операции и websocket не блокируют интерфейс синхронно;
- тяжелая CPU-работа в Kotlin/JS все равно конкурирует за тот же JS runtime.

### Почему используется `SupervisorJob`

Это хорошее решение для UI/feature scope:

- падение одного дочернего job не обязано автоматически уронить все остальные;
- отдельные задачи можно отменять локально;
- проще поддерживать независимые операции.

### Где видно локальную отмену

В `DefaultContactsComponent` есть:

- `searchJob?.cancel()`
- `addSearchJob?.cancel()`

Это используется для debounce-поиска и повторного запуска новых запросов.

### Где есть риск

Хотя подписки `watchState()` и некоторые child subscriptions умеют отменяться, в найденном коде не видно полноценного механизма завершения всего shared root lifecycle.

Проблемные точки:

- `createStandaloneComponentContext()` создает `LifecycleRegistry()`, но явного destroy не видно;
- у service bridge нет общего `dispose()`/`ngOnDestroy()` для финального teardown;
- долгоживущие coroutine scope выглядят слабо привязанными к завершению жизненного цикла web-приложения.

Это особенно важно для:

- подписок на `StateFlow`;
- Decompose `subscribe(...)`;
- websocket/event loops.

## Как устроен event flow на web

Особенно показателен `ContactsWebSocketClient`.

Он:

- держит отдельный `CoroutineScope`;
- открывает websocket при первом подписчике;
- закрывает соединение, когда подписчиков больше нет;
- реконнектится с exponential backoff;
- публикует события через `MutableSharedFlow<ContactEvent>`.

Это сильная сторона data-layer:

- связь с сервером живет в shared-коде;
- web не реализует websocket руками;
- real-time события унифицируются для всех платформ.

Но и тут жизненный цикл зависит от корректного `acquire()` / `release()` и общей дисциплины отмены.

## Что происходит, если ошибка возникает в Kotlin

### Сценарий 1. Ошибка в обычной бизнес-операции

Во многих местах используется `runCatching { ... }.onFailure { ... }`.

Это есть, например, в:

- `DefaultAuthComponent`
- `DefaultContactsComponent`
- `DefaultContactInfoComponent`
- `DefaultContactEditorComponent`

Что происходит в таком случае:

- ошибка не пробрасывается наружу как необработанное исключение;
- вместо этого в state записывается `errorMessage` или похожее состояние;
- web получает обновленный state и показывает ошибку в UI.

Это сильная сторона текущего решения.

### Сценарий 2. Ошибка в bridge bootstrap

Если KMP scripts не загрузились или `WebBridgeFactory` не найден:

- исключение поднимется в `initialize()`;
- `web-app/src/app/app.ts` ловит его в `try/catch`;
- в `bootstrapError` записывается сообщение;
- пользователю показывается экран ошибки инициализации bridge.

Это хороший и понятный fallback.

### Сценарий 3. Ошибка в Kotlin coroutine без локального перехвата

Вот здесь уже есть риск.

В проекте найдены места, где coroutine `collect { ... }` работает без явного `runCatching` вокруг самого потока или обработчика:

- `DefaultRootComponent.observeAuthorized()`
- `DefaultContactsComponent.observeEvents()`

Также в найденном коде не видно:

- `CoroutineExceptionHandler`
- общего global Kotlin error boundary для web
- централизованного конвертера необработанных исключений в UI-state

### Что это значит practically

Если ошибка произошла в коде, который обернут в `runCatching`, пользователь, скорее всего, увидит controlled UI error.

Если ошибка произошла в coroutine без локальной обработки, возможны такие последствия:

- отменится конкретная coroutine;
- часть shared-логики перестанет обновляться;
- ошибка уйдет в консоль;
- UI не обязательно упадет полностью, но может остаться в частично "подвисшем" состоянии без понятного сообщения пользователю.

Именно это сейчас выглядит одной из главных архитектурных слабостей web-связки.

## Итоговая оценка

### Сильные стороны

- `web-app` действительно тонкий и не тащит в себя доменную логику.
- KMP выступает единым источником истины для состояния и переходов.
- Shared navigation и data-flow централизованы.
- WebSocket и data behavior вынесены в shared-код.
- Ошибки большинства пользовательских операций уже переводятся в управляемый UI-state.

### Слабые стороны

- Bridge основан на полном JSON snapshot, а не на типобезопасном инкрементальном обмене.
- TypeScript-контракты частично дублируют Kotlin-модели вручную.
- Angular routing дублирует часть навигационного знания KMP.
- Startup зависит от runtime-загрузки множества скриптов.
- Lifecycle shared root и coroutine scopes не выглядит полноценно завершенным.
- Нет централизованной обработки необработанных исключений в Kotlin/JS.

## Практический вывод

Архитектурно эта связка хороша как thin-shell модель: web-часть действительно адаптирует shared KMP, а не пытается заново реализовать приложение.

Главная цена такого подхода в текущей реализации:

- данные между слоями гоняются крупными JSON-снимками;
- типовая безопасность на границе Kotlin/TS ограничена;
- ошибки и lifecycle закрыты не полностью.

Если говорить коротко:

- как shared-platform architecture решение выглядит сильным;
- как runtime bridge между Kotlin/JS и Angular реализация пока скорее практичная и рабочая, чем строгая и безопасная.
