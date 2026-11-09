# Contributing
***Note:*** *In English to invite contributors from all over the world to collaborate on SantaClaude.*

## Workflow
1. Pick an issue from the project board and move it to **In Progress**.
2. Branch from `develop`: `git checkout develop && git pull && git checkout -b feature/<short-name>`
3. Push and open a pull request into `develop`, linking the issue ("Closes #12").
4. CI (build, tests, Qodana) must be green before merging.

## Branches
- `main` – stable, deployed to Azure on every push. Only `develop → main` release PRs.
- `develop` – integrated work. Default branch.
- `feature/*` – one issue per branch, deleted after merge.

## Coding guidelines

### Naming
- Code, comments and commits in English; user-facing text in Danish
- Java: `PascalCase` classes, `camelCase` methods/variables, `UPPER_SNAKE_CASE` constants
- Database: `snake_case` tables/columns, singular table names
- URLs and templates: lowercase-with-hyphens

### Architecture
- Controller → Service → Repository; SQL only in repositories, business rules in services
- 
### Database
- JdbcTemplate with `?` placeholders; no string concatenation, no `SELECT *`
- `@Transactional` on multi-table writes
- Table changes in both `schema.sql` and `h2init.sql`

### Errors, logging, security
- Catch specific exceptions; custom exceptions from services; `@ControllerAdvice` for the rest
- Validate input on the server; session holds only the user id; passwords hashed with BCrypt

### Front-end
- Styles only in `static/css`; shared fragments for header/footer; semantic HTML

### Tests
- Service logic → unit test, endpoint → `@WebMvcTest`, SQL → H2 integration test
- `./mvnw clean test` passes before pushing

### Git
- Branches: `feature/<issue>-<short-name>`; imperative commit messages
- One issue per PR; no commented-out code

## Pull requests
- Every PR must be reviewed by another collaborator before merging
- Every PR must contain the standard template (Problem->Solution->How-to-test->Scope)

## Definition of Done
- Code compiles without errors or warnings 
- Manually tested against all acceptance criteria by someone who didn't write it 
- Peer reviewed - at least one other member has read the code 
- JavaDoc written on all classes and public methods 
- Java naming conventions followed 
- Committed to master with a meaningful commit message 
- Class diagram updated if new classes were added 
- No dead code or commented-out blocks remaining 
