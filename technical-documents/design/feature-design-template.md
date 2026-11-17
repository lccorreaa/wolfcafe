# Feature Design: Feature Name

Copy this file to `feature-short-name.md` for each feature-based role's design, fill it in, and link it from the [Design](index.md) page.  Write the design on its own document branch, such as `docs-i1a-design-orders`; corrections found later while implementing go on the feature branch with the code.  See [Technical Document Branches](../github-process.md#technical-document-branches).  Delete these instructions from your copy.

Save diagram and wireframe images in an `images` folder next to this file and embed them with Markdown, for example `![Item class diagram](images/item-class-diagram.png)`.

**Use Cases:** links to the use cases in the [requirements](../requirements/index.md) this feature implements

**Owners:** team members, and the branches each owns

## Static Design

### Backend: Class Diagram

TODO - A UML class diagram of the backend classes this feature adds, edits, or removes: their state, behaviors, and relationships (inheritance and composition).  If you plan to touch a class, it is in the diagram.  Note anything you remove under its class name and remove its associations.

### Frontend: Wireframes

TODO - Wireframes of each page this feature adds or changes, showing the UI elements you plan to add, edit, or remove.

## Dynamic Design

### Backend: Sequence Diagram

TODO - A sequence diagram for each use case, starting from the REST API call.

### Frontend: Navigation Map

TODO - Connect the wireframes into a flow showing how the user moves between pages.

## Design Decisions

TODO - A high level overview of the changes at every layer of the system, referencing the class diagram.  Explain which classes will be touched and why, what will be reused and what will stay the same, and the limitations and constraints of the design.  Write it so a project manager can evaluate the design and another programmer could implement it without asking you.

## REST API

TODO - The endpoints this feature adds or changes.  Record them in the team's [REST API](rest-api.md) too.

## Changes During Implementation

TODO - At final delivery, describe how the design changed during implementation, with updated diagrams, or say that you followed it as designed.
