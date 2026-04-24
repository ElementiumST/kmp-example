# run-kmp-check

## Purpose
Run cross-platform sanity checks after shared architecture changes.

## Steps
1. Compile shared metadata for changed modules.
2. Build Android debug app (`:android:app:assembleDebug` or `compileDebugKotlin`).
3. Build bridge-web metadata and JS distribution.
4. Run web tests/build if bridge service changed.
5. Collect and summarize actionable failures by module.
