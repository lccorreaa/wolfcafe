# UC N: Use Case Title

Copy this file to `ucN-short-name.md` (for example, `uc1-create-item.md`) for each use case, fill it in, and link it from the [Requirements](../index.md) page.  Delete these instructions from your copy.

A use case describes the system from the user's perspective: what has to be true before it starts, the main flow when everything goes right, and the alternative flows for errors and other paths.  Tag each alternative flow by name in the step where it can happen, as in the CoffeeMaker requirements.

**Actors:** Which user roles (Admin, Staff, Customer, or a role you add) take part.

**Preconditions:** What must be true before this use case starts, e.g. the user is logged in as Staff [UCx].

**Main Flow**
1. The user ...
2. The user enters ... and clicks Submit.  The ... is saved.  [Invalid Input] [Duplicate]
3. ...

**Alternative Flows**
- **[Invalid Input]** ...
- **[Duplicate]** ...

**Logging**
- What is logged for this use case: the user, their role, and the action (e.g. "Staff user `jdoe` created item 7").
