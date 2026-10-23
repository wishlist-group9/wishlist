# Contributing

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
_To be agreed by the group._

## Pull requests
_To be agreed by the group: how many reviewers, what a review must include._

## Definition of Done
_To be agreed by the group._