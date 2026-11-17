# UC 11: Use Case Staff Update Inventory

**Actors:** Staff

**Preconditions:** User must be logged in as a Staff member. Backend and frontend are running. Items must exist in inventory.

**Main Flow**
1. The user chooses "Inventory" from the menu.
2. A list of existing items is shown with the following information:
    - Item Name
    - Price
    - Description (including a photo if available)
    - Quantity in an editable field
    - A button to delete the item [Invalid Input]
3. The user edits the quantity of an item and clicks Submit. [Invalid Units]
4. The quantity of the item is updated in the inventory.

**Alternative Flows**
- **[Invalid Units]** The quantity must be a non-negative integer. If it is not, an error message is printed and the user is prompted to edit the form.
- **[Invalid Input]** If the user tries to delete an already deleted item, an error message is printed and the user is prompted to refresh the inventory list.

**Logging**
- "Staff user 'jdoe' updated item 'Latte' to quantity '15'."
