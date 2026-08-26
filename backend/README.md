
# Backend - FinanceAl

Backend de **FinanceAl**, una aplicación orientada al análisis de salud
financiera. Proporciona una API REST encargada de gestionar usuarios,
autenticación, transacciones y análisis financieros, además de comunicarse
con el servicio de Data Science para funcionalidades basadas en
Inteligencia Artificial.

## Tecnologías y herramientas

### Lenguaje y framework

- **Java 21**
- **Spring Boot**
- **Spring Web**
- **Spring Data JPA**
- **Spring Security**
- **Maven**

### Base de datos y persistencia

- **PostgreSQL**
- **Hibernate / JPA**
- **Flyway** para el control y versionado de migraciones de base de datos.

### Seguridad

- **Spring Security**
- **JWT (JSON Web Token)** para autenticación y autorización.
- Contraseñas almacenadas utilizando mecanismos de hash.

### Integración con otros servicios

- **REST API** para la comunicación con el frontend.
- Comunicación con el servicio de **Data Science** desarrollado en
  Python/FastAPI.
- Integración con servicios externos utilizados por el componente de
  Data Science.

### Herramientas de desarrollo

- IntelliJ IDEA / Visual Studio Code
- Git y GitHub
- Docker
- Docker Compose
- Swagger / OpenAPI

## Arquitectura

FinanceAl utiliza una arquitectura compuesta por diferentes servicios:

<img width="1536" height="1024" alt="image" src="https://github.com/user-attachments/assets/58ac877b-7780-4c90-97cc-f18bed444b5c" />


El backend funciona como punto central de comunicación entre el frontend,
la base de datos y el servicio de análisis de Data Science.

## Estructura del proyecto

```text
backend/
├── src/
│   ├── main/
│   │   ├── java/
|   |       └── config/
│   │      ├── controller/
│   |       ├── service/
│   |       ├── entity/
|   |       ├── enums/
│   |       ├── repository/
│   |       ├── dto/
|   |       ├── service/
|   |       └── util/
│   │   └── resources/
│   │       ├── db/
│   │       │   └── migration/     # Migraciones de Flyway
│   │       └── application.properties
│   └── test/
├── .mvn/
├── mvnw
├── mvnw.cmd
├── pom.xml
├── Dockerfile
└── README.md
```

## Requisitos previos

Para ejecutar el backend localmente se recomienda contar con:

- Java 21 o superior.
- Maven, aunque el proyecto incluye Maven Wrapper.
- PostgreSQL.
- Git.

Para ejecutar el proyecto mediante Docker:

- Docker Desktop.
- Docker Compose.

## Instalación

### 1. Clonar el repositorio

Desde la ubicación donde se desea guardar el proyecto:

```bash
git clone https://github.com/No-Country-simulation/G9-LATAM-Team-35-FinanceAI.git
cd /backend
```

### 2. Configurar la base de datos

El backend utiliza PostgreSQL.

Crea una base de datos llamada:

```text
financeai
```

Ejemplo:

```sql
CREATE DATABASE financeai;
```

Configura las credenciales correspondientes en las variables de entorno
o en la configuración de Spring Boot.

## Configuración

### Variables de entorno

El backend requiere diferentes variables para conectarse a la base de
datos, comunicarse con otros servicios y configurar la autenticación JWT.

Ejemplo para un entorno local:

```env
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/financeai
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=admin

SPRING_FLYWAY_ENABLED=true
SPRING_FLYWAY_LOCATIONS=classpath:db/migration
SPRING_FLYWAY_BASELINE_ON_MIGRATE=true
SPRING_FLYWAY_VALIDATE_ON_MIGRATE=false

PYTHON_API_URL=http://localhost:8000

JWT_SECRET=CAMBIAR_POR_UNA_CLAVE_SEGURA_LARGA
JWT_EXPIRATION=7200000
```

### Descripción de las variables

| Variable | Descripción |
|---|---|
| `SPRING_DATASOURCE_URL` | URL de conexión a PostgreSQL |
| `SPRING_DATASOURCE_USERNAME` | Usuario de PostgreSQL |
| `SPRING_DATASOURCE_PASSWORD` | Contraseña de PostgreSQL |
| `SPRING_FLYWAY_ENABLED` | Habilita las migraciones de Flyway |
| `SPRING_FLYWAY_LOCATIONS` | Ubicación de las migraciones |
| `PYTHON_API_URL` | URL del servicio de Data Science |
| `JWT_SECRET` | Clave utilizada para firmar los tokens JWT |
| `JWT_EXPIRATION` | Tiempo de expiración del token JWT en milisegundos |

