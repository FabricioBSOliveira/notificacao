# 📬 Notificacao — Email Notification Microservice

> A focused **Email Notification** microservice built with **Java 17** and **Spring Boot 4**, using **Spring Mail** to dispatch SMTP emails and **Thymeleaf** to render dynamic, production-grade **HTML email templates**. Part of a **multi-service distributed system** alongside [Usuario](https://github.com/FabricioBSOliveira/Usuario), [Agendador-tarefas](https://github.com/FabricioBSOliveira/Agendador-tarefas), and [bff-agendador-tarefas](https://github.com/FabricioBSOliveira/bff-agendador-tarefas).

---

## 🚀 What This Service Does

This is the **notification output layer** of the task scheduling platform. It has a single, well-defined responsibility: receive a notification request and send a formatted email to the target user. That's it — and that's the point.

Specifically, it:

- Receives notification events from `bff-agendador-tarefas` or other services via HTTP
- Renders dynamic, data-driven **HTML emails** using Thymeleaf templates
- Dispatches emails through an SMTP provider via **Spring Mail**
- Runs as a **stateless service** — no database, no persistence layer, no side effects

---

## 🏗️ Architecture & Design Decisions

### Single Responsibility, by Design

This service has no database. No JWT validation. No OpenFeign client. This is not an oversight — it is a deliberate architectural decision that reflects one of the core principles of microservice design: **each service does one thing and does it well**.

The `notificacao` service is a pure **side-effect service**: it receives a payload and fires an email. It holds no state. If it goes down, no data is lost. It can be restarted, scaled, or replaced independently of every other service in the platform.

```
┌───────────────────────────────────────────────────┐
│              REST Controller Layer                 │  ← Receives notification request
├───────────────────────────────────────────────────┤
│               Service Layer                        │  ← Orchestrates mail sending
├───────────────────────────────────────────────────┤
│   Thymeleaf Template Engine  │  Spring JavaMailSender  │
│   (renders HTML email body)  │  (sends via SMTP)       │
└───────────────────────────────────────────────────┘
                      ↓
               📧 User's Inbox
```

### Why Thymeleaf for Emails?

Simple string concatenation produces brittle, unreadable email bodies. Thymeleaf is a **server-side template engine** that lets you author email content as clean HTML files with dynamic variable injection — the same pattern used in enterprise Java applications for web views and document generation.

```html
<!-- templates/email-notificacao.html -->
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<body>
  <h2>Hello, <span th:text="${nomeUsuario}">User</span>!</h2>
  <p>Your task <strong th:text="${nomeTarefa}">Task</strong>
     is scheduled for <span th:text="${dataHora}">now</span>.</p>
</body>
</html>
```

The template is resolved at runtime with real data injected — no string formatting hacks, no HTML escaping issues, no inline HTML literals in Java code.

---

## ⚙️ Tech Stack

| Layer | Technology | Purpose |
|---|---|---|
| Language | Java 17 | LTS release with modern language features |
| Framework | Spring Boot 4 | Production-grade application framework |
| Email Dispatch | Spring Boot Mail (JavaMailSender) | SMTP abstraction for sending emails |
| Template Engine | Thymeleaf | Server-side HTML email template rendering |
| Boilerplate Reduction | Lombok | Eliminates repetitive getter/setter/builder code |
| Build Tool | Gradle 8 | Dependency management and build pipeline |
| Containerization | Docker | Reproducible, portable deployment |
| CI/CD | GitHub Actions | Automated build and test pipeline on every push |

**No database.** **No JWT.** **No OpenFeign.** Every dependency that isn't needed is a dependency that isn't a liability.

---

## 📧 How Email Sending Works

Spring Boot's `JavaMailSender` abstracts the SMTP protocol behind a clean Java interface. The service builds a `MimeMessage`, attaches the Thymeleaf-rendered HTML as the email body, and delivers it via the configured SMTP provider (Gmail, SendGrid, AWS SES, etc.):

```java
@Service
public class NotificacaoService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    public void enviarEmail(NotificacaoDTO dto) {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        // Inject data into the Thymeleaf template
        Context context = new Context();
        context.setVariable("nomeUsuario", dto.getNomeUsuario());
        context.setVariable("nomeTarefa", dto.getNomeTarefa());
        context.setVariable("dataHora", dto.getDataHora());

        String htmlBody = templateEngine.process("email-notificacao", context);

        helper.setTo(dto.getEmailDestinatario());
        helper.setSubject("Task Reminder: " + dto.getNomeTarefa());
        helper.setText(htmlBody, true); // true = send as HTML

        mailSender.send(message);
    }
}
```

---

## 🐳 Running with Docker

The service uses the same **multi-stage Docker build** pattern as the rest of the platform — a full build image discarded after compilation, leaving only a lean Alpine JDK runtime.

```bash
git clone https://github.com/FabricioBSOliveira/notificacao.git
cd notificacao

docker build -t notificacao .
docker run -p 8082:8082 \
  -e SPRING_MAIL_HOST=smtp.gmail.com \
  -e SPRING_MAIL_PORT=587 \
  -e SPRING_MAIL_USERNAME=your@email.com \
  -e SPRING_MAIL_PASSWORD=your-app-password \
  notificacao
```

> For the full platform (all 4 services), see the [bff-agendador-tarefas](https://github.com/FabricioBSOliveira/bff-agendador-tarefas) repository.

---

## 🛠️ Running Locally Without Docker

**Prerequisites:** Java 17, an SMTP-enabled email account (Gmail with App Password, Mailtrap for dev, etc.)

```bash
# Configure your SMTP credentials
SPRING_MAIL_HOST=smtp.gmail.com
SPRING_MAIL_PORT=587
SPRING_MAIL_USERNAME=your@email.com
SPRING_MAIL_PASSWORD=your-app-password
SPRING_MAIL_PROPERTIES_MAIL_SMTP_AUTH=true
SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE=true

# Build and run
./gradlew bootRun
```

The service starts on **port 8082**.

> 💡 **Tip for development:** Use [Mailtrap](https://mailtrap.io) as a fake SMTP inbox — it captures emails without actually sending them, letting you inspect the rendered HTML without spamming real inboxes.

---

## 📡 API Endpoint

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/notificacao/enviar` | Send an HTML email notification |

**Request body example:**

```json
{
  "emailDestinatario": "user@example.com",
  "nomeUsuario": "Fabricio",
  "nomeTarefa": "Submit project report",
  "dataHora": "2026-05-20T14:00:00"
}
```

---

## 🧪 Testing

- **JUnit 5** + to be implemented
- All tests are fully self-contained and run in CI without external dependencies

---

## 📦 Project Structure

```
src/
└── main/
    ├── java/com/Fabricio/
    │   ├── controller/     # REST endpoint (receives notification requests)
    │   ├── service/        # Mail sending logic + Thymeleaf rendering
    │   └── dto/            # NotificacaoDTO (request payload)
    └── resources/
        └── templates/
            └── email-notificacao.html   # Thymeleaf HTML email template
```

The `templates/` directory is where the HTML lives — the 23% of this repo that isn't Java. Those are the email layouts that end up in users' inboxes.

---

## 🌐 Platform Context

This service is one of four microservices in a distributed task scheduling platform:

```
[bff-agendador-tarefas]  ← API Gateway / BFF — single entry point for clients
        │
        ├── [Usuario]              ← Identity & Auth (PostgreSQL)
        ├── [Agendador-tarefas]    ← Task scheduling logic (MongoDB)
        └── [notificacao]          ← Email notification dispatch ← YOU ARE HERE
                                      (Stateless · No DB · Thymeleaf HTML emails)
```

Each service is independently deployable. `notificacao` is deliberately the lightest: no database, no auth layer, no external service calls. It receives a request, sends an email, returns. This makes it trivially scalable and easy to swap for a different notification channel (SMS, push, Slack) in the future without touching any other service.

---

## 👨‍💻 About the Author
Fabricio Butti Santos de Oliveira — a career-switching Mechanical Engineer who chose to apply the same systems constrains, critical thinking and problem solving to distributed software.
[![GitHub](https://img.shields.io/badge/GitHub-FabricioBSOliveira-181717?style=flat&logo=github)](https://github.com/FabricioBSOliveira)
