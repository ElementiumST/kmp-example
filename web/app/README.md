# Web shell

`web/app` это отдельная оболочка на `React + TypeScript`.

Сейчас она умеет работать в двух режимах:

- с fallback-состоянием, чтобы UI можно было поднять сразу;
- с реальным KMP bridge, если сгенерированный JS bundle экспортирует `createWebRootComponent()` в `window.SharedCoreBridge`.

## Дальше для реального bridge

1. Собрать JS library из `kmp/core`.
2. Подключить собранный KMP bundle в Vite app.
3. Пробросить экспорт `createWebRootComponent()` в `window.SharedCoreBridge`.

После этого React-приложение начнет работать через `rootComponent.authComponent` без промежуточного controller-слоя.