> ⚠️ **Importante:** No subir contraseñas, claves JWT, credenciales de
> servicios externos ni otros secretos al repositorio. Para producción
> deben utilizarse variables de entorno o un sistema de gestión de
> secretos.

## Migraciones de base de datos

El proyecto utiliza **Flyway** para administrar la estructura de la base
de datos.

Las migraciones se encuentran en:

```text
src/main/resources/db/migration/
```

Los archivos de migración siguen la nomenclatura de Flyway:

```text
V1__descripcion.sql
V2__descripcion.sql
V3__descripcion.sql
```

Al iniciar la aplicación, Flyway verifica y ejecuta automáticamente las
migraciones pendientes.

> Se recomienda no modificar una migración que ya haya sido ejecutada.
> Para realizar cambios en la estructura de la base de datos, se debe
> crear una nueva migración.

## Ejecutar en desarrollo

### Opción 1. Utilizando Maven Wrapper

El proyecto incluye Maven Wrapper, por lo que no es necesario instalar
Maven globalmente.

En Windows:

```bash
mvnw.cmd spring-boot:run
```

En Linux o macOS:

```bash
./mvnw spring-boot:run
```

El backend estará disponible en:

```text
http://localhost:8080
```

### Opción 2. Ejecutar el proyecto desde el IDE

También es posible ejecutar la aplicación directamente desde IntelliJ
IDEA u otro IDE compatible con Spring Boot.

La clase principal de la aplicación debe ejecutarse como una aplicación
Spring Boot.

Antes de iniciar, verifica que:

1. PostgreSQL esté ejecutándose.
2. La base de datos `financeai` exista.
3. Las variables de entorno estén configuradas.
4. El servicio de Data Science esté disponible si se utilizan
   funcionalidades que dependan de él.

## API y documentación

El backend proporciona una API REST para las diferentes funcionalidades
de FinanceAl.

La documentación de la API puede consultarse mediante Swagger/OpenAPI
cuando la aplicación se encuentra ejecutándose.

Por ejemplo:

```text
http://localhost:8080/swagger-ui/index.html
```

Las rutas disponibles incluyen funcionalidades relacionadas con:

- Autenticación y usuarios.
- Registro e inicio de sesión.
- Gestión de transacciones.
- Clasificación de transacciones.
- Análisis financiero.
- Indicadores de salud financiera.
- Historial de análisis.
- Frecuencia de ahorro.
- Comunicación con el servicio de Data Science.

## Autenticación

El backend utiliza **JWT** para proteger los endpoints que requieren
autenticación.

El flujo general es:

```text
Usuario
   │
   │ Login
   ▼
Backend
   │
   │ Genera JWT
   ▼
Frontend
   │
   │ Authorization: Bearer <token>
   ▼
Backend
   │
   │ Valida token
   ▼
Endpoint protegido
```

Los endpoints públicos y protegidos se encuentran definidos en la
configuración de Spring Security.

## Comunicación con Data Science

El backend se comunica con un servicio independiente desarrollado en
Python/FastAPI.

En un entorno local:

```text
Backend
   │
   │ HTTP
   ▼
http://localhost:8000
```

Cuando los servicios se ejecutan mediante Docker Compose, la comunicación
se realiza utilizando el nombre del servicio dentro de la red Docker:

```text
http://data-service:8000
```

Esto permite que el backend solicite al servicio de Data Science las
funcionalidades relacionadas con análisis y clasificación.

## Ejecución mediante Docker

El proyecto incluye un `Dockerfile` para construir y ejecutar el backend
en un contenedor.

El `Dockerfile` utiliza una construcción multietapa:

1. Utiliza JDK 21 para compilar la aplicación.
2. Descarga las dependencias mediante Maven.
3. Compila el proyecto y genera el archivo `.jar`.
4. Utiliza una imagen JRE 21 más ligera para ejecutar la aplicación.
5. Expone el puerto `8080`.

### Construir la imagen

Desde la carpeta raíz del proyecto:

```bash
docker build --no-cache -t financeai-backend ./backend
```

### Ejecutar el contenedor

```bash
docker run -p 8080:8080 financeai-backend
```

El backend estará disponible en:

```text
http://localhost:8080
```

