# Contributing to RideLink

This repository is the IT3130 group submission. Contribution is judged by quality, continuity and traceability — not commit count.

## Branch naming

```
feature/<service>/<short-description>
fix/<service>/<short-description>
docs/<topic>
chore/<topic>
```

Examples:

- `feature/account-service/suspend-endpoint`
- `feature/ride-service/no-driver-path`
- `docs/architecture-sequence`

## Workflow

1. Pull latest `main`.
2. Create a feature branch from `main`.
3. Keep commits focused and written in the imperative mood (`Add eligible-driver filter tests`).
4. Open a pull request into `main`.
5. Request review from at least one other member. The **service owner** should review (or be co-author on) changes to their service.
6. CI (GitHub Actions) must be green before merge.
7. Prefer squash or a short rebase so `main` stays demonstrable.

Do not push secrets. Do not commit `target/`, `.env`, or H2 data files.

## One owner per service

| Service | Owner |
| --- | --- |
| Account Service | Member 1 |
| Driver & Vehicle Service | Member 2 |
| Ride Management Service | Member 3 |
| Fare & Payment Service | Member 4 |

Shared contracts (error body, JWT claims, ride status enum, fare formula) are **group decisions**. Change them in a PR that all owners can see.

Architecture, API contracts, and integration are joint work even though each member owns one service.

## Review checklist

- [ ] Service still boots and Swagger loads
- [ ] Validation and the common error body are used
- [ ] Roles are enforced where relevant
- [ ] Unit tests cover the new rule (happy + at least one negative)
- [ ] No secrets or local DB files
- [ ] README / OpenAPI updated if the contract changed

## Identity

Commit with **your own** Git name and institute email so the viva can trace authorship. Do not share accounts.
