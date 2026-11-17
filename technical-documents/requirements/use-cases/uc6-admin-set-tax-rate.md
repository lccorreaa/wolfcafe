# UC 6: Use Case Admin Set Tax Rate

**Actors:** Admin

**Preconditions:**  User must be logged in as an Administrator. Backend and frontend is running.

**Main Flow**
1. The user goes to the WolfCafe website and selects "Set Tax Rate" from the menu.
2. The user is shown the current tax rate in an editable text box.
3. User presses submit button. [Invalid Unit]
4. Tax rate is set to number in text box.

**Alternative Flows**
- **[Invalid Unit]** The tax rate must be a positive float. If it is not a positive float, an error message is printed and the user is prompted to edit the form.

**Logging**
- "Admin user 'jdoe' set the tax rate to '8.0'%"
