# expose-component-to-web

## Purpose
Expose/extend web bridge API through annotations and KSP generation.

## Steps

1. In shared models used by web JSON states, annotate new DTO/state classes with `@BridgeModel`.
2. In `kmp/core/src/commonMain/.../WebRootBridge.kt`:
   - add bridge method;
   - annotate with `@BridgeAction(role = ...)`;
   - for kind methods, add `@BridgeStringUnion(...)`.
3. Regenerate TS bridge artifacts:
   - `./gradlew :kmp:bridge-web:syncBridgeTs`
4. Rebuild and sync runtime JS bundle:
   - `./gradlew :kmp:bridge-web:jsBrowserDevelopmentLibraryDistribution`
   - `npm run sync:shared-core`
5. Validate Angular shell uses new API from `data-access-kmp-bridge` package.

## Do NOT
- Manually edit `libs/src/lib/data-access-kmp-bridge/generated/*.ts`.
- Add web-only pattern matching to shared interfaces.