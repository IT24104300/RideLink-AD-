# Security

RideLink is a student backend. Do **not** commit:

- real passwords, JWT secrets, or connection strings
- `.env` files with live values
- `target/`, H2 `data/` files, or keystores

Local defaults in `application.yml` are **dev-only**. For a demo machine, set `JWT_SECRET` in the environment (same value on all four services).

If you find a leaked secret in Git history, rotate it and tell the group before the viva.
