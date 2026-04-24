# add-angular-screen

## Purpose
Add a new Angular screen that renders shared KMP state via bridge signals.

## Steps
1. Create route/component under `web-app/src/app`.
2. Consume state from `KmpBridgeService` signals.
3. Use generated types from `data-access-kmp-bridge/generated`.
4. Trigger shared actions through service methods only.
5. Ensure teardown-safe behavior and route synchronization.
