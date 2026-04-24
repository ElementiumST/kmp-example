# Web-приложение (Angular + Nx)

Актуальная web-оболочка проекта находится в `web-app`.

## Основные команды

- Dev server: `npm run dev:web`
- Сборка: `npm run build:web`
- Линт: `npm run lint:web`
- Юнит-тесты: `npm run test:web`

## Интеграция KMP

- Перед `serve` и `build` автоматически выполняется target `web-app:sync-shared-core`.
- Скрипт `web-app/scripts/sync-shared-core.mjs`:
  - запускает `:kmp:bridge-web:jsBrowserDevelopmentLibraryDistribution`
  - копирует JS bundle в `web-app/public/shared-core`
  - генерирует `manifest.json` с порядком загрузки UMD-скриптов
- Angular bridge сначала ищет KMP bundle в `kmp:bridge-web`, затем делает fallback на `kmp:core`.
- Runtime `WebBridgeFactory` / `WebRootBridge` сейчас реализованы в `kmp/core`, а `kmp/bridge-web` выступает как web-facing distribution entrypoint с generated route table и TS definitions.

## Прокси

Локальный dev-proxy настроен в `web-app/proxy.conf.json`:

- `/api` -> `https://alpha.hi-tech.org`
- `/ws` -> `wss://alpha.hi-tech.org`