> Para que el backend funcione correctamente dentro del contenedor,
> también deben estar disponibles PostgreSQL y el servicio de Data
> Science, además de configurar las variables de entorno necesarias.

## Ejecución con Docker Compose

El proyecto cuenta con una configuración de Docker Compose que permite
levantar todos los servicios de FinanceAl.

Desde la carpeta raíz del proyecto:

```bash
docker compose up --build
```

Los servicios principales son:

| Servicio | Tecnología | Puerto |
|---|---|---:|
| Frontend | Vue.js + Nginx | `80` |
| Backend | Spring Boot | `8080` |
| Data Science | Python + FastAPI | `8000` |
| Base de datos | PostgreSQL | `5432` |

Para detener los servicios:

```bash
docker compose down
```

Para reconstruir las imágenes desde cero:

```bash
docker compose build --no-cache
docker compose up
```

> Docker Compose configura automáticamente la comunicación entre los
> servicios mediante la red `financeai-network`.

## Compilación manual

Para generar el archivo `.jar` sin ejecutar la aplicación:

En Windows:

```bash
mvnw.cmd package -DskipTests
```

En Linux o macOS:

```bash
./mvnw package -DskipTests
```

El archivo generado estará disponible en:

```text
target/
```

Para ejecutar directamente el `.jar`:

```bash
java -jar target/nombre-del-archivo.jar
```

## Comandos disponibles

| Comando | Descripción |
|---|---|
| `mvnw.cmd spring-boot:run` | Ejecuta el backend en Windows |
| `./mvnw spring-boot:run` | Ejecuta el backend en Linux/macOS |
| `mvnw.cmd package -DskipTests` | Compila el proyecto |
| `docker build -t financeai-backend ./backend` | Construye la imagen Docker |
| `docker run -p 8080:8080 financeai-backend` | Ejecuta el contenedor |
| `docker compose up --build` | Construye y ejecuta todos los servicios |
| `docker compose down` | Detiene los servicios |

## Solución de problemas

### 1. Error de conexión con PostgreSQL

Verifica:

- Que PostgreSQL esté ejecutándose.
- Que exista la base de datos `financeai`.
- Que el usuario y contraseña sean correctos.
- Que `SPRING_DATASOURCE_URL` utilice el puerto correcto.

Para una ejecución local:

```text
jdbc:postgresql://localhost:5432/financeai
```

Para Docker Compose:

```text
jdbc:postgresql://postgres:5432/financeai
```

> Dentro de Docker Compose no se debe utilizar `localhost` para
> comunicarse con otro contenedor. Se utiliza el nombre del servicio,
> en este caso `postgres`.

### 2. Error de conexión con Data Science

Verifica que el servicio de Data Science esté ejecutándose.

En ejecución local:

```text
PYTHON_API_URL=http://localhost:8000
```

En Docker Compose:

```text
PYTHON_API_URL=http://data-service:8000
```

### 3. Error con Flyway

Verifica que las migraciones se encuentren dentro de:

```text
src/main/resources/db/migration/
```

y que sigan la nomenclatura requerida por Flyway.

### 4. Error relacionado con JWT

Verifica que `JWT_SECRET` esté configurado correctamente y que no esté
vacío.

Ejemplo:

```env
JWT_SECRET=CAMBIAR_POR_UNA_CLAVE_SEGURA
```

En producción se recomienda utilizar una clave segura y almacenarla como
variable de entorno.

### 5. Error al construir con Maven

Primero intenta limpiar y volver a compilar:

```bash
mvnw.cmd clean package -DskipTests
```

En Linux/macOS:

```bash
./mvnw clean package -DskipTests
```

Si el problema ocurre durante Docker:

```bash
docker build --no-cache -t financeai-backend ./backend
```

## Seguridad

Para ejecutar FinanceAl de forma segura en un entorno de producción:

- No almacenar secretos directamente en el código fuente.
- Utilizar variables de entorno para credenciales.
- Utilizar una clave JWT segura y suficientemente robusta.
- No exponer innecesariamente el puerto de PostgreSQL a Internet.
- Configurar correctamente CORS.
- Mantener actualizadas las dependencias de Spring Boot y Maven.
- Utilizar HTTPS en producción.
- Mantener protegidos los endpoints que requieren autenticación.
- No utilizar credenciales de prueba en producción.

## Estado del proyecto

FinanceAl es un proyecto desarrollado como parte de una simulación de
hackathon y tiene como objetivo proporcionar herramientas para el
seguimiento y análisis de la salud financiera de los usuarios.
