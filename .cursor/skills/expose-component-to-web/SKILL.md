# expose-component-to-web

## Purpose
Expose a shared KMP component to the web shell without polluting its multiplatform contract.

## Golden rule
Bridge wiring (pattern-matching, stringly-typed navigation, JSON shims, `@JsExport`
surface) lives in `kmp/bridge-web`. Shared interfaces stay minimal: MVI state, MVI
intents and, when needed, standard Decompose `Value<ChildStack<*, Child>>`.

## Steps

1. In the shared feature interface (`AuthComponent`, `ContactsComponent`, ...):
   - Add the pure MVI method that wraps a domain action (e.g. `fun refresh() = onAction(Refresh)`).
   - Keep the shared surface free of bridge annotations and `currentXxxOrNull()`-style helpers.

2. In `kmp/bridge-web/.../WebRootBridge.kt`:
   - Add the web-facing method (`fun myAction() { ... }`) that resolves the active
     child via the Decompose `stack` and forwards the call.
   - Reuse existing private helpers (`authComponentOrNull`, `contactsComponentOrNull`,
     `infoComponentOrNull`, `activeEditorComponentOrNull`) or add a new one.
   - If you need a new JSON state shape, add a private `toXxxJson()` function in
     the same file.

3. In `libs/src/lib/data-access-kmp-bridge/generated/bridge-types.ts`:
   - Add/extend the TypeScript type used from Angular.
   - Keep route kinds as string-union types, not Kotlin enums.

4. In `libs/src/lib/data-access-kmp-bridge/data-access-kmp-bridge.ts`:
   - Expose the new method / signal through `KmpBridgeService`.

5. Rebuild the web bundle:
   - `./gradlew :kmp:bridge-web:jsBrowserDevelopmentLibraryDistribution`
   - `npm run sync:shared-core`

## Do NOT
- Place `@ExposeToWeb` / `@BridgeAction` / `@BridgeChild` on shared interfaces.
- Add `currentXxxOrNull()`, `currentChildKind()`, `watchChildKind()` on shared interfaces.
- Depend on `kmp:tools:bridge-annotations` from `kmp:feature:*` or `kmp:core`.
