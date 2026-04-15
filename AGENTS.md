# Project Modules For Agents

## Source Of Truth
- Gradle modules are declared in `settings.gradle.kts`.
- `web/app` and `ios/app` are standalone platform shells and should also be treated as project zones.

## Project Map
| Zone | Role | Depends On | Edit Here When |
| --- | --- | --- | --- |
| `android/app` | Android Compose application shell | `kmp/core` | Android-only UI, manifest, app bootstrapping |
| `kmp/core` | Shared app shell, root component, bridges for Apple/Web | `kmp/data`, `kmp/domain`, `kmp/feature/base` | Cross-platform composition, navigation, shared state wiring |
| `kmp/data` | Shared data layer with Ktor and SQLDelight | `kmp/domain` | Networking, persistence, repository implementations |
| `kmp/domain` | Shared domain contracts, models, use cases | none inside project | Business models, repository interfaces, use cases |
| `kmp/feature/base` | Shared feature primitives and MVI base types | none inside project | Base feature abstractions and reusable state machinery |
| `web/app` | React + TypeScript shell over the KMP web bridge | built `kmp/core` JS output | Web-only UI, bridge integration, frontend tooling |
| `ios/app` | SwiftUI shell over `SharedCore.framework` | built `kmp/core` Apple framework | iOS-only UI and native integration |

## Dependency Direction
```text
android/app -> kmp/core
kmp/core -> kmp/data
kmp/core -> kmp/domain
kmp/core -> kmp/feature/base
kmp/data -> kmp/domain
web/app -> kmp/core bridge output
ios/app -> SharedCore.framework from kmp/core
```

## Working Rules
- Prefer the smallest zone that can solve the task.
- If a change only affects Android, Web, or iOS presentation, avoid editing shared KMP modules.
- If a change affects business rules or shared models, start in `kmp/domain`.
- If a change affects API, storage, or repository wiring, start in `kmp/data`.
- If a change affects cross-platform state, root composition, or platform bridges, start in `kmp/core`.
- Keep `web/app` and `ios/app` thin: they should adapt native/web UI to shared KMP behavior, not duplicate domain logic.

## Platform Notes
- `kmp/core` exports the shared bridge used by Web and Apple clients.
- `kmp/data` contains platform-specific source sets for HTTP clients and SQLDelight drivers.
- `kmp/domain` and `kmp/feature/base` are currently common-first modules with no project-internal platform splits in Gradle.
- `android/app` consumes shared logic through `kmp/core`, not directly through `kmp/data` or `kmp/domain`.

## Before Editing
- Read the local module `build.gradle.kts` before changing dependencies or platform targets.
- Check nearby README files for shell-specific setup in `web/app` and `ios/app`.
- Do not treat `.ai/mcp/mcp.json` as module documentation; it is not the project map.
