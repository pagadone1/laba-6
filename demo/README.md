# Car Service Application

Spring Boot REST API with in-memory storage. No database required.

## Run

```bash
mvn spring-boot:run
```

Runs on port 8080.

## API Endpoints

Each entity has CRUD operations:

| Entity | Base Path |
|--------|-----------|
| Customer | `POST/GET/PUT/DELETE /api/customers` |
| Vehicle | `POST/GET/PUT/DELETE /api/vehicles` |
| Mechanic | `POST/GET/PUT/DELETE /api/mechanics` |
| Part | `POST/GET/PUT/DELETE /api/parts` |
| ServiceOrder | `POST/GET/PUT/DELETE /api/service-orders` |

- `POST /api/{entity}` - Create
- `GET /api/{entity}` - Get all
- `GET /api/{entity}/{id}` - Get by id
- `PUT /api/{entity}/{id}` - Update
- `DELETE /api/{entity}/{id}` - Delete
