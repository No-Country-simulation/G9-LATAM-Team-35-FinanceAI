
# 🚀 FinanceAI – Asistente Inteligente de Salud Financiera

![Estado del Proyecto](https://img.shields.io/badge/Estado-En_Planificación-blue)
![Versión](https://img.shields.io/badge/Versión-1.0.0-green)
![Hackathon](https://img.shields.io/badge/Hackathon-ONE-orange)
![Oracle](https://img.shields.io/badge/Oracle-OCI-red)
![Java](https://img.shields.io/badge/Java-21-blue)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.x-brightgreen)

---


# 📋 Índice

* Estado del proyecto
* Descripción del proyecto
* Objetivos
* Sector empresarial
* Tecnologías
* Arquitectura
* Ejemplo de uso
* Equipo

---
# 🚧 Estado del proyecto

Actualmente el proyecto se encuentra en fase de planificación y diseño de arquitectura. La implementación se desarrollará durante el Hackathon ONE.

---
# 📖 Descripción del proyecto

**FinanceAI** es una solución inteligente orientada a mejorar la salud financiera de los usuarios mediante el análisis automático de sus transacciones y hábitos financieros.

A partir de la información proporcionada por el usuario, el sistema será capaz de analizar su comportamiento financiero y generar información útil que facilite una mejor toma de decisiones.

Entre la información procesada se encuentran:

* Ingreso mensual.
* Nivel de endeudamiento.
* Frecuencia de ahorro.
* Historial de transacciones.
* Descripción y monto de cada gasto.

---

# 🎯 Objetivos

El proyecto busca desarrollar un MVP capaz de:

* Clasificar automáticamente las transacciones financieras.
* Identificar patrones de consumo.
* Analizar el perfil financiero del usuario.
* Generar recomendaciones personalizadas.
* Exponer los resultados mediante una API REST.
* Integrar al menos un servicio de Oracle Cloud Infrastructure (OCI).

---

# 🏢 Sector Empresarial

**Fintech · Educación Financiera · Carteras Digitales**

FinanceAI está dirigido a personas que desean comprender mejor sus hábitos financieros, organizar sus gastos y tomar decisiones más informadas sobre el manejo de su dinero.

---

# 🛠️ Tecnologías

Actualmente el proyecto contempla el uso de las siguientes tecnologías:

### Backend

* Java 21
* Spring Boot
* Spring Data JPA
* Maven
* Flyway
* Lombok
* Swagger / OpenAPI

### Ciencia de Datos

* Python
* Pandas
* Scikit-Learn
* Jupyter Notebook

### Frontend

* Vue.js

### Infraestructura

La infraestructura del proyecto se encuentra actualmente en definición. Durante el desarrollo del hackathon se seleccionarán los servicios de Oracle Cloud Infrastructure (OCI) que mejor se adapten a las necesidades del proyecto.

---

# 🏗️ Arquitectura

La solución estará organizada en cuatro módulos principales:

* **Frontend**, encargado de la interacción con el usuario.
* **Backend**, responsable de la lógica de negocio y la API REST
* **Ciencia de Datos**, donde se desarrollarán y entrenarán los modelos de clasificación y análisis financiero.
* **Oracle Cloud Infrastructure (OCI)**, utilizado para el almacenamiento, procesamiento o despliegue de la solución.

La arquitectura podrá evolucionar conforme avance el desarrollo del proyecto.

---

# 💻 Ejemplo de uso

### Endpoint

```http
POST /api/analisis-financiero
```

### Solicitud

```json
{
  "ingreso_mensual": 4500,
  "nivel_endeudamiento": 25,
  "frecuencia_ahorro": "Media",
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
  "perfil_financiero": "En observación",
  "probabilidad": 0.82,
  "resumen_gastos": {
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


# 👥 Equipo

Proyecto desarrollado por el equipo ** G9-LATAM-Team 35 FinanceAI** durante el Hackathon Oracle Next Education (ONE).
