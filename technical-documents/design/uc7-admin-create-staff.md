# Feature Design: UC7 Admin Create Staff

**Use Cases:** [UC7: Admin Create Staff](../requirements/use-cases/uc7-admin-create-staff.md)

**Owners:** sajose: frontend and backend. Branches: `sajose/i1a-design-uc7` (this document)

## Static Design

### Backend: Class Diagram

UC7 reuses the existing authentication service and controller, which already register customers. It adds a `CreateStaffDto` for the request, reuses the `UserDto` and `UserMapper` designed in [UC5](uc5-admin-edit-users.md) for the response, and extends the existing service and controller with a staff-creation operation.

```mermaid
classDiagram
direction LR

class User {
  <<existing entity, edited>>
  -Long id
  -String name
  -String username
  -String email
  -String password
  -Collection~Role~ roles
}

class Role {
  <<existing entity>>
  -Long id
  -String name
}

class Roles {
  <<existing config>>
  +String ROLE_ADMIN$
}

class UserRoles {
  <<enumeration>>
  ROLE_STAFF
  ROLE_CUSTOMER
}

class CreateStaffDto {
  <<new DTO>>
  -String name
  -String username
  -String password
}

class UserDto {
  <<UC5 DTO, reused>>
  -Long id
  -String name
  -String username
  -String email
  -String phone
  -String role
}

class UserMapper {
  <<UC5 mapper, reused>>
  +mapToUserDto(User user)$ UserDto
}

class UserRepository {
  <<existing repository>>
  +existsByUsername(String username) Boolean
  +save(User user) User
}

class RoleRepository {
  <<existing repository>>
  +findByName(String name) Role
}

class AuthService {
  <<existing interface, extended>>
  +createStaff(CreateStaffDto createStaffDto) UserDto
}

class AuthServiceImpl {
  <<existing implementation, extended>>
  -UserRepository userRepository
  -RoleRepository roleRepository
  -PasswordEncoder passwordEncoder
  +createStaff(CreateStaffDto createStaffDto) UserDto
}

class AuthController {
  <<existing controller, extended>>
  -AuthService authService
  -AuditLogger auditLogger
  +createStaff(CreateStaffDto createStaffDto) ResponseEntity~UserDto~
}

class AuditLogger {
  <<existing>>
  +log(String action) void
}

class WolfCafeAPIException {
  <<existing exception>>
  -HttpStatus status
  -String message
}

class GlobalExceptionHandler {
  <<existing>>
}

User "*" -- "*" Role : assigned roles
Roles *-- UserRoles
UserRepository ..> User : persists
RoleRepository ..> Role : finds
AuthController --> AuthService
AuthController --> AuditLogger
AuthController ..> CreateStaffDto : @Valid request body
AuthServiceImpl ..|> AuthService
AuthServiceImpl --> UserRepository
AuthServiceImpl --> RoleRepository
AuthServiceImpl ..> UserRoles : assigns ROLE_STAFF
AuthServiceImpl ..> UserMapper
UserMapper ..> User
UserMapper ..> UserDto
AuthServiceImpl ..> WolfCafeAPIException : throws for duplicate username
GlobalExceptionHandler ..> WolfCafeAPIException : maps to its status
GlobalExceptionHandler ..> CreateStaffDto : failed validation maps to 400
```

Existing classes to edit: `User` (`email` becomes nullable, since UC7 does not collect one; it stays unique), `AuthService` / `AuthServiceImpl` (add `createStaff`), and `AuthController` (add the endpoint and its audit message). New class: `CreateStaffDto`, with bean validation annotations for the username and password rules. `UserRepository`, `RoleRepository`, `Roles`, `AuditLogger`, and `GlobalExceptionHandler` are used unchanged.

`CreateStaffDto` validation:

| Field      | Annotations                                                   | Rule from UC7                                                     |
|------------|---------------------------------------------------------------|-------------------------------------------------------------------|
| `name`     | `@NotBlank`                                                   | name is required                                                  |
| `username` | `@NotBlank`, `@Size(min = 4)`                                 | [Invalid Username]: more than 3 characters                        |
| `password` | `@NotBlank`, `@Size(min = 8)`, `@Pattern(regexp = ".*\\d.*")` | [Invalid Password]: at least 8 characters and at least one number |

### Frontend: Wireframes

The Create Staff page is a form with Name, Username, and Password inputs, a Submit button, and a Cancel button that returns to the Admin Menu. Validation errors appear under the field they belong to; a duplicate username error from the server appears under the Username field.

![Updated Create Staff wireframe](images/WolfCafe%20—%20Create%20Staff.png)
![Create Staff wireframe](images/uc7-admin-create-staff.png)

## Dynamic Design

### Backend: Sequence Diagram

The sequence starts when the admin submits the form. The endpoint is restricted to Admin users.

