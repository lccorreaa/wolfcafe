# WolfCafe Project Management Plan

This plan is operational for your team, and you will use it to discuss your progress with your manager at each check-in.

## Team

* [Team Goals](team-goals.md)
* [Ground Rules](ground-rules.md)
* [AI Policy](ai-policy.md)

## Introduction

### Goals & Benefits

What are the goals of the enhancements, maintenance, and configuration issues the project asks for?  What would a finished solution look like?  What are the benefits of the solution?
- We aim to finish the project with a working system that meets all requirements plus enhancements such as automated frontend testing. A finished solution is one that meets all requirements, is delivered on time, and can continue to be maintained and enhanced in future iterations. The benefits of this solution are that there is room for future changes and does not leave the system in a state that is difficult to maintain or enhance.

## Feature-based Roles

How will your team split into two feature-based sub-teams of 2-3 people?  Who is on each sub-team, and how might the sub-teams change between Iteration 1 and Iteration 2 so members gain experience in different roles?  Identify a team leader and sub-team leaders if you choose to have them.

| Sub-team | Members              | Leader |
|----------|----------------------|--------|
| 1        | Shreya, Raina, Lucas | --     |
| 2        | Sandra, Shion, Lylan | --     |

For now, we will be in our onboarding teams for working on backend and frontend implementation. As the project goes on, we can change the teams depending on who would like to work on what use case.

### Branches and Owners

TODO - List the topic branches you foresee and who owns each.  See [GitHub Process](../technical-documents/github-process.md) for the branch naming conventions.

| Branch                           | Owner  | Feature / Issue      |
|----------------------------------|--------|----------------------|
| lccorea/<staff-frontend-and-api> | Lucas  | UC8, UC9             |
| shstric2/<admin->                | Shion  | UC4                  |
| rwyowell/<i1A-design-staff>      | Raina  | UC10, UC11           |
| snkamath/<staff-backend>         | Shreya | UC8, UC9, UC10, UC11 |
| sajosen/<admin->                 | Sandra | UC5                  |
| labui/<admin->                   | Lylan  | UC6, UC7             |

## Process Plan

We will aim to finish our individual work before the team-set deadline so that other team members have time to review the pull requests and update the code if needed. For items like the requirements and project management, we will work on a common branch so we don't need to do too many pull requests.

## Development Plan

Each sub-team delivers at least one use case from the problem statement by the end of each iteration.

Break the project into tasks small enough to be assigned to one person and realistically completed in a single day, and record each as a GitHub issue.  Estimate each task in person-hours; as tasks are completed, record the actual hours.  Any task not delivered by the end of an iteration is copied to the next one (rolled over).

- Front page implementations
  - UC1: Customer main menu
  - UC4: Admin main menu
  - UC8: Staff main menu
- Other Features
  - Staff Object Creation
  - Customer Object Creation
  - Admin Object Creation
  - Login and Logout Features
  - System Testing for each use case
- Use Case Implementations
  - UC2: Backend implmentation
  - UC2: Frontend implementation
  - UC2: API implementation
  - UC3: Backend implementation
  - UC3: Frontend implementation
  - UC3: API implementation
  - UC5: Backend implementation
  - UC5: Frontend implementation
  - UC5: API implementation
  - UC6: Backend implementation
  - UC6: Frontend implementation
  - UC6: API implementation
  - UC7: Backend implementation
  - UC7: Frontend implementation
  - UC7: API implementation
  - UC9: Backend implementation
  - UC9: Frontend implementation
  - UC9: API implementation
  - UC10: Backend implementation
  - UC10: Frontend implementation
  - UC10: API implementation
  - UC11: Backend implementation
  - UC11: Frontend implementation
  - UC11: API implementation

**Task status**

* **None** - loaded into the iteration, but not worked on yet
* **In Progress** - actively being worked on
* **Acceptance** - the unit tests and system tests for the story pass
* **Delivered** - in `main` after acceptance testing

**Tasks by iteration**

* [Iteration 0 Tasks](tasks-i0.md)
* [Iteration 1A Tasks](tasks-i1a.md)
* [Iteration 1B Tasks](tasks-i1b.md)
* [Iteration 2A Tasks](tasks-i2a.md)
* [Iteration 2B Tasks](tasks-i2b.md)
* [Final Project Tasks](tasks-final.md)

## Changes During the Project

TODO - At final delivery, describe how this plan changed over the course of the project, or say that you followed it without changes and reflect on how it went.
