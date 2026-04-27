# Отчет: как Kotlin-код с корутинами работает в `web`, `iOS` и `desktop`

## Контекст этого проекта

В этом репозитории общий Kotlin-код живет в KMP-модулях и уже компилируется минимум под такие платформы:

- `JS` для `web`;
- `iOS` через Kotlin/Native;
- `Android`.

Это видно по текущим target'ам в `kmp/core`, `kmp/data`, `kmp/bridge-web` и `kmp/bridge-apple`: проект собирает `js(IR)`, `iosX64`, `iosArm64`, `iosSimulatorArm64` и `androidTarget`.

Отдельного `desktop`-shell в репозитории сейчас нет. Поэтому раздел про `desktop` ниже описывает, как тот же shared Kotlin-код с корутинами будет работать при добавлении JVM desktop-оболочки, например на Compose Desktop или Swing.

## Базовая модель: что именно общее между платформами

С точки зрения общего KMP-кода корутины работают одинаково по базовой схеме:

1. `suspend`-функции компилируются в машину состояний.
2. `launch` и `async` запускают coroutine job в `CoroutineScope`.
3. `Flow` и `StateFlow` доставляют асинхронные обновления состояния.
4. `Dispatcher` определяет, в какой среде и на каком потоке или event loop будет продолжено выполнение.
5. отмена идет через `Job`, `scope.cancel()` и lifecycle cleanup.

В этом проекте shared-код уже активно использует эту модель:

- в `kmp/core` и feature-модулях создаются `CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)` или `Dispatchers.Default`;
- состояния фич держатся в `MutableStateFlow`;
- жизненный цикл завязан на явный teardown через `destroy()` и отмену scope.

Это важная точка: логика бизнес-состояния и async pipeline в KMP общие, но реальное поведение `Dispatchers.Main`, `Dispatchers.Default`, сетевых клиентов и storage-драйверов зависит от платформы.

## Что в проекте уже платформенно различается

Даже при общем coroutine-коде платформа меняет рантайм вокруг него.

Сейчас это особенно видно в `kmp/data`:

- `androidMain` использует `Ktor OkHttp` и SQLDelight `android.driver`;
- `iosMain` использует `Ktor Darwin` и SQLDelight `native.driver`;
- `jsMain` использует `Ktor JS` и SQLDelight `web.worker.driver`.

Это значит, что shared use case или repository может выглядеть одинаково, но:

- сетевые запросы физически едут через разные движки;
- база и storage имеют разный backend;
- scheduling и выполнение continuation происходят через разный platform runtime.

## Как это работает на `web`

### Что происходит при сборке

Для web проект использует `JS IR` и bridge-слой `kmp/bridge-web`. Общий Kotlin-код компилируется в JavaScript-библиотеку, а web-shell уже обращается к ней из Angular через bridge.

На уровне coroutine runtime это означает:

- `suspend`-функции превращаются в JS state machine;
- continuation продолжаются через JavaScript event loop;
- `Flow`-обновления в итоге доходят до web-оболочки через bridge и сериализованные snapshot'ы состояния.

### Что означает `Dispatchers` на web

На web нет привычной JVM-модели с настоящим набором фоновых потоков для coroutine dispatching. В типичном Kotlin/JS browser-runtime:

- `Dispatchers.Main` привязан к browser event loop;
- `Dispatchers.Default` не дает настоящего CPU-parallel execution как на JVM;
- приостановка через `delay`, network I/O или callback-based API не блокирует UI-поток;
- тяжелая синхронная CPU-работа все равно блокирует страницу.

Практически это значит следующее:

- `launch { ... }` на web не делает код "параллельным" в смысле нескольких CPU-потоков;
- он делает код неблокирующим в терминах ожидания I/O;
- если внутри coroutine запустить тяжелый цикл без suspension points, UI и bridge-обновления начнут тормозить.

### Что это значит для этого проекта

Для текущего приложения на web корутины хорошо подходят для:

- загрузки данных;
- подписок на `StateFlow`;
- websocket/event pipelines;
- оркестрации навигации и screen-state.

Но на web особенно важно:

- не держать тяжелую синхронную обработку в shared coroutine-коде;
- помнить, что `Dispatchers.Default` здесь не эквивалентен JVM thread pool;
- аккуратно закрывать bridge и подписки при уничтожении Angular-оболочки.

## Как это работает на `iOS`

### Что происходит при сборке

Для `iOS` проект собирает `SharedCore.xcframework` через `kmp/bridge-apple`. Shared Kotlin-код компилируется в native framework, которую использует SwiftUI-оболочка.

Здесь корутины уже работают не поверх JS event loop, а поверх Kotlin/Native runtime.

### Что означает `Dispatchers` на `iOS`

На `iOS` важны два практических момента:

- `Dispatchers.Main` соответствует главному UI-потоку;
- `Dispatchers.Default` выполняет фоновые задачи вне главного потока.

Поэтому поведение coroutine-кода здесь ближе к привычной мобильной async-модели:

- UI-связанное состояние можно безопасно обновлять через `Main`;
- фоновые операции, репозитории и часть бизнес-логики могут идти на `Default`;
- `delay` и network I/O не блокируют главный поток;
- cancellation должна быть привязана к lifecycle экрана или root-компонента.

### Что это значит для этого проекта

В проекте root и feature-компоненты уже создают свои scope и требуют явного `destroy()`. Для `iOS` это особенно важно, потому что:

- SwiftUI-оболочка может освободить экран или root;
- если не отменить shared coroutine scope, можно оставить живые подписки и фоновые job;
- при bridge-интеграции утечки coroutine scope часто проявляются как "странные повторные обновления" или дублирующиеся события.

