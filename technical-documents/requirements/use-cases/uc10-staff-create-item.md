# UC 10: Use Case Staff Create Item

**Actors:** Staff

**Preconditions:** User must be logged in as a Staff member. Backend and frontend are running.

**Main Flow**
1. The user chooses "Add Item" from the menu.
2. The user enters the item name, price, description, and initial quantity and clicks Submit. [Invalid Price] [Invalid Unit] [Duplicate]
   2a. The description is optional and may allow a photo upload.
3. The item is saved in the inventory.

**Alternative Flows**
- **[Invalid Price]** The price must be a non-negative float. If it is not, an error message is printed and the user is prompted to edit the form.
- **[Invalid Unit]** The quantity must be a non-negative integer. If it is not, an error message is printed and the user is prompted to edit the form.
- **[Duplicate]** If an item with the same name already exists, an error message is printed and the user is prompted to edit the form.

**Logging**
- "Staff user 'jdoe' created item 'Latte' with price $'3.50' and quantity '10'."
