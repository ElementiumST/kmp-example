# Web App (Angular + Nx)

Актуальная web-оболочка проекта находится в `web-app`.

## Запуск

- Dev server: `npm run dev:web`
- Build: `npm run build:web`
- Lint: `npm run lint:web`
- Unit tests: `npm run test:web`

## Интеграция KMP

- Перед `serve` и `build` автоматически выполняется target `web-app:sync-shared-core`.
- Скрипт `web-app/scripts/sync-shared-core.mjs`:
  - запускает `:kmp:core:jsBrowserDevelopmentLibraryDistribution`
  - копирует JS bundle в `web-app/public/shared-core`
  - генерирует `manifest.json` с порядком загрузки UMD-скриптов
- Angular bridge использует `WebBridgeFactory` из `kmp/core` и проксирует auth/contacts функционал в UI.

## Proxy

Локальный dev proxy настроен в `web-app/proxy.conf.json`:

- `/api` -> `https://alpha.hi-tech.org`
- `/ws` -> `wss://alpha.hi-tech.org`
