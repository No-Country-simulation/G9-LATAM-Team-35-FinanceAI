# Módulo de Data Science — FinanceAI

Este módulo contiene el desarrollo, entrenamiento, evaluación y serialización de los modelos de Machine Learning encargados de clasificar transacciones financieras, diagnosticar el perfil de los usuarios y generar recomendaciones dentro del proyecto **FinanceAI**.

## Contrato de Datos (API Contracts)

Alineado directamente con el equipo de **Back-End** para los endpoints expuestos en Spring Boot:

### 1. Análisis Financiero (Entrada Múltiple / Estado de Cuenta)

- **Endpoint:** `POST /analisis-financiero`
- **Petición (Request):**
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
      }
    ]
  }
  ```
- **Respuesta (Response):**
  ```json
  {
    "perfil_financiero": "En observación",
    "probabilidad": 0.82,
    "resumen_gastos": {
      "alimentacion": 420,
      "transporte": 300
    },
    "recomendaciones": [
      "Monitorear gastos recurrentes de entretenimiento.",
      "Aumentar la reserva financiera mensual."
    ]
  }
  ```

### 2. Clasificación de Gasto Individual

- **Endpoint:** `POST /clasificar-transaccion`
- **Petición (Request):**
  ```json
  {
    "descripcion": "Uber",
    "valor": 45.00
  }
  ```
- **Respuesta (Response):**
  ```json
  {
    "descripcion": "Uber",
    "valor": 45.00,
    "categoria_gasto": "Transporte"
  }
  ```

## Integración con Oracle Cloud Infrastructure (OCI)

Para cumplir con la arquitectura en la nube de la solución:

1. **Artefactos del Modelo (.joblib):** Todos los artefactos entrenados (clasificador, vectorizador TF-IDF, escaladores y encoders) se almacenan localmente en `models/` únicamente para pruebas de desarrollo.
2. **Entorno de Producción:** Se suben al bucket de **OCI Object Storage** (`finance-ai-bucket`).
3. **Consumo Backend:** La API de Java/Spring Boot o el microservicio de Python descarga los modelos directamente desde OCI Object Storage al iniciar la aplicación para ejecutar las inferencias.

# Fases del proyecto

## Fase 0 — Diseño del Esquema de Datos

Antes de escribir código en Python, definiremos la matriz de consistencia:

- **Categorías de Gasto Obligatorias**: Alimentación, Transporte, Salud, Vivienda, Educación, Ocio y Servicios.
- **Diccionario de Conceptos**: Una lista de mínimo 20 comercios/textos reales de México (O más lugares) por categoría (ej. Transporte → Uber, DiDi, gasolinera, Metrobus, caseta).
- **Variables Macro del Perfil**: `ingreso_mensual `(numérico), `frecuencia_ahorro `(categórico: Baja/Media/Alta) y `nivel_endeudamiento `(numérico: % de 0 a 100).
- **Reglas de Perfil**: Definir la lógica exacta de negocio para etiquetar las clases oficiales: Saludable, En observación y En riesgo.

## Fase 1 — Generación de Datasets Simulados

Construiremos dos conjuntos de datos independientes para entrenar los dos modelos requeridos:

- **Dataset A (Clasificación de Gastos)**: Mínimo 300 filas que asocien descripciones textuales con su categoría financiera. Usaremos variaciones como mayúsculas, minúsculas, acentos y prefijos bancarios (ej. COMPRA EN UBER) mediante combinación programática. Cuidaremos el balance para evitar sesgos en el modelo.
- **Dataset B (Análisis de Perfil)**: Mínimo 400 filas con perfiles financieros simulados utilizando rangos económicos realistas de México. Aplicaremos las reglas de la Fase 0 para asignar las etiquetas de perfil (Saludable/En observación/En riesgo), inyectando un pequeño porcentaje de ruido aleatorio para evitar sobreajuste (overfitting).

## Fase 2 — Análisis Exploratorio de Datos (EDA)

Validación estadística y gráfica de los datos simulados:

- **Limpieza básica**: asegurar ausencia de nulos, duplicados no deseados y tipos de datos correctos (`int`, `float`, `str`).

- **Visualización con `Seaborn`/`Matplotlib`** de la distribución de categorías de gastos y montos para confirmar coherencia lógica (ej. que los montos de Vivienda sean proporcionalmente mayores a los de Ocio).

- **Análisis de correlación y frecuencia de las variables macro** respecto al perfil financiero asignado.

## Fase 3 — Procesamiento de Variables Textuales

Preparación del Dataset A para el procesamiento de lenguaje natural (NLP):

- **Limpieza de texto**: estandarización a minúsculas, eliminación de signos de puntuación, caracteres especiales y acentos.

- **Vectorización**: Implementación de la técnica **TF-IDF** (`TfidfVectorizer `de scikit-learn) para transformar las cadenas de texto en matrices numéricas inteligibles por el modelo.

## Fase 4 — Ingeniería de Atributos (Feature Engineering)

Optimización del Dataset B antes del modelado:

- **Creación de variables derivadas críticas**, como el `ratio_gasto_ingreso `(gasto acumulado total dividido entre el ingreso mensual).

- **Transformación de variables categóricas** Codificación ordinal o One-Hot Encoding aplicada específicamente a `frecuencia_ahorro`.

- **Escalamiento de características numéricas** con `StandardScaler` sobre `ingreso_mensual` y `nivel_endeudamiento` para asegurar estabilidad en los algoritmos de clasificación.

## Fase 5 — Modelo 1: Clasificación de Gastos

División de datos en entrenamiento y prueba (**Train/Test Split de 80/20**).

- Entrenamiento de algoritmos óptimos para procesamiento de texto corto: **Naive Bayes** y **Regresión Logística**.

- **Selección del modelo con el mejor desempeño** para predecir la categoría del gasto basándose en la descripción.

## Fase 6 — Modelo 2: Análisis del Perfil Financiero

- **División **del Dataset B (**80/20**).

- Entrenamiento de modelos de clasificación multiclase: **Árboles de Decisión** (para garantizar explicabilidad) o **Random Forest**.

- **El modelo final debe ser capaz de recibir las variables financieras del usuario y clasificarlo correctamente en uno de los tres perfiles oficiales**.

## Fase 7 — Evaluación y Lógica de Negocio

- **Generación de métricas de rendimiento obligatorias**: **Accuracy**, **Matriz de Confusión** y un reporte completo de **Precision**, **Recall** y **F1-Score**.

- **Implementación de Alertas de Gastos Elevados**: Desarrollo de la lógica de negocio complementaria para activar banderas de advertencia si una transacción individual o una categoría supera un umbral crítico relativo a los ingresos del usuario.

## Fase 8 — Serialización de Modelos

- **Exportación de los artefactos entrenados** utilizando la librería `joblib` o `pickle`.
- **Es obligatorio guardar**: el Modelo de Clasificación de Gastos, el Modelo de Perfil Financiero, el Vectorizador TF-IDF y los Encoders/Scalers utilizados en el preprocesamiento, garantizando que el Back-End pueda replicar exactamente el pipeline de datos.

## Fase 9 — Pipeline de Predicción (Wrapper)

- **Desarrollo de funciones de Python unificadas que tomen las peticiones JSON provenientes del Back-End (soportando arreglos de 1 o N transacciones), carguen los archivos serializados, clasifiquen cada gasto, calculen el resumen agrupado, predigan el perfil global y devuelvan la respuesta estructurada exactamente en el contrato acordado.**

# Estructura del proyecto

```sh
data-science/
│
├── README.md  # llenarlo con el plan de fases
│
├── notebooks/
│   ├── 01_yul_clasificacion_gastos.ipynb # Dataset A, fases 1,2,3
│   ├── 02_marco_perfil_financiero.ipynb  # Dataset B, fases 1,2,4
│   ├── 03_luz_bi_analysis.ipynb          # fases 5,6,7
│   └── 00_notebook_final.ipynb           # El que se arma juntando todo al final
│
├── data/
│   ├── raw/
│   │   ├── transacciones_simuladas.csv # Dataset A
│   │   └── perfiles_simulados.csv # Dataset B
│   └── processed/
│       ├── transacciones_procesadas.csv       # Después de limpieza/EDA
│       └── perfiles_procesados.csv
│
├── models/ # Temporal en lo que se usa OCI Object Storage
│   ├── clasificador_gastos.joblib/pkl # Modelo serializado (Fase 8)
│   ├── vectorizador_tfidf.joblib/pkl # Vectorizador TF-IDF (Fase 8)
│   ├── modelo_perfil_financiero.joblib/pkl # Modelo serializado (Fase 8)
│   ├── encoder_frecuencia_ahorro.joblib/pkl # Encoder (Fase 8)
│   └── scaler_variables_numericas.joblib/pkl # Scaler (Fase 8)
│
├── src/
│   ├── generar_dataset_transacciones.py # Script reutilizable (opcional)
│   ├── generar_dataset_perfiles.py # Script reutilizable (opcional)
│   └── prediction_wrapper.py # Fase 9 — función unificada que consume backend
│
└── requirements.txt # pandas, scikit-learn, seaborn, joblib/pickle, etc.
```

## Instalación y Entorno Local

1. **Crear y activar entorno virtual:**

   ```bash
   python -m venv venv
   # En Windows:
   .\venv\Scripts\activate
   # En Linux/Mac:
   source venv/bin/activate
   ```

2. **Instalar dependencias requeridas:**

   ```bash
   cd .\data-science\ pip install -r requirements.txt
   ```

3. **Ejecución:**
   Abrir los notebooks en orden numérico (`01`, `02`, `03`) o simplemente usar el completo (`00`) dentro de Jupyter / VS Code para reproducir el entrenamiento y evaluación de los modelos.
