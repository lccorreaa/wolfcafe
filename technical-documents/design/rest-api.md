# WolfCafe REST API

The REST API is designed as a team, with the frontend feature-based roles leading the discussion.  Record every endpoint you add, change, or remove here, with the roles allowed to call it.

Every endpoint outside `/api/auth` requires a login.  A request without a valid login token is answered with 401 Unauthorized, and a user without one of the listed roles with 403 Forbidden.  Error responses from `GlobalExceptionHandler` are JSON with `timeStamp`, `message`, and `details` fields.

## Base Endpoints

These endpoints come with the base WolfCafe project.

### Authentication - `AuthController`

| Method | Path                  | Roles  | Request Body                                   | Responses                                                                                     |
|--------|-----------------------|--------|------------------------------------------------|-----------------------------------------------------------------------------------------------|
| POST   | `/api/auth/register`  | anyone | `RegisterDto`: name, username, email, password | 201 Created; 400 if required fields/email are invalid or username/email already exists      |
| POST   | `/api/auth/login`     | anyone | `LoginDto`: usernameOrEmail, password          | 200 OK with `JwtAuthResponse`: accessToken, tokenType, role; 401 if the credentials are wrong |
| GET    | `/api/auth/users`    | Admin  |                                                | 200 OK with staff and customer `UserDto` list                                                 |
| PUT    | `/api/auth/user/{id}`| Admin  | `UserDto`: name, email, phone                  | 200 OK with updated `UserDto`; 400 if fields are invalid or email is in use; 404 if missing or not staff/customer |
| DELETE | `/api/auth/user/{id}`| Admin  |                                                | 200 OK; 404 if no staff/customer account has the id                                           |

### Items - `ItemController`

| Method | Path              | Roles                  | Request Body                        | Responses                                                                                                                    |
|--------|-------------------|------------------------|-------------------------------------|------------------------------------------------------------------------------------------------------------------------------|
| GET    | `/api/items`      | Admin, Staff, Customer |                                     | 200 OK with a list of `ItemDto`                                                                                              |
| GET    | `/api/items/{id}` | Admin, Staff, Customer |                                     | 200 OK with the `ItemDto`; 404 if no item has the id                                                                         |
| POST   | `/api/items`      | Admin, Staff           | `ItemDto`: name, description, price | 201 Created with the saved `ItemDto`; 400 if the name is blank or the price is negative; 409 if an item already has the name |
| PUT    | `/api/items/{id}` | Admin                  | `ItemDto`: name, description, price | 200 OK with the updated `ItemDto`; 400 as for POST; 404 if no item has the id; 409 if another item has the name              |
| DELETE | `/api/items/{id}` | Admin                  |                                     | 200 OK; 404 if no item has the id                                                                                            |

### Orders - `OrderController`

| Method | Path               | Roles                  | Request Body                       | Responses                                                                                       |
|--------|--------------------|------------------------|------------------------------------|-------------------------------------------------------------------------------------------------|
| GET    | `/api/orders`      | Admin, Staff           |                                    | 200 OK with a list of `OrderDto`                                                                |
| GET    | `/api/orders/{id}` | Admin, Staff, Customer |                                    | 200 OK with the `OrderDto`; 404 if no order has the given id                                    |
| POST   | `/api/orders`      | Admin, Customer        | `OrderDto`: Items, Amounts, Status | 200 OK and the body shows the saved `OrderDto`; 400 if amounts are negative or lists are empty; |
| PUT    | `/api/orders/{id}` | Admin, Customer        | `OrderDto`: Items, Amounts, Status | 200 OK with the updated `OrderDto`; 404 if no item has the id                                   |
| DELETE | `/api/orders/{id}` | Admin                  |                                    | 200 OK; 404 if no order exists with the given id                                                |

### Fulfill Order - `OrderController`

| Method | Path                       | Roles         | Request Body | Responses                                                     |
|--------|----------------------------|---------------|--------------|---------------------------------------------------------------|
| PUT    | `/api/orders/{id}/fulfill` | Admin, Staff  | n/a          | 200 OK, 404 if order doesn't exist, 409 for validation errors |


## New and Changed Endpoints

The authentication endpoints below are mainly for Admin.

| Method | Path              | Roles                  | Request Body                               | Responses                                                                                                                                                                                                      | Use Case |
|--------|-------------------|------------------------|--------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|----------|
| PUT    | `api/inventory`   | Admin, Staff           | `ItemDto`: [ {name, amount}, ...]          | 200 OK with the updated `Inventory`; 400 if any amounts are negative                                                                                                                                           | UC11     |
| GET    | `api/inventory`   | Admin, Staff           |                                            | 200 OK with the current `Inventory`                                                                                                                                                                            | UC11     |
| GET    | `/api/tax`        | Admin, Staff, Customer |                                            | 200 OK with `TaxRateDto`: id, rate (created with default 0.0 if none exists)                                                                                                                                   | UC6      |
| PUT    | `/api/tax`        | Admin                  | `TaxRateDto`: rate                         | 200 OK with the updated `TaxRateDto`; 400 if the rate is missing or not a positive number                                                                                                                      | UC6      |
| POST   | `/api/auth/staff` | Admin                  | `CreateStaffDto`: name, username, password | 201 Created with the new staff member's `UserDto`; 400 if the name is blank, the username is 3 characters or fewer, the password is shorter than 8 characters or has no number, or the username already exists | UC7      |
| GET    | `/api/auth/users` | Admin                  |                                            | 200 OK with staff and customer `UserDto` list; DTOs omit passwords                                                                                                                                             | UC5      |
| PUT    | `/api/auth/user/{id}` | Admin               | `UserDto`: name, email, phone              | 200 OK with updated `UserDto`; 400 if fields are invalid or email is in use; 404 if missing or not staff/customer                                                                                               | UC5      |
| DELETE | `/api/auth/user/{id}` | Admin               |                                            | 200 OK; 404 if no staff/customer account has the id                                                                                                                                                            | UC5      |
