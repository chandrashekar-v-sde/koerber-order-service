# Order Service

Order Service accepts product orders and communicates with Inventory Service to reserve stock.

---

# 1️⃣ Overview

Responsibilities:
- Accept product orders
- Call Inventory Service for stock reservation
- Store order details
- Fetch reservation details

Tech Stack:
- Spring Boot
- Spring Data JPA
- H2 Database
- Liquibase
- RestTemplate
- Log4j2
- JUnit 5 & Mockito

Runs on:
http://localhost:8080

Inventory Service must run on:
http://localhost:8081

application.properties:
inventory.service.url=http://localhost:8081

---

# 2️⃣ Setup Instructions

mvn clean install
mvn spring-boot:run

H2 Console:
http://localhost:8080/h2-console

JDBC=jdbc:h2:mem:orderdb

---

# 3️⃣ API Documentation

---------------------------------------
POST /order
---------------------------------------

Description:
Places a new order and reserves inventory.

Request Body:

{
"productId": 1002,
"quantity": 3
}

Success Response:

{
"orderId": 5012,
"productId": 1002,
"productName": "Smartphone",
"quantity": 3,
"status": "PLACED",
"reservedFromBatchIds": [9],
"message": "Order placed. Inventory reserved"
}

Inventory Failure Response:

{
"status": 400,
"message": "Insufficient inventory available"
}

---------------------------------------
GET /order/{orderId}
---------------------------------------

Description:
Fetch order details including reserved batch IDs.

Example:
GET http://localhost:8080/order/5012

Success Response:

{
"orderId": 5012,
"productId": 1002,
"productName": "Smartphone",
"quantity": 3,
"status": "PLACED",
"reservedFromBatchIds": [9],
"message": "Order fetched successfully"
}

If order not found:

{
"status": 404,
"message": "Order not found with id: 5012"
}

If inventory service unreachable:

{
"status": 500,
"message": "Failed to fetch reservation details from inventory service"
}
