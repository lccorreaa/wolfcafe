# WolfCafe Design

This folder holds the design of WolfCafe: the architecture of the base system you were given, the REST API, and a design document for each feature your team adds.

* [Security and Logging](security.md) - user roles, login tokens, protecting endpoints, and the audit log
* [REST API](rest-api.md) - every endpoint, who may call it, and what it returns
* Feature designs - one per feature-based role, started from the [feature design template](feature-design-template.md)

## Feature Designs

TODO - Link each feature design here as you create it.

| Feature | Use Cases | Owners | Iteration |
| ------- | --------- | ------ | --------- |
| | | | |

## Architecture

Software architecture is the structure or structures of a system: the software components, their externally visible properties, and the relationships among them.  WolfCafe, like CoffeeMaker, follows a **layered architecture**.  The backend is a Spring Boot REST API in `wolf-cafe-backend`; the frontend is a React application in `wolf-cafe-frontend` that calls it.

### Backend Layers

The standard Spring architecture consists of three layers, which map to the following WolfCafe packages under `edu.ncsu.csc326.wolfcafe`:

- **Repository Layer** - interfaces to the data store and the entities they store.
  - `entity` - JPA entities, one per database table: `Item`, `User`, `Role`
  - `repositories` - Spring Data JPA repositories for each entity
- **Service Layer** - business logic.
  - `services` - service interfaces, e.g. `ItemService`
  - `services.impl` - their implementations, e.g. `ItemServiceImpl`
- **Web Layer** - the REST API.
  - `controllers` - REST controllers, e.g. `ItemController`, `AuthController`
  - `exception` - exceptions and `GlobalExceptionHandler`, which turns them into error responses

Data moves between layers as Data Transfer Objects (`dto`), and is persisted as entities.  `mapper` classes convert between the two as data crosses the service layer (e.g., `ItemMapper.mapToItem()` / `ItemMapper.mapToItemDto()`).  The package names match CoffeeMaker's, so CoffeeMaker classes you bring over drop into the same packages.

Two packages cut across the layers:

- `security` - checks the login token on every request; see [Security and Logging](security.md)
- `config` - security rules, user roles, the start-up data loader, and the `AuditLogger`

Entities and DTOs use [Lombok](https://projectlombok.org/) annotations (`@Getter`, `@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor`) to generate their getters, setters, and constructors at compile time.  CoffeeMaker classes with handwritten getters and setters work alongside them unchanged.

### Frontend

- `src/components` - one React component per page or page part, e.g. `ListItemsComponent`, `ItemComponent`
- `src/services` - one module per backend controller that makes the REST calls with axios, e.g. `ItemService.js`.  `ApiConfig.js` holds the backend URL, and `AuthService.js` stores the login token and adds it to every request.
- `src/App.jsx` - the routes, i.e. which component is shown at which URL, and which routes require a login

### Following a Request Through the System

Adding an item from the Add Item page:

1. `ItemComponent` calls `saveItem()` in `ItemService.js`, which sends `POST /api/items` with the item as JSON and the login token in the `Authorization` header.
2. `JwtAuthenticationFilter` checks the token and records who the user is and what role they have.
3. Spring matches the request to `ItemController.addItem()`.  `@PreAuthorize` checks the user's role, and `@Valid` checks the `ItemDto` against its validation annotations.
4. `ItemServiceImpl.addItem()` checks for a duplicate name, converts the `ItemDto` to an `Item` with `ItemMapper`, and saves it through `ItemRepository`.
5. The controller records the action with the `AuditLogger` and returns the saved item as JSON with status 201 Created.

An exception thrown along the way becomes an error response: `ResourceNotFoundException` is a 404, and `GlobalExceptionHandler` answers a `WolfCafeAPIException` with the status it carries and a failed validation with a 400.

## References

- [Understanding Spring Web Application Architecture](https://www.petrikainulainen.net/software-development/design/understanding-spring-web-application-architecture-the-classic-way/)
