# Security and Logging

WolfCafe users log in, and what they may do depends on their role.  This page explains how the base system does that, and how to extend it.

## User Roles

User roles are defined in `config.Roles`.  The `ADMIN` role is a constant, `Roles.ROLE_ADMIN`.  All other roles are listed in the `Roles.UserRoles` enumeration, which starts with `ROLE_STAFF` and `ROLE_CUSTOMER`.  Add a role by adding its name, starting with `ROLE_`, to the enumeration.

A user who registers through `POST /api/auth/register` is given the `CUSTOMER` role.

### The Roles and Admin User in the Database

`config.SetupDataLoader` runs every time the application starts.  It creates any role that is missing from the database, and creates the default admin user if it does not exist yet:

| Field | Value |
| ----- | ----- |
| Username | `admin` |
| Email | `admin@admin.edu` |
| Password | `app.admin-user-password` in `application.properties`: `admin`, unless the `ADMIN_PASSWORD` setting is given in `wolf-cafe-backend/.env` |

The password is only read when the admin user is created, so changing it later has no effect on an existing admin.  Set `ADMIN_PASSWORD` before the first start-up, or drop the `wolfcafe` database to start over.

## Logging In

1. The frontend sends the username or email and password to `POST /api/auth/login`.
2. `AuthServiceImpl.login()` checks them with Spring Security.  Wrong credentials are answered with 401 Unauthorized.
3. `JwtTokenProvider` creates a JSON Web Token (JWT) that names the user, signed with `app.jwt-secret`.  It expires after `app.jwt-expiration-milliseconds` (7 days).
4. The response holds the token and the user's role.  `LoginComponent` saves `Bearer <token>` in the browser's local storage and the username and role in session storage.
5. From then on, `AuthService.js` adds the token to every request in the `Authorization` header.
6. On each request, `JwtAuthenticationFilter` checks the token's signature and expiry, loads the user and their roles, and records them for the rest of the request.  A request with a missing, expired, or forged token stays anonymous.

The signing secret has a development-only default in `application.properties`.  Anyone who knows a secret can forge a token for any user, so a deployed WolfCafe must set its own `JWT_SECRET` (see `wolf-cafe-backend/.env.template`).

## Protecting an Endpoint

`config.SpringSecurityConfig` makes `/api/auth/**` public and requires a login for everything else.  Each controller method then states which roles may call it with `@PreAuthorize`:

```java
@PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
@PostMapping
public ResponseEntity<ItemDto> addItem(@Valid @RequestBody ItemDto itemDto) {
```

`hasRole` and `hasAnyRole` add the `ROLE_` prefix themselves, so write `'STAFF'`, not `'ROLE_STAFF'`.  A request with no valid login is answered with **401 Unauthorized**; a logged in user without one of the roles is answered with **403 Forbidden**.  Give every new endpoint a `@PreAuthorize`, and record the roles in the [REST API](rest-api.md).

### In the Frontend

`AuthService.js` provides `hasAnyRole('ROLE_STAFF', 'ROLE_ADMIN')` and `isAdminUser()` for deciding which pages and buttons to show.  Hiding a button only helps the user; it does not protect anything.  Every request is still checked by the backend, so the backend's `@PreAuthorize` is the real control.  Keep the two in step.

Routes nested under `<AuthenticatedRoute>` in `App.jsx` are shown only to a logged in user.

### Testing Secured Endpoints

- `@WithMockUser(username = "staff", roles = "STAFF")` on a test runs it as a logged in user with that role, without a real account or token.  See `ItemControllerTest` and `ItemControllerWebMvcTest`.
- A test without `@WithMockUser` runs as a user who has not logged in.
- `AuthControllerTest.testLoginTokenAuthorizesRequests()` logs in for real and sends the token, as the frontend does.

Test each endpoint with every role that may call it and with one that may not.

## Audit Logging

The requirements call for each major create, read, update, and delete operation to log the user, their role, and the action.  Controllers do this with `config.AuditLogger`, after the action has succeeded:

```java
ItemDto savedItem = itemService.addItem(itemDto);
auditLogger.log("created item " + savedItem.getId() + " (" + savedItem.getName() + ")");
```

`log(action)` reads the logged in user and their roles itself.  Where there is no logged in user yet, as on registration and login, `log(user, role, action)` names them explicitly.  Entries go to the console through the logger named `wolfcafe.audit`:

```
... INFO ... wolfcafe.audit : user=staff role=ROLE_STAFF action=created item 7 (Coffee)
```

To keep the log in a file as well, add `logging.file.name=logs/wolfcafe.log` to `application.properties`.  If your requirements call for users to view the log in WolfCafe itself, that is a design decision for your team, e.g. storing entries in a database table.

Never log a password, a token, or other secret.

## Trying the API in Postman

### Register a Customer

Endpoint: `POST http://localhost:8080/api/auth/register`

Body:

```json
{
    "name": "Jordan Estes",
    "username": "jestes",
    "email": "jestes@example.com",
    "password": "a-good-password"
}
```

Response: 201 Created

```
User registered successfully.
```

### Log In

Endpoint: `POST http://localhost:8080/api/auth/login`

Body:

```json
{
    "usernameOrEmail": "jestes",
    "password": "a-good-password"
}
```

Response: 200 OK

```json
{
    "accessToken": "eyJhbGciOiJIUzM4NCJ9...",
    "tokenType": "Bearer",
    "role": "ROLE_CUSTOMER"
}
```

The token differs on every login.  Copy it for the requests below.

### Call a Secured Endpoint

Endpoint: `GET http://localhost:8080/api/items`

Authorization: in Postman's **Authorization** tab, choose **Bearer Token** and paste the `accessToken`.

Response: 200 OK, with a JSON list of items.
