# FinanceAl - Frontend

Frontend de **FinanceAl**, un asistente inteligente de salud financiera
que permite a los usuarios registrar sus transacciones, analizar sus
hábitos financieros y obtener recomendaciones personalizadas.

## Tecnologías y herramientas

- Vue 3
- Vite
- JavaScript
- Tailwind CSS
- Axios
- Phosphor Icons
- Vue Router

## Funcionalidades

El frontend permite:

- Crear una cuenta e iniciar sesión.
- Registrar y consultar transacciones.
- Clasificar transacciones mediante inteligencia artificial.
- Consultar ingresos y gastos por período.
- Visualizar indicadores de salud financiera.
- Consultar el nivel de endeudamiento.
- Determinar la frecuencia de ahorro.
- Realizar análisis financiero.
- Consultar recomendaciones personalizadas.
- Consultar el historial de análisis realizados.

## Vistas de la aplicación

### Inicio de sesión y registro
Permite al usuario crear una cuenta o iniciar sesión para acceder
a las funcionalidades de FinanceAl.
<img width="1365" height="566" alt="Vista de inicio de sesión y registro de FinanceAl" src="https://github.com/user-attachments/assets/7d3d91c7-ed67-435c-89b2-6d77b54461bb" />

### Dashboard

Es la vista principal de la aplicación. Permite consultar de manera
general la situación financiera del usuario y acceder a las principales
funcionalidades de análisis.

Desde esta vista se pueden consultar indicadores financieros, visualizar
información del período seleccionado y acceder al análisis de salud
financiera.

<img width="1365" height="583" alt="Vista del dashboard" src="https://github.com/user-attachments/assets/4ff68912-9ae4-4ecf-8fac-b6f13ddf6330" />

### Transacciones
Permite registrar, consultar, editar y eliminar las transacciones
financieras del usuario.

Al registrar o editar una transacción de tipo **gasto**, el usuario puede
utilizar la funcionalidad de **clasificación mediante Inteligencia
Artificial**, que analiza la descripción de la transacción y propone
automáticamente una categoría de gasto.

Las transacciones de tipo ingreso se gestionan de manera independiente
y pueden utilizarse posteriormente para calcular indicadores financieros
como el ingreso mensual.

<img width="1343" height="588" alt="Vista del crud de transacciones" src="https://github.com/user-attachments/assets/dd4e24c6-76f9-4b58-9a12-73cc24b2cf2d" /> 

<img width="1342" height="593" alt="Vista de la edicción de una transaccion" src="https://github.com/user-attachments/assets/cbbd823a-8445-404a-900d-acdcfcf8d226" />


### Análisis financiero

Permite realizar un análisis de la situación financiera del usuario y
obtener un perfil de salud financiera acompañado de recomendaciones.

El análisis considera diferentes indicadores, entre ellos:

- **Ingreso mensual:** puede obtenerse a partir de las transacciones de
  ingreso registradas para el período seleccionado o utilizar información
  previamente registrada.
- **Nivel de endeudamiento:** puede calcularse a partir de las cuotas
  mensuales de deuda y el ingreso mensual.
- **Frecuencia de ahorro:** puede determinarse automáticamente mediante
  el análisis de las transacciones registradas durante un período de
  seis meses o mediante una encuesta cuando no existen suficientes datos
  transaccionales.
- **Perfil de salud financiera:** clasifica la situación financiera del
  usuario en diferentes niveles y proporciona recomendaciones de acuerdo
  con los resultados obtenidos.

<img width="1346" height="590" alt="Vista de los datos usados para el analisis financiero" src="https://github.com/user-attachments/assets/b32d8747-2c5f-4ab5-9441-40cbd87caeb2" /> 

<img width="1347" height="594" alt="Vista del analisis financiero obtenido" src="https://github.com/user-attachments/assets/96668326-f7ff-47cf-894d-a87331064392" />


### Historial de análisis

Permite consultar los análisis financieros realizados anteriormente,
facilitando al usuario el seguimiento de sus resultados a lo largo del
tiempo.

El usuario puede revisar los resultados obtenidos en diferentes períodos
y consultar nuevamente la información de sus análisis anteriores.

<img width="1347" height="597" alt="Vista del historial de análisis" src="https://github.com/user-attachments/assets/2012c050-7795-445c-81e2-413dfaafe5b1" />

### Configuración de usuario

Permite gestionar la información y preferencias de la cuenta del usuario.

Desde esta sección se pueden consultar y actualizar los datos personales
de la cuenta y gestionar las opciones disponibles de configuración.

