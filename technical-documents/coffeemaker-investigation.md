# CoffeeMaker Investigation

Your team inspects each member's CoffeeMaker project during Iteration 0 to decide which one to bring into WolfCafe, and to find the parts of CoffeeMaker your Team Project work will build on.  Record what you investigated and what you found.  Replace every TODO, including this one.

The audience is your manager: show how you investigated the system and which parts of it will be useful for your implementation.  Under the Team Project AI rules, AI may help you explore the code, but the findings and analysis here are written by your team.

## Projects Inspected

One row per team member's CoffeeMaker project.

| Team Member  | Repository | Strengths                                                       | Weaknesses                                                                                                                                                        |
|--------------|------------|-----------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Sandra Jose  | 5          | The frontend is cohesive and consistent between the use cases.  | No @PreAuthorize or role checks on any endpoint (CoffeeMaker had no users)<br/> System tests cover only UC2, UC4, UC5, and UC7. UC1, UC3, and UC6 don't have any. |

## Selected Project

Which CoffeeMaker project your team is bringing into WolfCafe, and why.  Note any changes you plan to make as you bring it over.

We're bringing Sandra's CoffeeMaker into WolfCafe because her implementation is functional and the frontend is consistent.

## Launching CoffeeMaker

Any challenges you ran into starting the selected CoffeeMaker backend and frontend.

Reviewing the frontend after updating the coffee maker implementation was difficult because we would have to log in.

When running the backend, I (sajose) ran into an issue with a duplicate Ingredient class, but I couldn't see the duplicate class.

I used AI to link my components to the wolf cafe frontend.

## System Tests

Run the selected project's system tests.  Note any that failed, any challenges running them, and anything in the tests or acceptance scenarios that will be useful as you write WolfCafe's requirements.