На `iOS` общий Kotlin-код с корутинами в этом проекте будет работать хорошо при соблюдении простого правила: shared-компоненты должны жить ровно столько же, сколько живет соответствующий root или экран в native shell.

### Что меняется по сравнению с web

Основное отличие от web:

- на `iOS` есть реальный UI main thread и фоновые execution context'ы;
- `Dispatchers.Default` здесь полезен именно как background dispatcher, а не просто как другой логический label;
- network и storage-слой идут через native реализации, а не через браузерный runtime.

## Как это будет работать на `desktop`

### Важное уточнение

Сейчас desktop-target в этом репозитории не настроен. Но shared KMP-код написан в стиле, который в целом переносим на JVM desktop, если добавить target и соответствующую оболочку.

### Какая модель будет на JVM desktop

Если добавить `desktop` на JVM, например через Compose Desktop:

- `suspend` и `Flow` будут работать через JVM coroutine runtime;
- `Dispatchers.Default` будет backed JVM thread pool'ом и действительно даст background execution;
- `Dispatchers.IO` при необходимости даст отдельный elastic pool для блокирующего I/O;
- `Dispatchers.Main` потребует UI-specific integration, например Swing/Compose dispatcher.

Это ключевой момент для текущего shared-кода проекта: он уже использует `Dispatchers.Main.immediate` в ряде компонентов. Значит, для desktop нельзя просто "включить JVM target" без платформенного main-dispatcher integration. Иначе код, который ожидает наличие `Main`, может упасть или не стартовать корректно.

### Что это значит практически

На desktop shared coroutine-код будет ближе всего к Android/JVM-модели:

- настоящая многопоточность доступна;
- `Default` подходит для CPU-bound и части фоновой работы;
- UI-state должен возвращаться на desktop main thread;
- lifecycle все равно нужно привязывать к окну, root-компоненту или screen host.

Если в проект будет добавлен desktop-shell, нужно будет отдельно проверить:

- установлен ли `Main` dispatcher для UI;
- где создается и уничтожается root-компонент;
- кто вызывает `destroy()` при закрытии окна;
- нет ли в shared-коде предположений, завязанных только на browser или iOS runtime.

## Сравнение по средам

### `web`

- Выполнение идет через JavaScript event loop.
- `Main` и `Default` не означают настоящую многопоточность как на JVM.
- Отлично подходит для async I/O и UI orchestration.
- Плохо подходит для тяжелой CPU-работы в coroutine без выноса в отдельный механизм.

### `iOS`

- Выполнение идет через Kotlin/Native runtime.
- `Main` привязан к главному UI-потоку.
- `Default` реально полезен для фоновой работы.
- Очень важны lifecycle cleanup и отмена scope при уничтожении native-объектов.

### `desktop`

- Выполнение шло бы через JVM coroutine runtime.
- `Default` дал бы реальный background thread pool.
- Для UI нужен корректно установленный `Main` dispatcher.
- Поведение по threading ближе к Android/JVM, чем к web.

## Что остается одинаковым между всеми платформами

Несмотря на различия runtime, в shared Kotlin-коде остаются одинаковыми:

- `suspend`-контракты;
- structured concurrency;
- `CoroutineScope`, `SupervisorJob`, `Job`;
- `Flow`, `StateFlow`, `MutableStateFlow`;
- правила отмены и обработки ошибок;
- необходимость явно завершать долгоживущие root-компоненты.

Именно поэтому общий KMP-код для состояния экранов, use case'ов и orchestration-логики переносится хорошо: меняется не модель программирования, а platform runtime под ней.

## Практические выводы для этого проекта

### 1. Shared coroutine-код уже написан в переносимом стиле

Текущая архитектура опирается на:

- `CoroutineScope + SupervisorJob`;
- `StateFlow` для состояния;
- явный lifecycle teardown;
- platform-specific data engines при общем API.

Это хороший базис для multi-platform поведения.

### 2. `web` и `iOS` уже поддерживаются, но ведут себя по-разному по threading

Даже если Kotlin-код одинаковый:

- на web корутины в основном кооперативно живут внутри event loop;
- на `iOS` они реально разделяются между main и background execution;
- поэтому одинаковый код может иметь разный performance profile и разную чувствительность к блокирующим операциям.

### 3. Для `desktop` главный риск не в бизнес-логике, а в runtime wiring

Если добавлять desktop-shell, главные проверки будут вокруг:

- наличия `Dispatchers.Main`;
- lifecycle root-компонента;
- интеграции UI-thread и shared state;
- выбора desktop-specific data/storage backend, если он понадобится.

### 4. Главная ошибка - считать `Dispatchers.Default` одинаковым везде

В KMP это не просто "универсальный фон". Его реальное поведение зависит от платформы:

- на web это не полноценный JVM-like pool;
- на `iOS` это background execution context;
- на desktop/JVM это уже полноценный thread pool.

## Краткий итог

Kotlin-код с корутинами в этом проекте переносится между платформами хорошо, потому что общий слой опирается на `suspend`, `Flow`, `StateFlow`, lifecycle-aware `CoroutineScope` и явный teardown. Но исполняется этот код по-разному:

- на `web` - поверх JavaScript event loop;
- на `iOS` - поверх Kotlin/Native runtime с main/background execution;
- на `desktop` - работал бы поверх JVM coroutine runtime, если добавить target и UI dispatcher.

Итоговая мысль простая: общий coroutine-код в KMP переносим, но не "одинаково исполняем". Переносится модель, а поведение `Dispatchers`, threading, I/O backend и lifecycle wiring всегда остаются платформенно зависимыми.
