# Feature Design: UC8 Staff View Orders

**Use Cases:** [UC9: Staff View Orders](../requirements/use-cases/uc9-staff-fulfill-order.md)

**Owners:** Lucas (lccorrea): frontend design | Branch: lccorrea/1a-frontend-design 
Shreya (snkamath): backend implementation | Branch: snkamath/iteration1B-backend

## Static Design

### Backend: Class Diagram

![UML Diagram](../design/wolf_cafe_diagram.png)
Relevant classes:
- Order
- OrderDto
- OrderServiceImpl
- StaffService
- OrderController

### Frontend: Wireframes

![Updated UC9 Wireframe](../design/images/WolfCafe%20—%20UC9%20·%20Orders.png)
![Updated UC9 Wireframe](../design/images/WolfCafe%20—%20UC9%20·%20Order%20fulfilled.png)
![Updated UC9 Wireframe](../design/images/WolfCafe%20—%20UC9%20·%20Insufficient%20inventory.png)
![UC9 Wireframe](../design/images/uc9-wireframe.png)

## Dynamic Design

### Backend: Sequence Diagram

![UC9-sequence](../design/images/uc9-sequence.png)

### Frontend: Navigation Map

There is no navigation for this feature. Everything is handled in one page, there is only one wireframe

## Design Decisions

Order - Creates order entity with items, amounts, and status
OrderServiceImpl - Handles a list of order objects, such as creating, deleting, editing, and looking up orders by name
StaffService - Handles fulfillment of orders
OrderController - Handle API endpoints, such as POST, GET and DELETE orders

## REST API

[REST API](../design/rest-api.md)
- GET /api/orders
- PUT /api/orders/{id}/fulfill


## Changes During Implementation

TODO - At final delivery, describe how the design changed during implementation, with updated diagrams, or say that you followed it as designed.