| Test ID                                | Description                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              | Expected Results                                                                                                                                                                                                                                                                                                                                                                                                                              | Actual Results |
|----------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|----------------|
| `addRecipeValid1`                      | **Preconditions:** CoffeeMaker is running and no recipe named "Coffee" exists.<br><br>1. Browse to [http://localhost:3000](http://localhost:3000).<br>2. Click **Add a Recipe**.<br>3. Enter name `Coffee`, price `50`, coffee `3`, milk `1`, sugar `1`, chocolate `0`.<br>4. Click **Submit**.                                                                                                                                                                                                                                                                                                          | The recipe is created and a success message is displayed.  "Coffee" appears in the recipe list on the main menu.                                                                                                                                                                                                                                                                                                                              |                |
| `addRecipeNegativePrice1`              | **Preconditions:** CoffeeMaker is running and no recipe named "Mocha" exists.<br><br>1. Browse to [http://localhost:3000](http://localhost:3000).<br>2. Click **Add a Recipe**.<br>3. Enter name `Mocha`, price `-10`, coffee `3`, milk `1`, sugar `1`, chocolate `2`.<br>4. Click **Submit**.                                                                                                                                                                                                                                                                                                           | The recipe is **not** created.  An error message indicates the price must not be negative.  "Mocha" does not appear in the recipe list.                                                                                                                                                                                                                                                                                                       |                |
| `makeRecipeValid`                      | **Preconditions:** CoffeeMaker is running and recipe "Coffee" exists.<br><br>1. Browse to [http://localhost:3000](http://localhost:3000).<br>2. Click **Order Beverage**.<br>3. Select **Coffee** in the Beverage dropdown.<br>4. Enter `100` in Amount Paid.<br>5. Click **Order**.                                                                                                                                                                                                                                                                                                                     | The inventory should be decremented by the recipe amount.                                                                                                                                                                                                                                                                                                                                                                                     |                |
| `makeRecipeInsufficientPayment`        | **Preconditions:** CoffeeMaker is running and recipe "Coffee" exists.<br><br>1. Browse to [http://localhost:3000](http://localhost:3000).<br>2. Click **Order Beverage**.<br>3. Select **Coffee** in the Beverage dropdown.<br>4. Enter `10` in Amount Paid.<br>5. Click **Order**.                                                                                                                                                                                                                                                                                                                      | An error should be returned, no inventory should be changed, and the amount paid should be returned.                                                                                                                                                                                                                                                                                                                                          |                |
| `makeRecipeInsufficientIngredients`    | **Preconditions:** CoffeeMaker is running and recipe "Coffee" exists.<br><br>1. Browse to [http://localhost:3000](http://localhost:3000).<br>2. Click Inventory and make sure all the ingredients have 0 units. <br>3. Click **Order Beverage**.<br>4. Select **Coffee** in the Beverage dropdown.<br>5. Enter `100` in Amount Paid.<br>6. Click **Order**.                                                                                                                                                                                                                                              | An error should be returned, no inventory should be changed, and the amount paid should be returned.                                                                                                                                                                                                                                                                                                                                          |                |
| `orderBeverageValidPostman`            | **Preconditions:** CoffeeMaker backend is running. Inventory holds Coffee `10` and Milk `5`. Recipe "Latte" exists with price `50`, Coffee `3`, Milk `2`.<br><br>1. In Postman, send `POST http://localhost:8080/api/makerecipe/Latte` with header `Content-Type: application/json` and raw body `75`.<br>2. Send `GET http://localhost:8080/api/inventory`.                                                                                                                                                                                                                                             | Step 1 returns status `200 OK` with body `25` (the change).<br>Step 2 returns status `200 OK` and shows Coffee with amount `7` and Milk with amount `3`.                                                                                                                                                                                                                                                                                      |                |
| `orderBeverageInvalidPaymentPostman`   | **Preconditions:** CoffeeMaker backend is running. Inventory holds Coffee `10` and Milk `5`. Recipe "Latte" exists with price `50`, Coffee `3`, Milk `2`.<br><br>1. In Postman, send `POST http://localhost:8080/api/makerecipe/Latte` with header `Content-Type: application/json` and raw body `50.5`.<br>2. Send `GET http://localhost:8080/api/inventory`.<br>3. Repeat step 1 with raw body `"abc"`.<br>4. Send `GET http://localhost:8080/api/inventory`.                                                                                                                                          | Steps 1 and 3 each return status `400 Bad Request` with an empty body.<br>Steps 2 and 4 each return status `200 OK` and show Coffee with amount `10` and Milk with amount `5` (no inventory used).                                                                                                                                                                                                                                            |                |
| `orderBeverageValid`                   | **Preconditions:** CoffeeMaker is running. Inventory holds Coffee `10` and Milk `5`. Recipe "Latte" exists with price `50`, Coffee `3`, Milk `2`.<br><br>1. Browse to [http://localhost:3000](http://localhost:3000).<br>2. Click **Order Beverage**.<br>3. Select **Latte - 50** in the Beverage dropdown.<br>4. Enter `75` in Amount Paid.<br>5. Click **Order**.<br>6. Click **Inventory**.<br>7. Click **Order Beverage**, then click **Return to Main Menu**.                                                                                                                                       | Step 5 displays the success message "Your Latte is ready. Your change is 25." and the user stays on the Order Beverage page.<br>Step 6 shows Coffee `7` and Milk `3`.<br>Step 7 shows the List of Recipes page.                                                                                                                                                                                                                               |                |
| `orderBeverageInsufficientPayment`     | **Preconditions:** CoffeeMaker is running. Inventory holds Coffee `10` and Milk `5`. Recipe "Latte" exists with price `50`, Coffee `3`, Milk `2`.<br><br>1. Browse to [http://localhost:3000](http://localhost:3000).<br>2. Click **Order Beverage**.<br>3. Select **Latte - 50** in the Beverage dropdown.<br>4. Enter `40` in Amount Paid.<br>5. Click **Order**.<br>6. Click **Inventory**.                                                                                                                                                                                                           | Step 5 displays the error message "Insufficient payment: Latte costs 50. Your payment of 40 has been returned." and the user stays on the Order Beverage page.<br>Step 6 shows Coffee `10` and Milk `5` (no inventory used).                                                                                                                                                                                                                  |                |
| `orderBeverageInvalidPayment`          | **Preconditions:** CoffeeMaker is running. Inventory holds Coffee `10` and Milk `5`. Recipe "Latte" exists with price `50`, Coffee `3`, Milk `2`.<br><br>1. Browse to [http://localhost:3000](http://localhost:3000).<br>2. Click **Order Beverage**.<br>3. Select **Latte - 50** in the Beverage dropdown.<br>4. Enter `50.5` in Amount Paid.<br>5. Click **Order**.<br>6. Click **Inventory**.<br>7. Click **Order Beverage** and select **Latte - 50**.<br>8. Enter `abc` in Amount Paid.<br>9. Click **Order**.<br>10. Click **Inventory**.                                                          | Step 5 displays the error message "Payment must be a whole number of 0 or more. Your payment of 50.5 has been returned."<br>Step 9 displays the error message "Payment must be a whole number." (nothing is returned because the input was not numeric).<br>In both cases the user stays on the Order Beverage page.<br>Steps 6 and 10 show Coffee `10` and Milk `5` (no inventory used).                                                     |                |
| `orderBeverageInsufficientIngredients` | **Preconditions:** CoffeeMaker is running. Inventory holds Coffee `10` and Milk `5`. Recipe "Latte" exists with price `50`, Coffee `3`, Milk `2`.<br><br>1. Browse to [http://localhost:3000](http://localhost:3000).<br>2. Click **Order Beverage**.<br>3. Select **Latte - 50** in the Beverage dropdown.<br>4. Enter `50` in Amount Paid.<br>5. Click **Order**.<br>6. Enter `50` in Amount Paid and click **Order** again.<br>7. Click **Inventory**.<br>8. Click **Order Beverage** and select **Latte - 50**.<br>9. Enter `50` in Amount Paid.<br>10. Click **Order**.<br>11. Click **Inventory**. | Steps 5 and 6 each display the success message "Your Latte is ready. Your change is 0."<br>Step 7 shows Coffee `4` and Milk `1`, which is not enough Milk for another Latte.<br>Step 10 displays the error message "Not enough ingredients in inventory to make Latte. Your payment of 50 has been returned." and the user stays on the Order Beverage page.<br>Step 11 shows Coffee `4` and Milk `1` (no inventory used by the third order). |                |


## Requirements and Pages

Review the functionality for each CoffeeMaker use case.  Record the path to each page of interest and the information you need to run a manual test of it.

| Use Case | Page (URL)                            | Notes |
|----------|---------------------------------------|-------|
| UC1      | / (also /recipes)                     |       |
| UC2      | /add-recipe                           |       |
| UC3      | /recipes (Delete button on each card) |       |
| UC4      | /edit-recipe/:incomingName            |       |
| UC5      | /inventory                            |       |
| UC6      | /make-recipe                          |       |
| UC7      | /add-ingredient                       |       |



## Frontend

The frontend components and services you may need to use, refactor, or follow as an example.

I used AI to update App.jsx to add my coffee maker components to the frontend.

## REST API

The CoffeeMaker endpoints you may need to use, refactor, or follow as an example.  Compare them with the endpoints WolfCafe already has in the [REST API](design/rest-api.md).

## End Point Design

API documentation is the primary resource explaining what operations are possible in the system, and the reference developers return to with questions about the syntax or behavior of an endpoint.

Document each endpoint with: the name of the function, the relative URL, the HTTP verb, a brief description, the input data, the response status codes, and the response data.

### GET Routes

GET routes *fetch* data. Document the endpoint and the data returned.

#### Example

**Request:** `GET /api/recipes/{name}`

**Function:** `RecipeController.getRecipe(String name)`

**Description:** Returns the recipe with the given name, or 404 if no recipe with that name exists.

**Path parameters**

- `name` — the recipe's name (required)

**Response status codes**

| Status Code | Description |
| ----------- | ----------- |
| `200` | OK |
| `404` | Resource not found |

**Response**

```
Status: 200
{
  "name": "Mocha",
  "price": 10,
  "sugar": 2,
  "milk": 3,
  "coffee": 5,
  "chocolate": 3
}
```

```
Status: 404
```

### PUT/POST Routes

PUT and POST routes *send* data. Document the endpoint and the data sent to it, as well as the data returned — particularly when the response differs from the request.

#### Example

**Request:** `PUT /api/inventory`

**Function:** `InventoryController.updateInventory(InventoryDto inventoryDto)`

**Description:** Updates the CoffeeMaker's inventory with the values provided. The values are *added* to the amounts already stored rather than overwriting them.

Because the inventory is managed as a singleton — there is only one — the URL takes no name or id. This is unusual; PUT routes normally identify a resource in the URL.

**Body parameters**

- `inventoryDto` — an Inventory object. No id is needed here, since it is the *quantity* provided that is added to the existing values. For a resource that isn't a singleton, the id would be part of the URL.

```
{
  "sugar": 2,
  "milk": 2,
  "coffee": 3,
  "chocolate": 4
}
```

**Response status codes**

| Status Code | Description |
| ----------- | ----------- |
| `200` | OK |

**Response**

```
Status: 200
{
  "id": 1,
  "sugar": 2,
  "milk": 2,
  "coffee": 3,
  "chocolate": 4
}
```

### Add Ingredient (UC7)

**Request:** `POST /api/ingredients/`

**Function:** `IngredientController.createIngredient(IngredientDto ingredientDto)`

**Description:** Creates a new ingredient with an initial inventory quantity. Unlike `PUT /api/inventory`, this is not idempotent — a second request with the same name will fail, since the ingredient already exists.

**Body parameters**

- `ingredientDto` — an Ingredient object. No `id` is included, since the ingredient doesn't exist yet.

```
{
  "name": "Pumpkin Spice",
  "quantity": 10
}
```

**Response status codes**

| Status Code | Description |
| ----------- | ----------- |
| `201` | Created |
| `400` | Bad Request — quantity is not a positive integer (Invalid Unit) |
| `409` | Conflict — an ingredient with this name already exists (Duplicate) |

**Response**

```
Status: 201
{
  "id": 5,
  "name": "Pumpkin Spice",
  "quantity": 10
}
```

```
Status: 400
```

```
Status: 409
```

### Add Recipe (UC2)
**Request:** `POST /api/recipes/`

**Function:** `RecipeController.createRecipe(RecipeDto recipeDto)`

**Description:** Creates a new recipe. Fails if there are three recipes in the system, if the price/ingredient quantity isn't positive, no ingredients, or if there's a duplicate recipe name

**Body parameters**

- `recipeDto` — a Recipe object.

```
{
  "name": "Mocha",
  "price": 6,
  "ingredientList": [
    { "name": "Coffee", "amount": 3 },
    { "name": "Milk", "amount": 4 },
    { "name": "Chocolate", "amount": 2 }
  ]
}
```

**Response status codes**

| Status Code | Description                                                                                                                                                                                                  |
|-------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `201`       | Created                                                                                                                                                                                                      |
| `400`       | Bad Request — three recipes already exist (Too Many), price is not a positive integer (Invalid Price), no ingredients given (No Ingredients), or an ingredient unit is not a positive integer (Invalid Unit) |
| `409`       | Conflict — a recipe with this name already exists (Duplicate)                                                                                                                                                |


**Response**

```
Status: 201
{
  "id": 3,
  "name": "Mocha",
  "price": 6,
  "ingredientList": [
    { "name": "Coffee", "amount": 3 },
    { "name": "Milk", "amount": 4 },
    { "name": "Chocolate", "amount": 2 }
  ]
}
```

### Get Recipe by ID (UC4)

**Request:** `GET /api/recipes/id/{id}`

**Function:** `RecipeController.getRecipeById(Long recipeId)`

**Description:** Returns the recipe with the given id, so the Edit Recipe form can be pre-filled with its current price and ingredient list. Returns 404 if no recipe with that id exists.

**Path parameters**

- `id` — the recipe's id (required)

**Response status codes**

| Status Code | Description |
| ----------- | ----------- |
| `200` | OK |
| `404` | Resource not found |

**Response**

```
Status: 200
{
  "id": 3,
  "name": "Mocha",
  "price": 6,
  "ingredientList": [
    { "name": "Coffee", "amount": 3 },
    { "name": "Milk", "amount": 4 },
    { "name": "Chocolate", "amount": 2 }
  ]
}
```

```
Status: 404
```

### Edit Recipe (UC4)
**Request:** `PUT /api/recipes/{id}`

**Function:** `RecipeController.updateRecipe(Long recipeId, RecipeDto recipeDto)`

**Description:** Replaces the price and ingredient list of the recipe with the given id.

**Body parameters**

- `recipeDto` — updated price and ingredient list.

```
{
  "price": 6,
  "ingredientList": [
    { "name": "Coffee", "amount": 3 },
    { "name": "Milk", "amount": 4 },
    { "name": "Chocolate", "amount": 2 }
  ]
}
```

**Response status codes**

| Status Code | Description                                                                                                                                                          |
|-------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `200`       | OK                                                                                                                                                                   |
| `400`       | Bad Request — price is not a positive integer (Invalid Price), no ingredients given (No Ingredients), or an ingredient unit is not a positive integer (Invalid Unit) |
| `404`       | Not Found                                                                                                                                                            |

## Backend

The backend classes (entities, repositories, services, controllers, DTOs, mappers) you may need to use, refactor, or follow as an example.

As of right now, I don't think we need to refactor any of the backend classes.

## User Roles

Which WolfCafe user roles (Admin, Staff, Customer) should be able to use each page, and why.

| Page                                 | Admin | Staff | Customer |
|--------------------------------------|-------|-------|----------|
| Add Recipe/Item (`/add-recipe`)      |       | X     |          |
| Add Ingredient (`/add-ingredient`)   |       | X     |          |
| Update Inventory (`/inventory`)      | X     | X     |          |
| Order Beverage/Item (`/make-recipe`) |       |       | X        |
