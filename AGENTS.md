# AGENTS.md - Rules for AI coding assistants (opencode, Claude Code, etc.)

These rules apply to any AI assistant working in this repository. Commits and PRs must
go under the name of the human developer using the assistant, never under the AI's.

## Project conventions

- Spring Boot 3 / Java 21 / Maven. In-memory state with Java collections.
- All code, identifiers, comments and commit messages in English. REST paths keep the names from the case study (`/api/hotel/reservar`, etc.).
- `TASKS.md` is the source of truth for the work plan and business assumptions. Read it before starting.

## Git workflow

1. **Never work on `main`.** Before the first change, create a branch from an up-to-date `main`: `task-<n>-<short-description>` (e.g. `task-1-room-service`). If the work depends on a branch not yet in `main`, branch from that one.
2. **One commit per subtask or logical change**, not one giant commit at the end. Use Conventional Commits in English (`feat(room): add availability search`). Use the task message from `TASKS.md` including its `[x.y]` suffix, and mark the task `[x]` in that same commit.
3. **Before every commit, run `mvn test`** (or `mvn verify`). Do not commit if it fails.
4. **Push the branch** (`git push -u origin <branch>`) when the work is ready.
5. **Open a PR to `main`** (or to the branch it depends on) with a description in Spanish: what changes, why, and how it was tested. Use `gh pr create`; if `gh` is not available, give the `.../pull/new/<branch>` link printed by `git push`.
6. **When finished, hand the PR link to the developer** (and the branch link if the PR could not be opened).

## Forbidden

- `git merge`, `git rebase`, `git reset --hard`, `git tag`, `git push --force`, and direct pushes to `main`. Do not merge PRs: the developer does it.
- `--no-verify` or disabling hooks.
- **AI co-authorship:** no commit or PR may contain `Co-Authored-By: <AI>`, an AI vendor `noreply@` address, or "Generated with ..." footers. Commits go only under the developer's own git identity.
- Committing secrets: make sure `.env`, keys, credentials and personal data never enter a commit. Only `.env.example` is tracked.

## Identity (important)

- Commit with the identity already configured in the developer's git (`git config user.name` / `user.email`). Check it with those commands before the first commit.
- **Never change or override the git identity**, and never set the author to someone else or to an AI.
- If no identity is configured, stop and ask the developer to set it; do not invent one.

## Special cases

- **Uncommitted changes that are not yours** when you start: ask the developer before touching them.
- **Remote without a `main` branch yet** (empty repo): do not do the first push to `main` without asking the developer.
