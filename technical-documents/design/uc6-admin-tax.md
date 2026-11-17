# Feature Design: UC6 Set Tax Rate


**Use Cases:** [UC6: Admin Set Tax Rate](../requirements/use-cases/uc6-admin-set-tax-rate.md)

**Owners:** Sandra Jose (sajose): frontend and backend. Branches: `sajose/i1a-design-uc6` (this document)

## Static Design

### Backend: Class Diagram

UC6 adds a new tax rate that follows the same pattern as Inventory: one entity stored as a single row, with a repository, service, mapper, DTO, and controller. No existing classes are edited or removed. The controller uses the existing `AuditLogger`, and bad input is reported with the existing `WolfCafeAPIException`, which `GlobalExceptionHandler` already turns into an error response.

```mermaid
classDiagram
direction LR

class TaxRate {
  <<Entity>>
  -Long id
  -Double rate
  +TaxRate()
  +TaxRate(Long id, Double rate)
  +getId() Long
  +setId(Long id) void
  +getRate() Double
  +setRate(Double rate) void
}

class TaxRateDto {
  -Long id
  -Double rate
  +getId() Long
  +setId(Long id) void
  +getRate() Double
  +setRate(Double rate) void
}

class TaxRateMapper {
  +mapToTaxRateDto(TaxRate taxRate)$ TaxRateDto
  +mapToTaxRate(TaxRateDto taxRateDto)$ TaxRate
}

class TaxRateRepository {
  <<interface>>
}

class JpaRepository {
  <<interface>>
}

class TaxRateService {
  <<interface>>
  +getTaxRate() TaxRateDto
  +updateTaxRate(TaxRateDto taxRateDto) TaxRateDto
}

class TaxRateServiceImpl {
  -TaxRateRepository taxRateRepository
  -findSingleTaxRate() Optional~TaxRate~
  +getTaxRate() TaxRateDto
  +updateTaxRate(TaxRateDto taxRateDto) TaxRateDto
}

class TaxRateController {
  -TaxRateService taxRateService
  -AuditLogger auditLogger
  +getTaxRate() ResponseEntity~TaxRateDto~
  +updateTaxRate(TaxRateDto taxRateDto) ResponseEntity~TaxRateDto~
}

class AuditLogger {
  <<existing>>
  +log(String action) void
}

class WolfCafeAPIException {
  <<existing>>
  -HttpStatus status
  -String message
}

JpaRepository <|-- TaxRateRepository : JpaRepository of TaxRate, Long
TaxRateService <|.. TaxRateServiceImpl
TaxRateController --> TaxRateService
TaxRateController --> AuditLogger
TaxRateServiceImpl --> TaxRateRepository
TaxRateServiceImpl ..> TaxRateMapper
TaxRateServiceImpl ..> WolfCafeAPIException : throws
TaxRateMapper ..> TaxRate
TaxRateMapper ..> TaxRateDto
TaxRateRepository ..> TaxRate
```

New classes:
- `TaxRate` (entity): the single row holding the current tax rate as a percent, e.g. `8.0` for 8%.
- `TaxRateDto`: carries the rate between the frontend and backend.
- `TaxRateMapper`: converts between `TaxRate` and `TaxRateDto`.
- `TaxRateRepository`: JPA repository for `TaxRate`.
- `TaxRateService` / `TaxRateServiceImpl`: gets the single rate (creating it with a default if none exists) and validates and saves updates.
- `TaxRateController`: REST endpoints at `/api/tax`; logs each successful update.

Frontend additions: `SetTaxRateComponent.jsx` (the page) and `TaxRateService.js` (axios calls to `/api/tax`).

### Frontend: Wireframes

![Updated Set Tax Rate wireframe](images/WolfCafe%20Admin%20Settings.png)
![Set Tax Rate wireframe](images/uc6-set-tax-rate.png)

Set Tax Rate: the page loads the current tax rate into an editable text box. When the admin presses Save Tax Rate, the new rate is saved and a success message is shown. If the value is not a positive number, an error message appears under the text box and the admin can fix the value.

## Dynamic Design

### Backend: Sequence Diagram

