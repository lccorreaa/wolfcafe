# UC 12: Customer Create Account

**Actors:** Unregistered Customer

**Preconditions:** The customer is not logged in. The frontend and backend are running.

**Main Flow**
1. The customer selects "Create Account" from the login page.
2. The form shows fields for name, username, email, password, and password confirmation.
3. The customer enters the required information and selects the "Submit" button.
4. The fields are validated. [Invalid Input] [Duplicate]
5. The system creates an account with the Customer role and securely hashes the password.
6. The system displays a success message and directs the customer to log in.

**Alternative Flows**
- **[Invalid Input]**: The system identifies invalid or missing fields and allows the customer to edit them.
- **[Duplicate]**: If the username or email already exists, the system displays an error and does not create the account.

**Logging**
- Record successful account creation using the username and account ID
- Passwords are never logged
