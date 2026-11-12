# OSIV Demo: Open Session in View problem and fix

A minimal Spring Boot 3.5 / Java 21 app that shows why `spring.jpa.open-in-view` should be turned off for REST APIs (for example, a backend serving an Angular UI), and how to fix the code so it works without it.

## The scenario

- 20 customers and 50 orders with 3 items each, in in-memory H2
- `PurchaseOrder.customer` and `PurchaseOrder.items` are **LAZY**
- Two endpoints return the same data:

| Endpoint | Approach |
| --- | --- |
| `GET /api/bad/orders` | Service returns **entities**; Jackson lazy-loads associations while writing JSON |
| `GET /api/good/orders` | Service uses **`join fetch`** and maps to **DTOs** inside the transaction |
| `GET /api/good/orders/{id}` | Same fix using **`@EntityGraph`** |

## Results

| | OSIV **on** (Spring Boot default) | OSIV **off** (recommended) |
| --- | --- | --- |
| `/api/bad/orders` | 200 OK, but **71 SQL queries** (1 + 20 + 50, hidden N+1) | **500**: `LazyInitializationException` (fails fast) |
| `/api/good/orders` | 200 OK, **1 query** | 200 OK, **1 query** |

With OSIV on, the bad code looks fine but quietly runs 71 queries and holds a DB connection while the JSON is written. Turning OSIV off exposes the problem immediately. The fixed code is fast and behaves the same either way.

## Run it

Prove it with tests (prints the query counts):

```bash
./mvnw test
```

```
[OSIV ON ] /api/bad/orders  -> 71 SQL statements
[OSIV ON ] /api/good/orders -> 1 SQL statement(s)
[OSIV OFF] /api/bad/orders  -> 500, root cause: org.hibernate.LazyInitializationException: Could not initialize proxy ... - no session
[OSIV OFF] /api/good/orders -> 1 SQL statement(s)
```

Or run the app and call the endpoints yourself. Each request logs its SQL count (`>>> GET /api/... -> N SQL statement(s)`), and the individual SQL statements are printed too.

```bash
# OSIV off (the project's setting)
./mvnw spring-boot:run

# OSIV on (Spring Boot's default)
./mvnw spring-boot:run -Dspring-boot.run.profiles=osiv-on
```

```bash
curl localhost:8080/api/bad/orders
curl localhost:8080/api/good/orders
curl localhost:8080/api/good/orders/1
```

## Where to look

| File | What it shows |
| --- | --- |
| `src/main/resources/application.yml` | `open-in-view: false`, plus the `osiv-on` profile |
| `service/OrderService.java` | `findAllEntities()` (problem) vs `findAllDtos()` (fix) |
| `repo/OrderRepository.java` | `join fetch` query and `@EntityGraph` method |
| `service/OrderDto.java` | DTOs the API returns instead of entities |
| `config/QueryCountFilter.java` | Logs SQL statements per request |
| `src/test/.../OsivOnTest.java`, `OsivOffTest.java` | Automated proof of both behaviours |

## The fix in short

```yaml
spring:
  jpa:
    open-in-view: false
```

```java
// Repository: load what the response needs in ONE query
@Query("select distinct o from PurchaseOrder o join fetch o.customer join fetch o.items")
List<PurchaseOrder> findAllWithCustomerAndItems();

@EntityGraph(attributePaths = {"customer", "items"})
Optional<PurchaseOrder> findWithDetailsById(Long id);

// Service: map to DTOs while the transaction is open
@Transactional(readOnly = true)
public List<OrderDto> findAllDtos() {
    return orderRepository.findAllWithCustomerAndItems().stream().map(OrderDto::from).toList();
}
```

Avoid these "fixes", which bring the hidden queries back: `FetchType.EAGER`, `hibernate.enable_lazy_load_no_trans=true`, and `@Transactional` on controllers.

## Notes

- `join fetch` on two `List` collections in one query throws `MultipleBagFetchException`. Fetch one collection, and use `@BatchSize` or a second query for the other.
- Don't combine collection `join fetch` with pagination (Hibernate paginates in memory). For paged lists, use a DTO projection.
- `QueryCountFilter` uses global Hibernate statistics. It's for demos only, not production.
