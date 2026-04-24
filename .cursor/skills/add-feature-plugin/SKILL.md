# add-feature-plugin

## Purpose
Introduce a reusable feature plugin for shared runtime behavior.

## Steps
1. Implement plugin in `kmp/feature/base`.
2. Keep plugin side-effect free except explicit callbacks.
3. Wire plugin into target feature component constructor.
4. Ensure plugin updates are deterministic and testable.
5. Add tests for plugin behavior in common test scope.
