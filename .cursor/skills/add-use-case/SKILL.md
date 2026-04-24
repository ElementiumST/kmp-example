# add-use-case

## Purpose
Add a new domain use case and wire it through data and composition layers.

## Steps
1. Add use case in `kmp/domain`.
2. Extend repository contract if needed.
3. Implement repository method in `kmp/data`.
4. Register use case in DI modules.
5. Consume use case from feature component and add tests.
