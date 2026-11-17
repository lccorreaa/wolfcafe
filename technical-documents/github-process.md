# GitHub Process

GitHub is used throughout CSC 326 for version control and teaming.  This document covers the branching, pull request, and issue processes every WolfCafe team must follow.  Your team's [Ground Rules](../project-management/ground-rules.md) record how you will meet them.  **Note: this course uses GitHub Enterprise Cloud for submission repos.**

## Branches

Branches provide a place to experiment and work without breaking `main`.  `main` represents a working, deployable product.

- **`main`** - the deliverable version of the project.  It must always build, pass all tests, and be deployable.  No direct commits.
- **`development`** - where new features and bug fixes are integrated and system tested before they reach `main`.  Create it from `main` at the start of the project; the name is all lower case.
- **Feature branches** - created from `development`.  Each feature has three:
  - `<feature>-backend` - development and testing of the backend
  - `unityID/<feature>-frontend-data-entry` - development and testing of frontend data entry
  - `unityID/<feature>-frontend-report` - development and testing of frontend reporting and display
- **Other branches** - smaller tasks and bug fixes may have their own branches, named `unityID/branchName` so ownership is easy to see.  Bringing in your team's CoffeeMaker functionality is the one exception to feature-based branches.
- **Project management branches** - see below.

Record your branches and their owners in the [Project Management Plan](../project-management/index.md#feature-based-roles).

### Project Management Branches

Changes to the documents in `project-management/` (the plan, the task sheets, team goals, ground rules, and AI policy) are made on a branch and delivered by pull request, the same as code - never committed directly to `development` or `main`.  Your team chooses one approach and records it in your [Ground Rules](../project-management/ground-rules.md):

- **One branch for the whole project**, named `project-management`.  After each pull request merges, merge `development` back into it before the next round of updates.
- **One branch per iteration part**: `project-management-i0`, `project-management-i1a`, `project-management-i1b`, `project-management-i2a`, `project-management-i2b`, and `project-management-final`, each created from `development` when the part starts.

The names use a hyphen rather than a slash because Git cannot have both a `project-management` branch and a `project-management/...` branch.

Either way, every team member commits their own updates (their task status and hours, their approval rows) in their own commits, each commit references an issue, and the branch is merged by pull request to `development` and then `main` by the deadline of each iteration part.

### Technical Document Branches

Changes to the documents in `technical-documents/` are made on branches and delivered by pull request too.  Which branch depends on whether the change stands on its own or goes with code:

- **Document-only work** - where the document is the deliverable - goes on its own branch named `docs-<part>-<topic>`: for example `docs-i0-investigation` (CoffeeMaker investigation), `docs-i0-requirements` (requirements and system test plan), `docs-i1a-design-orders` (a feature design), `docs-i1a-inspection-fixes` (changes from an inspection), or `docs-users-guide`.
- **Documents that change because code changed** go on the **feature branch** with the code, so one pull request shows both: the actual results of that feature's system tests, new or changed endpoints in `design/rest-api.md`, a use case's logging details, and design corrections found while implementing.

Document branches follow the same rules as project management branches: each member commits their own contributions, each commit references an issue, they merge by pull request to `development` and then `main` by the part's deadline, and they are listed in the Branches and Owners table.  Keep `project-management/` and `technical-documents/` changes on separate branches.

Your team decides, and records in your [Ground Rules](../project-management/ground-rules.md):

- whether REST API design changes go on their own shared document branch or on the feature's `<feature>-backend` branch, and
- how to organize `system-tests.md` so that feature branches updating actual results don't collide (for example, grouping tests by use case so each sub-team edits only its own section).

## Pull Requests

Changes reach `development` and then `main` only through pull requests:

- **Feature branch → `development`.**  Reviewed by another team member before merging.
- **`development` → `main`.**  Only once `development` is green.  Reviewed by a member of the *other* sub-team.  Bypassing `development` with a pull request straight to `main` is a deduction.

The reviewer inspects the changes, which should take longer than five minutes, and leaves a comment that approves them or asks for more work.  Any commits added after that must be inspected too.  A review confirms that:

- the architecture of the system is maintained;
- the [departmental style guidelines](static-analysis.md) are met;
- tests are high quality, and all tests pass;
- all static analysis notifications are resolved;
- code is commented, with Javadoc up to date on every class and method.

Note any exceptions in the pull request itself.  If AI-generated code or tests are part of the change, acknowledge it the way your team's [AI Policy](../project-management/ai-policy.md) specifies.  Never delete a merged branch; the history is part of what's evaluated.

The [GitHub Actions build](static-analysis.md#static-analysis-in-github-actions) runs on every push, and its checks appear on the pull request: **Build backend**, **Lint frontend**, and **Quality**.  Wait for all three before merging.  A red Quality check means a Checkstyle problem, an ESLint problem, or coverage below the minimums.  Problems in the changed files are marked beside the code in the pull request's **Files changed** tab, and every problem is listed on the run's summary page.  See [The Quality check](static-analysis.md#the-quality-check).

## Green `main`

`main` must stay green: its latest GitHub Actions run passes, including the Quality check.  If you run into build failures on `development`, let the teaching staff know with enough time to figure out what's going on.  Make at least one pull request from `development` to `main` in each part of each iteration - at least one a week.

A feature branch with work in progress may have a red Quality check while the build itself stays green: a failing test turns **Build backend** red, but a coverage shortfall, a Checkstyle violation, or an ESLint problem turns only **Quality** red.  Resolve the Quality problems before the work counts as done; see [Maven Build](maven-build.md#test-coverage) for the coverage minimums.

## Issues

GitHub Issues record tasks and bugs.  An issue can be assigned to the developer who owns it, and all team members can contribute to the discussion.

- Document every enhancement task, general task, and bug as an issue, with a meaningful name.
- Assign every issue to a team member, taking feature-based roles into account.
- Reference the issue in every commit message with a `#` mention, e.g. `Add item price validation #12`.  Writing `Fixes #12` in a pull request merged into `main` closes the issue automatically.
- Close each issue when its work is delivered.  At the end of the project, all issues should be closed unless the issue is a bug found during development and not fixed.

**Bug reports must include:**
- **Reproduce** - the steps to reproduce the bug.
- **Isolate** - other conditions you tested, to understand the extent of the bug.
- **Generalize** - whether the same bug happens in other similar situations.

## Resources

- [GitHub Pull Requests Documentation](https://docs.github.com/en/pull-requests/collaborating-with-pull-requests/proposing-changes-to-your-work-with-pull-requests/about-pull-requests)
- [GitHub Flow](https://docs.github.com/en/get-started/using-github/github-flow)
