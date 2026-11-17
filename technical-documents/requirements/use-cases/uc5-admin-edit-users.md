# UC 5: Use Case Admin Edit Users

**Actors:** Admin

**Preconditions:** User must be logged in as an Administrator. Backend and frontend is running. Staff or Customers exist in user list.

**Main Flow**
1. The user goes to the WolfCafe website and selects "Edit Users" from the menu.
2. A list of users is displayed, separating customers and staff. An edit and delete button are displayed next to each user. [Delete User]
3. When edit is selected, the user is taken to a form where changes can be made to the user's information.
4. The user presses "Submit" to save the changes. A success message is shown. [Cannot Edit] [Invalid Input]
5. Changes are saved and the user is returned to the edit users page.

**Subflow**
- **[Delete User]** The user can delete a user by selecting the "Delete" button next to the user in the list.

**Alternative Flows**
- **[Cannot Edit]** If the user has been deleted before the changes are saved and error message is printed and the user is returned to the edit users page.
- **[Invalid Input]** All boxes must be filled with valid input. If any boxes are empty or do not contain valid input, an error message is printed and the user is prompted to edit the form.

**Logging**
- "Admin user 'jdoe' edited customer/staff user 'jdoe2'. User information now reads Name: 'Jane Doe', Email: 'jdoe@gmail.com', Phone: '828-888-8282'"
