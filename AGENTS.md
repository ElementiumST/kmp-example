# Project Modules For Agents

## Source Of Truth
- Gradle modules are declared in `settings.gradle.kts`.
- `web-app` and `ios/app` are standalone platform shells and should also be treated as project zones.

## Project Map
| Zone | Role | Depends On | Edit Here When |
| --- | --- | --- | --- |
| `android/app` | Android Compose application shell | `kmp/app`, `kmp/navigation` | Android-only UI, manifest, app bootstrapping |
| `kmp/app` | Shared app startup facade | `kmp/core` | Shared app initialization and root factory access |
| `kmp/navigation` | Shared navigation aliases and root contracts | `kmp/core` | Root routing contracts and child-kind integration |
| `kmp/bridge-web` | KMP JS bridge module + generated routes | `kmp/core`, `kmp/tools/*` | Web bridge generation/output and JS exports |
| `kmp/bridge-apple` | Apple bridge facade for framework consumers | `kmp/core` | iOS-facing shared factory API |
| `kmp/core` | Legacy composition module (migration compatibility) | `kmp/data`, `kmp/domain`, `kmp/feature/*` | Existing shared composition during migration window |
| `kmp/data` | Shared data layer with Ktor and SQLDelight | `kmp/domain` | Networking, persistence, repository implementations |
| `kmp/domain` | Shared domain contracts, models, use cases | none inside project | Business models, repository interfaces, use cases |
| `kmp/feature/base` | Shared feature runtime, plugins, lifecycle helpers | none inside project | FlowMVI-aligned runtime helpers and shared feature plumbing |
| `kmp/tools/bridge-annotations` | Source annotations for bridge exposure (bridge modules only) | none inside project | `@ExposeToWeb`, `@BridgeAction`, `@BridgeChild` annotations. MUST NOT be used on shared feature interfaces. |
| `kmp/tools/bridge-codegen` | KSP bridge codegen processor | `kmp/tools/bridge-annotations` | Code generation and TS type emit support |
| `web-app` | Angular + Nx shell over generated KMP bridge types | built `kmp/bridge-web` JS output | Web-only UI, bridge integration, frontend tooling |
| `ios/app` | SwiftUI shell over `SharedCore.framework` | built Apple framework | iOS-only UI and lifecycle integration |

## Dependency Direction
```text
android/app -> kmp/app
android/app -> kmp/navigation
kmp/app -> kmp/core
kmp/navigation -> kmp/core
kmp/bridge-web -> kmp/core
kmp/bridge-apple -> kmp/core
kmp/core -> kmp/data
kmp/core -> kmp/domain
kmp/core -> kmp/feature/base
kmp/data -> kmp/domain
web-app -> kmp/bridge-web bridge output
ios/app -> Shared framework bridge facade
```

## Working Rules
- Prefer the smallest zone that can solve the task.
- If a change only affects Android, Web, or iOS presentation, avoid editing shared KMP modules.
- If a change affects business rules or shared models, start in `kmp/domain`.
- If a change affects API, storage, or repository wiring, start in `kmp/data`.
- If a change affects cross-platform state, root composition, or platform bridges, start in `kmp/app`, `kmp/navigation`, or bridge modules; touch `kmp/core` only for migration compatibility.
- Keep `web-app` and `ios/app` thin: they should adapt native/web UI to shared KMP behavior, not duplicate domain logic.

## Platform Notes
- `kmp/bridge-web` is the preferred web bridge module and route-table source. All web-specific pattern-matching / kind-string logic lives here, never on shared interfaces.
- `kmp/bridge-apple` is the preferred Apple shell facade module. iOS uses `AppleRootAccessor` / `AppleContactsAccessor` instead of shared bridge methods. Builds `SharedCore.xcframework` via `./gradlew :kmp:bridge-apple:assembleSharedCoreXCFramework`.
- `kmp/data` contains platform-specific source sets for HTTP clients and SQLDelight drivers.
- `kmp/domain` and `kmp/feature/base` are currently common-first modules with no project-internal platform splits in Gradle.
- `android/app` should consume shared logic through `kmp/app` and `kmp/navigation`, not directly through `kmp/data` or `kmp/domain`.

## Before Editing
- Read the local module `build.gradle.kts` before changing dependencies or platform targets.
- Check nearby README files for shell-specific setup in `web-app` and `ios/app`.
- Do not treat `.ai/mcp/mcp.json` as module documentation; it is not the project map.
