## Summary

- Service(s) touched:
- What changed and why:

## Checklist

- [ ] I own this service, or the owner reviewed the PR
- [ ] `./mvnw -B verify` (or GitHub Actions) is green
- [ ] Swagger still loads for the changed service
- [ ] Validation / error body / roles still apply
- [ ] Tests cover the new rule (happy path + one negative)
- [ ] No secrets, `.env`, `target/`, or H2 `data/` files
- [ ] README or OpenAPI updated if the contract changed
