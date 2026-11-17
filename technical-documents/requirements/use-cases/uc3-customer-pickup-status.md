# UC 3: Use Case Customer Pickup Status

**Actors:** Customer

**Preconditions:** User is logged in as a Customer or proceeding as a guest. Backend and frontend is running. Order has been placed and is in the system.

**Main Flow**
1. The user goes to the WolfCafe website.
2. The user selects the "Pickup Status" button from the menu.
3. The system displays the status of the user's order, including whether it is ready for pickup or still being prepared.
4. After 15 minutes of the order being ready for pickup, the order is deleted from the system

**Logging**
- "Customer user 'jdoe' viewed pickup status for order 1, which is ready for pickup."
