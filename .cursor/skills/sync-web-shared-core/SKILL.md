# sync-web-shared-core

## Purpose
Rebuild and sync shared web bridge artifacts into Angular shell.

## Steps
1. Run `./gradlew :kmp:bridge-web:jsBrowserDevelopmentLibraryDistribution`.
2. Run `web-app/scripts/sync-shared-core.mjs`.
3. Confirm `web-app/public/shared-core/manifest.json` exists.
4. Verify global bridge namespace is loaded in browser.
5. Smoke-test bootstrap in `web-app/src/app/app.ts`.
