# UC 2: Use Case User Order

**Actors:** Customer

**Preconditions:** User is logged in as a Customer or proceeding as a guest. Backend and frontend is running.

**Main Flow**
1. The user goes to the WolfCafe website.
2. The user selects the "Order" menu option.
3. The user selects items and amounts from the menu and adds them to their order. [Invalid Unit] [Remove Item]
4. The user reviews the order total including sales tax and selects a tip option (15%, 20%, 25%, or custom). [Invalid Price]
5. The user selects the "Submit Order" button to submit the order. The order is saved and a confirmation message is displayed.
6. User returns to the main menu.

**Subflows**
- **[Remove Item]** The user can remove an item from their order by selecting the "Remove" button next to the item in the order.

**Alternative Flows**
- **[Invalid Unit]** The amount of an item must be a positive integer. If the unit on any of the items is invalid, an error message is printed and the user is prompted to re-enter the amount.
- **[Invalid Price]** The price of the tip, if custom, must be a positive integer or zero. If the price is not positive or zero, an error message is printed and the user is prompted to re-enter the amount.

**Logging**
- "Customer user 'jdoe' created order 1 with items [item 1, item 2, item 3] and tip $5.00. Id: 8."

