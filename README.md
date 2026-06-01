# EPAM Buy Ticket - Multi-Agent AI CRM System

This project is a high-performance, modern Java application demonstrating **Multi-Agent Systems (MAS)** and **Spring AI** integration. It serves as a travel and media booking CRM where AI agents (Orchestrator, Flight, Hotel, Media) collaborate to fulfill complex user requests.

## 🚀 Key Features

- **Multi-Agent Architecture**: 
  - **Orchestrator**: Analyzes user intent and routes tasks.
  - **Flight Agent**: Handles dynamic flight search and booking.
  - **Hotel Agent**: Integrated with Booking.com (via RapidAPI) for real-time accommodation search.
  - **Media Group**: Script, Image, Video, and Motion Design agents for automated content production.
- **Spring AI & LLM**: Integration with **Groq (Llama 3)**, **OpenAI**, and **Luma AI** for text, image, and video generation.
- **RAG (Retrieval-Augmented Generation)**: Uses vector search to provide agents with customer context and business rules.
- **Reactive Performance**: Built with **Java 21 Virtual Threads** (Project Loom) for efficient handling of blocking I/O.
- **Booking Planner**: A dedicated UI to review, confirm, or cancel AI-generated travel proposals.

## 🛠 Tech Stack

- **Backend**: Java 21, Spring Boot 3.2+, Spring Data JPA, Spring Security.
- **AI/ML**: Spring AI, Vector Stores, OpenAI/Groq API.
- **Frontend**: Thymeleaf, Bootstrap 5, HTMX/JavaScript.
- **Database**: H2 (In-memory for demo) / MySQL support.
- **Documentation**: SpringDoc OpenAPI (Swagger).

## 🚦 Quick Start

### Prerequisites
- JDK 21
- Maven 3.9+

### Configuration
Update [application.properties](src/main/resources/application.properties) with your API keys:
```properties
# AI Providers
app.ai.providers[0].api-key=your_groq_key
app.ai.providers[1].api-key=your_openai_key

# RapidAPI (Booking.com)
app.booking.api-key=your_rapidapi_key
```

### Running the App
**Windows**:
```bash
run.bat
```
**Linux/macOS**:
```bash
./run.sh
```
Access the UI at: `http://localhost:8082/`

## 🔒 Security
- **CSRF Protection**: Enabled for all state-changing operations.
- **Authentication**: Basic Auth / Form Login enabled.
- **Demo Credentials**: `admin` / `admin`.

## 📂 Project Structure
- `src/main/java/.../agent`: Core Multi-Agent logic and Orchestration.
- `src/main/java/.../skills`: Actionable tools used by agents (Booking, RAG).
- `src/main/java/.../controller`: REST and Web endpoints.
- `src/main/resources/templates`: UI Layouts (CRM & Planner).

---
*Developed as a demonstration of AI-driven CRM solutions.*
