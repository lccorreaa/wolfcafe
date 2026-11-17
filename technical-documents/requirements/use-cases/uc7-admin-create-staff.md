# UC 7: Use Case Admin Create Staff

**Actors:** Admin

**Preconditions:**  User must be logged in as an Administrator. Backend and frontend is running.

**Main Flow**
1. The user goes to the WolfCafe website and selects "Create Staff" from the menu.
2. User enters name, username, and password of the new staff. [Invalid Username] [Invalid Password] [Duplicate]
3. User presses "Submit" and is returned to the main menu.

**Alternative Flows**
- **[Invalid Username]** The username must be more than 3 characters. If it is not, an error message is printed, and the user is prompted to edit the form.
- **[Invalid Password]** The password must contain at least 8 characters, and at least one number. If it does not, an error message is printed, and the user is prompted to edit the form.
- **[Duplicate]** Each staff must have a unique username. If any user (admin, staff, or customer) already exists with the entered username, an error message is printed and the user is prompted to edit the form.

**Logging**
- What is logged for this use case: the user, their role, and the action (e.g. "Staff user `jdoe` created item 7")
- "Admin user 'jdoe' created new staff, Name: 'Jane Doe', Username: 'janedoe', Id: 8."
- The new staff member's password is never logged.
