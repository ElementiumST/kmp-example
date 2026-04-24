# sync-web-shared-core

## Purpose
Rebuild and sync generated web bridge artifacts into Angular shell.

## Steps
1. Run `./gradlew :kmp:bridge-web:syncBridgeTs`.
2. Run `./gradlew :kmp:bridge-web:jsBrowserDevelopmentLibraryDistribution`.
3. Run `web-app/scripts/sync-shared-core.mjs` (or `npm run sync:shared-core`).
4. Confirm `web-app/public/shared-core/manifest.json` exists.
5. Verify global bridge namespace is loaded in browser.
6. Smoke-test bootstrap in `web-app/src/app/app.ts`.