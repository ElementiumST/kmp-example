# add-kmp-feature

## Purpose
Scaffold a new KMP feature module aligned with this project architecture.

## Steps
1. Create feature module under `kmp/feature/<name>`.
2. Add `component`, `model`, `di` packages.
3. Base feature runtime on `FeatureStoreComponent`.
4. Add bridge annotations for web-facing methods.
5. Register feature DI and wire it in shared app/navigation layers.
