# Database Migration

- Manage relational database schema changes with Flyway.
- Add a Flyway migration in the same change as every JPA entity schema change.
- Never modify or delete an applied migration. Add `V{next version}__snake_case_description.sql`.
- Use `ddl-auto=validate` in shared environments.
- `ddl-auto=update` is allowed only for temporary experiments on a disposable local database. Add the migration and verify with `validate` before committing.
