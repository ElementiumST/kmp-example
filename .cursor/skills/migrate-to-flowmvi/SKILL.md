# migrate-to-flowmvi

## Purpose
Migrate an existing component from legacy base MVI to project FlowMVI-aligned runtime.

## Steps
1. Replace `BaseMviComponent` with `FeatureStoreComponent`.
2. Emit actions through `publishAction`.
3. Wrap async work in `launchSafely`.
4. Attach `RecoverPlugin` and `LoggingPlugin`.
5. Verify Android + shared metadata compilation.
