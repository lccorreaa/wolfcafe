# UC 9: Use Case Staff Fulfill Order

**Actors:** Staff

**Preconditions:** User must be logged in as a Staff member. Backend and frontend are running. Orders must be waiting for fulfillment.

**Main Flow**
1. The user chooses "Orders" from the menu.
2. A list of orders is shown with the following information:
    - Order ID
    - Customer Name
    - A button to fulfill the order
3. The user clicks the fulfill button for an order.
4. The order is marked as fulfilled and removed from the list of orders. [Insufficient Inventory]

**Alternative Flows**
- **[Insufficient Inventory]** If there is not enough inventory to fulfill the order, the system displays an error message and does not mark the order as fulfilled. No inventory is used.

**Logging**
- "Staff user 'jdoe' fulfilled order for customer 'Jane Doe' with items: '2x Latte, 1x Croissant' and id: '1'."
