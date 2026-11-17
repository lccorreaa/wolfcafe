# WolfCafe Requirements

## Introduction

The requirements document includes terms, user roles, personas, functional requirements, non-functional requirements, constraints, any questions about the requirements, and any changes made to the requirements during developement.

## Glossary

| Term             | Definition                                                                                                                                  |
|------------------|---------------------------------------------------------------------------------------------------------------------------------------------|
| Administrator    | User that is in charge of cafe operations including updating inventory, adding/editing drinks, streamlining efficiency                      |
| Staff            | User that is responsible for taking orders, making drinks, and interacting with the customer during payment                                 |
| Customer         | User that is responsible for ordering and paying for the drink of choice                                                                    |
| Adversarial User | User that is interested in disrupting or harming cafe operations by taking advantage of weak controls and spamming the system with requests |

## Personas

1. Allison Smith: Administrator
   1. Personal Characteristics: organized, responsible, analytical
   2. Interests: business performance, inventory management, customer satisfaction
   3. Background and Skills: several years experience, comfortable using technology for business operations, can learn administrator software quickly
   4. Motivations and Attitudes: wants to streamline orders, monitor sales, track inventory efficiently
   5. Attitude to Technology: positive response to technology that is reliable, uses documentation and exploration to understand the software
2. James Carter: Staff
   1. Personal Characteristics: friendly, practical, fast-paced
   2. Interests: coffee preparation, teamwork, customer service
   3. Background and Skills: couple years experience working in cafés, familiar with digital order systems, not very experienced in technical troubleshooting
   4. Motivations and Attitudes: wants to process orders quickly, reduce mistakes, and avoid confusion
   5. Attitude to Technology: positive response if the technology does not complicate existing workflows, uses exploration to understand the software after short demo
3. Olivia Brown: Customer
   1. Personal Characteristics: busy, independent, convenience-oriented
   2. Interests: specialty coffee drinks, mobile apps, discovering local cafés
   3. Background and Skills: student at local university, regularly uses mobile apps for ordering and online payments
   4. Motivations and Attitudes: wants to browse menu, customize drinks, place order, and pay conveniently
   5. Attitude to Technology: positive response if the technology is user-intuitive, uses exploration to understand software (students tend to not have much time for reading docs on how the system works)
4. Alex Morgan: Adversarial User
   1. Personal Characteristics: persistent, opportunistic, willing to misuse online services
   2. Interests: taking advantage of weak controls, submitting excessive requests, disrupting normal system use
   3. Background and Skills: basic to intermediate knowledge of websites/online forms, can automate simple requests for spamming the system
   4. Motivations and Attitudes: wants to flood the system with requests to disrupt normal system use, wants to exploit weak protections
   5. Attitude to Technology: instrumental and adversarial, uses technology to disrupt café operations

## Functional Requirements
If you carry forward a CoffeeMaker use case, list it here.  Highlight any modified text (in **bold** or _italic_) so the change is visible, including new or changed logging.

| Use Case | Title                                                                | New or Modified |
|----------|----------------------------------------------------------------------|-----------------|
| UC1      | [Customer View Menu](use-cases/uc1-customer-view-menu.md)            | New             |
| UC2      | [Customer Order](use-cases/uc2-cutomer-order.md)                     | New             |
| UC3      | [Customer Pickup Status](use-cases/uc3-customer-pickup-status.md)    | New             |
| UC4      | [Admin View Menu](use-cases/uc4-admin-view-menu.md)                  | New             |
| UC5      | [Admin Edit Users](use-cases/uc5-admin-edit-users.md)                | New             |
| UC6      | [Admin Set Tax Rate](use-cases/uc6-admin-set-tax-rate.md)            | New             |
| UC7      | [Admin Create Staff](use-cases/uc7-admin-create-staff.md)            | New             |
| UC8      | [Staff View Menu](use-cases/uc8-staff-view-menu.md)                  | New             |
| UC9      | [Staff Fulfill Order](use-cases/uc9-staff-fulfill-order.md)          | New             |
| UC10     | [Staff Create Item](use-cases/uc10-staff-create-item.md)             | New             |
| UC11     | [Staff Update Inventory](use-cases/uc11-staff-update-inventory.md)   | New             |
| UC12     | [Customer Create Account](use-cases/uc12-customer-create-account.md) | New             |

## Non-Functional Requirements

List requirements that are not about system functionality, such as performance or accessibility, as a numbered list.

1. Frontend should be accessible: information shouldn't be conveyed by color alone, all interactive elements are reachable in a logical tab order, and form controls and images have text labels or alternative text usable by screen readers.
2. Passing unit and integration tests that cover at least 70% of lines and 50% of branches in the backend.
3. System tests documented in system-tests.md, run by hand and passing before each delivery 
4. The build backend and frontend GitHub Actions should pass on every pull request before merge to development and main
5. The backend shouldn't have any checkstyle or pmd errors/warnings
6. All users should log in with a username and password to access the system, unless a customer is placing an anonymous order.

## Constraints

List constraints on development from the customer or from the existing projects as a numbered list.

1. The backend implementation must be written in Java using Spring Boot and built with Maven.
2. The backend unit testing should be completed in JUnit.
3. The frontend should be implemented in React (JavaScript) using Vite, with Bootstrap for styling.
4. The database should be in MySQL.
5. The system should support three roles: Admin, Staff, and Customer.

## Questions

TODO - Record questions about the scope of the requirements, and the answers from the teaching staff.

## Changes During Development

TODO - At final delivery, describe how these requirements changed during development, or say that they did not.
