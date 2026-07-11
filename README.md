# Synapse Helpdesk: AI-Powered IT Ticketing System

🚀 **Live Demo:** [Insert your React Render URL here]
⚙️ **Backend API:** [Insert your Spring Boot Render URL here]

Synapse is an enterprise-grade IT Helpdesk ticketing system featuring role-based access control (RBAC), secure JWT authentication, and intelligent ticket triaging powered by Google's Gemini AI.

## 🌟 Key Features
* **AI Ticket Triaging:** Integrates Google Gemini AI to automatically analyze ticket descriptions and assign priority levels (CRITICAL, HIGH, MEDIUM, LOW).
* **Role-Based Workflows:** Strict data isolation between `Employees` (who can only see their own tickets) and `IT Agents` (who manage the global queue).
* **Secure Authentication:** Stateless JWT-based authentication via Spring Security.
* **Modern UI/UX:** Fully responsive, Tailwind-styled React dashboard.

## 🛠️ Tech Stack
* **Frontend:** React, Vite, Tailwind CSS, Axios, React Router
* **Backend:** Java 17, Spring Boot 3, Spring Security, Spring Data JPA
* **Database:** PostgreSQL (Hosted on Neon.tech)
* **AI/ML:** Google Gemini API
* **DevOps:** Docker, Render (Cloud Hosting), Git Flow Architecture

## 🏗️ Architecture
The application follows a standard Microservices-ready pattern. The frontend is a static React SPA hosted on Render's CDN, communicating via REST to a containerized Spring Boot API. Passwords and secrets are injected purely via environment variables to maintain zero-trust cloud security.