```mermaid
sequenceDiagram
actor Admin
participant CreateStaffComponent
participant AuthController
participant AuthServiceImpl
participant UserRepository
participant RoleRepository
participant UserMapper
participant AuditLogger

Admin->>CreateStaffComponent: Enter name, username, password and press Submit
CreateStaffComponent->>CreateStaffComponent: validate username and password
alt invalid username or password
    CreateStaffComponent-->>Admin: Show error, stay on form
else valid on client
    CreateStaffComponent->>AuthController: POST /api/auth/staff (CreateStaffDto)
    alt caller is not an Admin
        AuthController-->>CreateStaffComponent: 403 Forbidden
    else @Valid fails (username too short, password too short or no number)
        AuthController-->>CreateStaffComponent: 400 Bad Request with field message
        CreateStaffComponent-->>Admin: Show error, stay on form
    else request is valid
        AuthController->>AuthServiceImpl: createStaff(createStaffDto)
        AuthServiceImpl->>UserRepository: existsByUsername(username)
        alt username already exists
            UserRepository-->>AuthServiceImpl: true
            AuthServiceImpl-->>AuthController: throw WolfCafeAPIException(BAD_REQUEST)
            AuthController-->>CreateStaffComponent: 400 Bad Request "Username already exists."
            CreateStaffComponent-->>Admin: Show duplicate error, stay on form
        else username is available
            UserRepository-->>AuthServiceImpl: false
            AuthServiceImpl->>RoleRepository: findByName("ROLE_STAFF")
            RoleRepository-->>AuthServiceImpl: staff Role
            AuthServiceImpl->>AuthServiceImpl: build User (encoded password, staff role)
            AuthServiceImpl->>UserRepository: save(user)
            UserRepository-->>AuthServiceImpl: saved user with id
            AuthServiceImpl->>UserMapper: mapToUserDto(user)
            UserMapper-->>AuthServiceImpl: UserDto
            AuthServiceImpl-->>AuthController: UserDto
            AuthController->>AuditLogger: log("created new staff, Name: ..., Username: ..., Id: ...")
            AuthController-->>CreateStaffComponent: 201 Created with UserDto
            CreateStaffComponent-->>Admin: Navigate to Admin Menu
        end
    end
end
```

### Frontend: Navigation Map

Login → Admin Menu → Create Staff form → Submit → Admin Menu

If any field is invalid or the username is taken, the admin stays on the Create Staff form with the error shown. Cancel returns to the Admin Menu without creating a user.

## Design Decisions

- **Reuse existing user management.** `AuthController.register()` and `AuthServiceImpl.register()` already create customers, so UC7 adds a parallel `createStaff` operation in the same classes rather than a new controller. The only difference from registration is the role (`ROLE_STAFF`), the Admin-only access, and the fields collected.
- **Separate request DTO.** `RegisterDto` requires an email and is used by the public registration page, so UC7 adds `CreateStaffDto` (name, username, password) rather than loosening `RegisterDto`'s meaning. Its bean validation annotations enforce the UC7 rules, and the existing `GlobalExceptionHandler` turns failures into 400 responses with the field's message.
- **Nullable email.** `User.email` is currently `nullable = false`, but UC7 does not collect one. The column becomes nullable (still unique; multiple `NULL`s are allowed). Customer registration still requires and checks email, and login by username is unaffected.
- **Reuse UC5's `UserDto`.** The response uses the `UserDto` and `UserMapper` from UC5 so the frontend gets the new staff member's id without the password. UC7 depends on UC5 landing these classes first; coordinate with the UC5 owner.
- **Duplicate username returns 400.** This matches the existing `register` endpoint and UC5. The check is done in the service with `existsByUsername`, which covers every user, not only staff, because the `username` column is unique across all users.
- **Validation in both layers.** `CreateStaffComponent` checks the same rules before submitting so the admin gets immediate feedback; the backend validation is the authority.
- **Audit logging without the password.** `AuditLogger` must never record passwords or other secrets, so UC7's logging section was updated to drop the password from its example. The log entry records the admin (supplied by `AuditLogger.log(String)`), their role, and `created new staff, Name: 'Jane Doe', Username: 'janedoe', Id: 8`.
- **Frontend.** Add `CreateStaffComponent` at route `/create-staff` inside the authenticated routes, linked from the UC4 Admin Menu, and `createStaffAPICall(staffObj)` in `AuthService.js`.

## REST API

The endpoint requires a valid login and the Admin role. A missing/invalid token returns 401; an authenticated non-admin receives 403. Recorded in the team's [REST API](rest-api.md).

| Method | Path | Roles | Request Body | Responses |
|--------|------|-------|--------------|-----------|
| POST | `/api/auth/staff` | Admin | `CreateStaffDto`: name, username, password | 201 Created with the new staff member's `UserDto`; 400 if the name is blank, the username is 3 characters or fewer, the password is shorter than 8 characters or has no number, or the username already exists |

Example request:

```json
{
  "name": "Jane Doe",
  "username": "janedoe",
  "password": "JaneDoe8"
}
```

Example response (201 Created):

```json
{
  "id": 8,
  "name": "Jane Doe",
  "username": "janedoe",
  "email": null,
  "phone": null,
  "role": "ROLE_STAFF"
}
```

Example error (400 Bad Request):

```json
{
  "timeStamp": "2026-10-28T14:03:11",
  "message": "Username already exists.",
  "details": "uri=/api/auth/staff"
}
```

## Changes During Implementation

Implementation follows this design: staff creation uses a separate validated request DTO, assigns `ROLE_STAFF`, encodes the password, and returns the password-free `UserDto` shared with UC5.
