# 🚀 FinanceAI – Asistente Inteligente de Salud Financiera
 
![Estado del Proyecto](https://img.shields.io/badge/Estado-En_Pulido_Final-yellow)
![Versión](https://img.shields.io/badge/Versión-1.0.0-green)
![Hackathon](https://img.shields.io/badge/Hackathon-ONE-orange)
![Oracle](https://img.shields.io/badge/Oracle-OCI-red)
![Java](https://img.shields.io/badge/Java-21-blue)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-brightgreen)
![Vue](https://img.shields.io/badge/Vue-3-42b883)
![Python](https://img.shields.io/badge/Python-FastAPI-informational)
 
---
 
# 📋 Índice
 
* Estado del proyecto
* Descripción del proyecto
* Objetivos
* Sector empresarial
* Tecnologías
* Arquitectura
* Capturas de pantalla
* Estructura del proyecto
* Instalación y ejecución local
* Ejemplo de uso
* Documentación
* Roadmap
* Equipo
* Licencia
---
 
# 🚧 Estado del proyecto
 
El MVP se encuentra **funcional de punta a punta**: registro/inicio de sesión, gestión de transacciones, clasificación automática de gastos, análisis de perfil financiero, recomendaciones, historial de análisis y modo invitado. El equipo se encuentra en etapa de pulido final de detalles antes de la entrega del Hackathon ONE.
 
---
 
# 📖 Descripción del proyecto
 
**FinanceAI** es una solución inteligente orientada a mejorar la salud financiera de los usuarios mediante el análisis automático de sus transacciones y hábitos financieros.
 
A partir de la información proporcionada por el usuario, el sistema es capaz de analizar su comportamiento financiero y generar información útil que facilita una mejor toma de decisiones.
 
Entre la información procesada se encuentra:
 
* Ingreso mensual (manual o calculado automáticamente a partir de transacciones).
* Nivel de endeudamiento (registrado mediante deudas/cuotas).
* Frecuencia de ahorro (por encuesta inicial, o calculada automáticamente con historial de 3+ meses).
* Historial de transacciones.
* Descripción y monto de cada gasto.
El sistema soporta tanto usuarios registrados (con historial persistente) como usuarios invitados (análisis inmediato sin necesidad de crear cuenta, sin persistencia de datos).
 
---
 
# 🎯 Objetivos
 
El proyecto desarrolla un MVP capaz de:
 
* Clasificar automáticamente las transacciones financieras mediante un modelo de NLP (TF-IDF + Regresión Logística).
* Identificar patrones de consumo.
* Analizar el perfil financiero del usuario (Saludable / En observación / En riesgo) mediante un modelo de clasificación (Random Forest).
* Generar recomendaciones personalizadas.
* Exponer los resultados mediante una API REST documentada.
* Integrar Oracle Cloud Infrastructure (OCI Object Storage) como parte de la arquitectura.
---
 
# 🏢 Sector Empresarial
 
**Fintech · Educación Financiera · Carteras Digitales**
 
FinanceAI está dirigido a personas que desean comprender mejor sus hábitos financieros, organizar sus gastos y tomar decisiones más informadas sobre el manejo de su dinero, en un contexto de equipo y audiencia internacional (soporte de múltiples monedas).
 
---
 
# 🛠️ Tecnologías
 
### Backend
 
* Java 21
* Spring Boot
* Spring Security + JWT
* Spring Data JPA
* Maven
* Flyway
* Lombok
* Swagger / OpenAPI
### Ciencia de Datos
 
* Python
* FastAPI
* Pandas
* Scikit-Learn
* Joblib
* Jupyter Notebook
* Docker
### Frontend
 
* Vue 3 + Vite
* Tailwind CSS
* Vue Router
* Chart.js (vue-chartjs)
* Phosphor Icons
### Infraestructura
 
* PostgreSQL 17
* Oracle Cloud Infrastructure (OCI) — Object Storage, para almacenamiento de los artefactos del modelo entrenado.
* Docker — contenedorización del microservicio de Machine Learning.
---
 
# 🏗️ Arquitectura
 
La solución está organizada en tres módulos principales que se comunican mediante peticiones HTTP/JSON:
 
* **Frontend** (Vue 3), encargado de la interacción con el usuario. Se comunica exclusivamente con el Backend.
* **Backend** (Java / Spring Boot), responsable de la lógica de negocio, la persistencia en PostgreSQL, la autenticación JWT, y la orquestación de la API REST expuesta al Frontend.
* **Microservicio de Ciencia de Datos** (Python / FastAPI), donde se ejecutan los modelos de clasificación de gastos y de perfil financiero, empaquetado en un contenedor Docker independiente.
Oracle Cloud Infrastructure (OCI Object Storage) se integra como repositorio de los artefactos del modelo entrenado, permitiendo desacoplar su almacenamiento del entorno de ejecución local.
 
```
Frontend (Vue) ⇄ Backend (Java/Spring Boot) ⇄ Microservicio ML (Python/FastAPI)
                          ⇓
                    PostgreSQL
```
 
---
 
 
# 📂 Estructura del proyecto
 
```
G9-LATAM-Team-35-FinanceAI/
│
├── backend/                 # API REST (Java 21 + Spring Boot)
│   └── src/main/java/com/team35/backend/
│       ├── controller/
│       ├── service/
│       ├── entity/
│       ├── repository/
│       ├── dto/
│       └── config/
│
├── frontend/                 # Interfaz de usuario (Vue 3 + Vite)
│   └── src/
│       ├── views/
│       ├── components/
│       ├── services/
│       └── router/
│
├── data-science/             # Modelos de ML y microservicio (Python + FastAPI)
│   ├── data/
│   ├── models/
│   ├── notebooks/
│   └── src/
│
└── README.md
```
 
---
 
# ⚙️ Instalación y ejecución local
 
El proyecto requiere levantar 3 servicios en paralelo. Se recomienda usar Docker para el microservicio de Machine Learning; Backend y Frontend pueden ejecutarse de forma nativa.
 
### Requisitos previos
 
* Java 21
* Node.js (para Vue/Vite)
* PostgreSQL 17
* Python 3.10+ (o Docker, alternativamente)
### 1. Backend (Java / Spring Boot)
 
```bash
# Crear la base de datos "financeai" en PostgreSQL antes de continuar
cd backend
./mvnw spring-boot:run
```
El backend queda disponible en `http://localhost:8080`. Las migraciones de Flyway se aplican automáticamente al iniciar.
 
### 2. Microservicio de Machine Learning (Python / FastAPI)
 
**Con Docker (recomendado):**
```bash
cd data-science
docker build -t financeai-wrapper .
docker run -p 8000:8000 financeai-wrapper
```
 
**Sin Docker:**
```bash
cd data-science
python -m venv venv
.\venv\Scripts\activate       # En Linux/Mac: source venv/bin/activate
pip install -r requirements.txt
cd src
uvicorn prediction_wrapper:app --reload
```
El microservicio queda disponible en `http://localhost:8000` (documentación interactiva en `/docs`).
 
### 3. Frontend (Vue)
 
```bash
cd frontend
npm install
npm run dev
```
El frontend queda disponible en `http://localhost:5173`.
 
---
 
# 💻 Ejemplo de uso
 
### Endpoint
 
```http
POST /analisis-financiero
Authorization: Bearer <token>   (opcional — con token se persiste el análisis, sin token se procesa en modo invitado)
```
 
### Solicitud
 
```json
{
  "ingresoMensual": 4500,
  "nivelEndeudamiento": 25,
  "frecuenciaAhorro": "Media",
  "transacciones": [
    {
      "descripcion": "Supermercado",
      "valor": 420
    },
    {
      "descripcion": "Combustible",
      "valor": 300
    },
    {
      "descripcion": "Streaming",
      "valor": 40
    }
  ]
}
```
 
### Respuesta
 
```json
{
  "perfilFinanciero": "En observación",
  "probabilidad": 0.82,
  "resumenGastos": {
    "alimentacion": 420,
    "transporte": 300,
    "entretenimiento": 40
  },
  "recomendaciones": [
    "Monitorear gastos recurrentes de entretenimiento.",
    "Aumentar la reserva financiera mensual."
  ]
}
```
 
---
 
# 📄 Documentación
 
La documentación técnica completa del proyecto (arquitectura detallada, roles, flujos de usuario, requerimientos funcionales y no funcionales, modelos de datos y seguridad) está disponible en Notion:
 
👉 [Documentación completa — FinanceAI](https://app.notion.com/p/PROYECTO-Finance-AI-Asistente-Inteligente-de-Salud-Financiera-39f68a293c108045b4fdd7a277e821b2)
 
La documentación interactiva de la API REST (Swagger UI) está disponible ejecutando el backend localmente en:
 
👉 `http://localhost:8080/swagger-ui.html`
 
---
 
# 🗺️ Roadmap
 
Funcionalidades evaluadas y dejadas fuera del alcance del MVP actual, como posibles mejoras futuras:
 
* Seguimiento de presupuesto por categoría (comparativo gastado / presupuestado).
* Recuperación de contraseña.
* Conversión real entre monedas (actualmente el soporte de moneda es solo visual/informativo).
* Activación completa de la integración con OCI Object Storage para la carga de modelos en producción.
---

 # 📜 Licencia
 
Proyecto académico desarrollado en el marco del Hackathon Oracle Next Education (ONE). 
# 📜 Licencia
 
Proyecto académico desarrollado en el marco del Hackathon Oracle Next Education (ONE). Sin fines comerciales.
