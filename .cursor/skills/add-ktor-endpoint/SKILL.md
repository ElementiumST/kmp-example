# add-ktor-endpoint

## Purpose
Add a new API endpoint integration in shared data layer.

## Steps
1. Add DTOs in `kmp/data/remote`.
2. Implement Ktor call + serialization.
3. Map DTOs to domain models.
4. Expose method in repository implementation.
5. Add or update error logging in data layer.