```mermaid
sequenceDiagram
actor Admin
participant SetTaxRateComponent
participant TaxRateController
participant TaxRateServiceImpl
participant TaxRateRepository
participant AuditLogger

Admin->>SetTaxRateComponent: select "Set Tax Rate" from Admin Menu
SetTaxRateComponent->>TaxRateController: GET /api/tax
TaxRateController->>TaxRateServiceImpl: getTaxRate()
TaxRateServiceImpl->>TaxRateRepository: findAll()
TaxRateRepository-->>TaxRateServiceImpl: list of TaxRate
opt no tax rate row exists yet
    TaxRateServiceImpl->>TaxRateRepository: save(new TaxRate with default rate 0.0)
    TaxRateRepository-->>TaxRateServiceImpl: saved TaxRate
end
TaxRateServiceImpl-->>TaxRateController: TaxRateDto
TaxRateController-->>SetTaxRateComponent: 200 OK with TaxRateDto
SetTaxRateComponent-->>Admin: show current rate in editable text box

Admin->>SetTaxRateComponent: edit rate and press Submit
alt rate is not a positive number (frontend check)
    SetTaxRateComponent-->>Admin: show error, keep form open for editing
else rate looks valid
    SetTaxRateComponent->>TaxRateController: PUT /api/tax (TaxRateDto: rate)
    TaxRateController->>TaxRateServiceImpl: updateTaxRate(taxRateDto)
    alt rate is null, NaN, infinite, or <= 0
        TaxRateServiceImpl-->>TaxRateController: throw WolfCafeAPIException(BAD_REQUEST)
        TaxRateController-->>SetTaxRateComponent: 400 Bad Request with ErrorDetails
        SetTaxRateComponent-->>Admin: show error message, keep form open for editing
    else rate is a positive float
        TaxRateServiceImpl->>TaxRateRepository: findAll()
        TaxRateRepository-->>TaxRateServiceImpl: single TaxRate
        TaxRateServiceImpl->>TaxRateServiceImpl: setRate(rate)
        TaxRateServiceImpl->>TaxRateRepository: save(taxRate)
        TaxRateRepository-->>TaxRateServiceImpl: saved TaxRate
        TaxRateServiceImpl-->>TaxRateController: TaxRateDto
        TaxRateController->>AuditLogger: log("Admin user 'jdoe' set the tax rate to '8.0'%")
        TaxRateController-->>SetTaxRateComponent: 200 OK with updated TaxRateDto
        SetTaxRateComponent-->>Admin: show new rate and success message
    end
end
```

### Frontend: Navigation Map

Login → Admin Menu → Set Tax Rate → (Save Tax Rate) → Set Tax Rate with the saved rate, or with an error message to fix

The admin can also reach Set Tax Rate (`/set-tax-rate`) from any page through the **Settings** tab in the header, which only the admin sees.

## Design Decisions

- **Singleton entity.** There is only one tax rate for the whole cafe, so it is stored as a single `TaxRate` row. `TaxRateServiceImpl` finds it by reading the only row (`findAll().stream().findFirst()`) rather than a hard-coded id, the same way `InventoryServiceImpl` does.
- **Default rate.** If no row exists yet, `getTaxRate()` creates one with a rate of `0.0` so the page always has a value to show. An admin must then set a positive rate.
- **Rate is stored as a percent.** `8.0` means 8%, matching both what the admin types and the log message. Code that computes order totals later divides by 100.
- **Admin-only updates, wider reads.** Only admins can `PUT` the rate. `GET` is open to Admin, Staff, and Customer because placing and paying for orders in later use cases needs the rate to compute tax.

## REST API

See [REST API](rest-api.md). UC6 adds:

| Method | Path       | Roles                  | Request Body       | Responses                                                                                      |
|--------|------------|------------------------|--------------------|------------------------------------------------------------------------------------------------|
| GET    | `/api/tax` | Admin, Staff, Customer |                    | 200 OK with `TaxRateDto`: id, rate (creates the rate with a default of 0.0 if none exists yet) |
| PUT    | `/api/tax` | Admin                  | `TaxRateDto`: rate | 200 OK with the updated `TaxRateDto`; 400 if the rate is missing or not a positive number      |

Example `PUT /api/tax` request body:

```json
{ "rate": 8.0 }
```

Example 400 response:

```json
{
  "timeStamp": "2026-10-28T14:03:11.512",
  "message": "Tax rate must be a positive number.",
  "details": "uri=/api/tax"
}
```

## Changes During Implementation

TODO - At final delivery, describe how the design changed during implementation, with updated diagrams, or say that you followed it as designed.
