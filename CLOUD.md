# ☁️ Cloud-Native Customer CRM Project

## 🛠 Технологический стек (Stack)

### Core Backend
- **Language**: [Java 21](https://www.oracle.com/java/technologies/javase/21-relnote-issues.html) (LTS) - Virtual Threads (Project Loom).
- **Framework**: [Spring Boot 3.2.5](https://spring.io/projects/spring-boot) - Cloud-native optimization.
- **Build System**: [Maven 3.9+](https://maven.apache.org/).

### AI & Intelligence
- **Spring AI**: Integration with [OpenAI GPT-3.5/4](https://openai.com/).
- **Features**: Automatic notes summarization, response suggestion.

### Data & Persistence
- **ORM**: Spring Data JPA / Hibernate 6.
- **Database**: 
  - Development: H2 In-memory (Auto-configured).
  - Production: MySQL 8.0 (Ready for configuration).
- **Validation**: Jakarta Bean Validation.

### Web & API
- **Templating**: Thymeleaf (Server-side rendering).
- **Frontend**: Bootstrap 5.3 (Responsive UI).
- **API Documentation**: SpringDoc OpenAPI (Swagger UI).
- **API Standard**: RESTful, Versioning (v1), RFC 7807 (ProblemDetail).

### DevOps & Infrastructure
- **CI/CD**: GitHub Actions (Maven Build & Test).
- **Observability**: Spring Boot Actuator.
- **Concurrency**: Virtual Threads for high-throughput blocking I/O.

---

## 🚀 Реализация (Implementation Roadmap)

### ✅ Завершено (Completed)
- [x] **Project Initialization**: Maven structure with Java 21 & Spring Boot 3.2.
- [x] **Domain Model**: JPA Entity `Customer` with indexing and validation.
- [x] **Service Layer**: Transactional logic with DTO mapping.
- [x] **REST API**: Full CRUD with v1 versioning.
- [x] **Quick Search**: Efficient search by name/email/address using JPQL.
- [x] **AI Integration**: Service for notes summarization via OpenAI.
- [x] **Web UI**: Thymeleaf templates for dashboard and customer forms.
- [x] **Error Handling**: Global Exception Handler using `ProblemDetail`.
- [x] **Documentation**: README with C4 diagrams and API overview.
- [x] **CI/CD**: Basic GitHub Actions workflow.

### ⏳ В процессе / Необходимо реализовать (Backlog)
- [ ] **Dockerization**: Create `Dockerfile` and `docker-compose.yml` for cloud deployment.
- [ ] **Security**: Spring Security integration (OAuth2/JWT).
- [ ] **Monitoring**: Prometheus & Grafana dashboard for Actuator metrics.
- [ ] **Caching**: Redis integration for high-frequency search queries.
- [ ] **Logging**: Structured JSON logging (Logstash/ELK ready).
- [ ] **Testing**: Increase coverage with Testcontainers (Integration tests with real DB).
- [ ] **API Versioning v2**: Introduce breaking changes support.

---

## 📈 Архитектурные принципы
- **Stateless API**: Легкое масштабирование в облаке.
- **Virtual Threads**: Эффективное использование ресурсов без Reactive-стека.
- **Separation of Concerns**: Четкое разделение на Entity, DTO, Service и Controller.
- **API First**: Автоматическая генерация документации через Swagger.
