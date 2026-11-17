# Feature Design: UC4 Admin Menu


**Use Cases:** [UC4: Admin View Menu](../requirements/use-cases/uc4-admin-view-menu.md)

**Owners:** Shion Strickland (shstric2): frontend and backend. Branches: `shstric2/uc4_frontend-entry` (code), `shstric2/i1A-design-uc4` (this document).

## Static Design

### Backend: Class Diagram
No classes are added, edited, or removed. UC4 reuses the existing authentication classes: AuthController, AuthServiceImpl, JwtTokenProvider, and JwtAuthResponse.

### Frontend: Wireframes

![Updated Login wireframe](images/WolfCafe%20—%20Login.png)
![Login wireframe](images/uc4-wireframe-1-login.png)

Login: the user enters a username and password and clicks Submit. An admin is then taken to the Admin Menu.

![Updated Admin menu wireframe](images/WolfCafe%20Admin%20Settings.png)
![Admin menu wireframe](images/uc4-wireframe-2-admin-menu.png)

Admin Menu: shows three options: Edit Users (UC5), Create Staff (UC7), and Set Tax Rate (UC6). This page is only available to admins.

![Updated Edit Users wireframe](images/WolfCafe%20Admin%20Settings.png)
![Edit Users wireframe](images/uc4-wireframe-3-edit-users.png)

Edit Users: the real page is built by UC5.

![Updated Create Staff wireframe](images/WolfCafe%20—%20Create%20Staff.png)
![Create Staff wireframe](images/uc4-wireframe-4-create-staff.png)

Create Staff: the real page is built by UC7.

![Updated Set Tax Rate wireframe](images/WolfCafe%20Admin%20Settings.png)
![Set Tax Rate wireframe](images/uc4-wireframe-5-set-tax-rate.png)

Set Tax Rate: the real page is built by UC6.

## Dynamic Design

### Backend: Sequence Diagram

```mermaid
sequenceDiagram
participant LoginComponent
participant AuthController
participant AuthServiceImpl
participant JwtTokenProvider
LoginComponent->>AuthController: POST /api/auth/login (username, password)
AuthController->>AuthServiceImpl: login(loginDto)
AuthServiceImpl->>AuthServiceImpl: authenticate username and password, look up user role
AuthServiceImpl->>JwtTokenProvider: generateToken(authentication)
JwtTokenProvider-->>AuthServiceImpl: JWT token
AuthServiceImpl-->>AuthController: JwtAuthResponse (token, role)
AuthController-->>LoginComponent: 200 OK with JwtAuthResponse
LoginComponent->>LoginComponent: store token and role, navigate to Admin Menu if role is admin
```

UC4 adds no new backend calls, since the role already arrives in the login response.

### Frontend: Navigation Map

Login → Admin Menu → Edit Users / Create Staff / Set Tax Rate

## Design Decisions

- **A single Admin Menu page.** Admin features (Edit Users, Create Staff, Set Tax Rate) are reached from one landing page after login, so an admin does not need to remember or type separate URLs.
- **Admin-only access.** The menu is shown only to users with the admin role, so staff and customers never see admin options.
- **Frontend only.** The menu is just a page of links to other use cases, so it needs no new backend classes or endpoints. Login and role information come from the existing authentication flow.
- **Placeholder pages.** The three destination pages are wireframed here, but each is built by its own use case (UC5, UC6, UC7). This keeps UC4 small and avoids overlapping work.


## REST API

UC4 adds no endpoints, because the menu is a frontend page.

## Changes During Implementation

TODO - At final delivery, describe how the design changed during implementation, with updated diagrams, or say that you followed it as designed.