<img width="1360" height="585" alt="Vista de la configuración de la cuenta del usuario" src="https://github.com/user-attachments/assets/c0a33ab3-8a99-4c1b-b1d2-9dddfbd8bc98" /> 

### Análisis financiero como invitado

FinanceAl permite realizar un análisis financiero sin necesidad de crear
una cuenta. Esta modalidad está pensada para que los usuarios puedan
conocer de manera rápida su nivel de salud financiera antes de registrarse
en la plataforma.

El usuario proporciona la información financiera solicitada y obtiene un
resultado con su perfil de salud financiera, indicadores y recomendaciones
personalizadas.

<img width="1346" height="596" alt="Vista del analisis financiero como invitado" src="https://github.com/user-attachments/assets/c414849f-8f49-4d1b-950e-431dd7b67137" />


# Manual de Instalación y Configuración del Frontend

## Instalación

### 1. Clonar el repositorio

```bash
git clone https://github.com/No-Country-simulation/G9-LATAM-Team-35-FinanceAI.git
cd frontend
```

### 2. Instalar dependencias
Instala las dependencias necesarias para ejecutar el proyecto:

```bash
npm install
```

## Configuración

### Variables de entorno

Crea un archivo `.env` con (entorno local):

```bash
VITE_API_URL=http://localhost:8080
```
La variable VITE_API_URL indica la URL base del backend al que se conectará
el frontend. Para producción, debe configurarse con la URL pública correspondiente.

> ⚠️ **Importante:** No almacenar credenciales sensibles en el frontend.

## Estructura del proyecto

```
frontend/
├── src/
│   ├── components/
│   ├── views/
│   └── services/
└── package.json
```
## Ejecutar en desarrollo

Inicia el servidor de desarrollo con:
```bash
npm run dev
```
Vite mostrará en la terminal la dirección local donde estará disponible
la aplicación.

## Compilar/Ejecutar  para producción
El proyecto puede prepararse para producción de dos formas: mediante
la compilación directa con Node/Vite o utilizando Docker.

### Opción 1. Compilar para producción

Para generar la versión optimizada para producción:
```bash
npm run build
```

Los archivos generados estarán disponibles en la carpeta:

`dist/`

Para realizar una previsualización local de la versión compilada:
```bash
npm run preview
```
>npm run build únicamente genera los archivos de producción. Para
>servir la aplicación se requiere un servidor web o utilizar
>npm run preview para realizar una prueba local.

### Opción 2. Ejecutar mediante Docker

El proyecto incluye un Dockerfile que automatiza la preparación de la
aplicación para producción.

El proceso incluye la instalación de las dependencias, la compilación
del proyecto mediante npm run build y la preparación de los archivos
generados para ser servidos mediante el servidor web configurado en
el contenedor.

#### 2.1 Construir la imagen

Desde la carpeta raíz del proyecto:

```bash
docker build --no-cache -t frontend-image ./frontend
```

Para ejecutar el contenedor:
```bash
docker run -p 8080:80 frontend-image
```

#### 2.2 Ejecutar el proyecto completo mediante Docker Compose

El proyecto también incluye una configuración de Docker Compose para
levantar los diferentes servicios que conforman FinanceAl.

La configuración incluye:

- **PostgreSQL:** base de datos de la aplicación. 
- **Backend:**  API desarrollada con Spring Boot. 
- **Data Science:**  servicio desarrollado con Python/FastAPI. 
- **Frontend:**  aplicación Vue.js servida mediante Nginx. 

Desde la carpeta raíz del proyecto, ejecuta:

```bash
docker compose up --build
```


## Comandos disponibles

| Comando                                     | Descripción                             |
| ------------------------------------------- | --------------------------------------- |
| `npm install`                               | Instala las dependencias del frontend   |
| `npm run dev`                               | Inicia el servidor de desarrollo        |
| `npm run build`                             | Compila el frontend para producción     |
| `npm run preview`                           | Previsualiza la versión compilada       |
| `docker build -t frontend-image ./frontend` | Construye la imagen Docker del frontend |
| `docker run -p 8080:80 frontend-image`      | Ejecuta el contenedor del frontend      |
| `docker compose up --build`                 | Construye y levanta todos los servicios |
| `docker compose down`                       | Detiene los servicios de Docker Compose |

## Solución de problemas

1. **Error de conexión**
   - Verifica que el backend esté corriendo
   - Revisa la variable `VITE_API_URL`

2. **Error de compilación**
   ```bash
   npm install --force
   ```
