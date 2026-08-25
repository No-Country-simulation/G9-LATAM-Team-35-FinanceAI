from fastapi import FastAPI, Request, Body
from fastapi.responses import JSONResponse
from pydantic import BaseModel, Field
from pathlib import Path
import joblib, re, unicodedata
import pandas as pd
from fastapi.middleware.cors import CORSMiddleware
import os
from oci_service import OCIService

"""
Ancla las rutas a la ubicación de este archivo, 
no al directorio desde donde se ejecuta el proceso 
(necesario porque Docker corre uvicorn)
"""
BASE_DIR = Path(__file__).resolve().parent
MODELS_DIR = BASE_DIR / ".." / "models"


app = FastAPI(title="FinanceAI - Microservicio de clasificación y perfil financiero")
backend_url = os.getenv("BACKEND_URL", "http://localhost:8080")

app.add_middleware(
    CORSMiddleware,
    allow_origins=[
        "http://localhost:8080",      # Backend local
        "http://localhost:8000",      # FastAPI local (para pruebas)
        backend_url,                  # Backend en producción
    ],
    allow_credentials=True,
    allow_methods=["GET", "POST", "PUT", "OPTIONS"],
    allow_headers=["*"],
)

@app.exception_handler(Exception)
async def manejador_global(request: Request, exc: Exception):
    """
    Red de seguridad para errores no anticipados
    (no reemplaza la validación de Pydantic, que ya
    rechaza inputs invalidos antes de llegar aqui)
    """
    return JSONResponse(
        status_code=500,
        content={"detail": "Ocurrió un error inesperado procesando la solicitud."},
    )


# Comentar si no se usara con alguna key para validar el OCI
oci_service = OCIService(bucket_name="finance-ai-bucket")

"""Modelos en local"""
#modelo_perfil_financiero = joblib.load(MODELS_DIR / "modelo_perfil_financiero.joblib")
#modelo_clasificador_gastos = joblib.load(MODELS_DIR / "clasificador_gastos.joblib")

"""Modelo OCI en RAM"""
modelo_perfil_financiero = oci_service.cargar_modelo_joblib(
    "modelo_perfil_financiero.joblib"
)
modelo_clasificador_gastos = oci_service.cargar_modelo_joblib(
    "clasificador_gastos.joblib"
)
print("✅ Modelos cargados desde OCI correctamente")

"""Modelo OCI en cache (carpeta temp)"""
# modelo_perfil_financiero = oci_service.cargar_modelo_con_cache(
#     "modelo_perfil_financiero.joblib"
# )
# modelo_clasificador_gastos = oci_service.cargar_modelo_con_cache(
#     "clasificador_gastos.joblib"
# )


def limpiar_texto(texto):
    if not texto:
        return ""
    texto = str(texto).lower()
    texto = (
        unicodedata.normalize("NFKD", texto).encode("ASCII", "ignore").decode("utf-8")
    )
    texto = re.sub(r"[^a-z0-9\s]", " ", texto)
    return re.sub(r"\s+", " ", texto).strip()


# Clasificación de gastos


class Transaccion(BaseModel):
    descripcion: str = Field(..., min_length=1)
    valor: float = Field(..., gt=0)


class ClasificacionResponse(BaseModel):
    descripcion: str
    valor: float
    categoria_gasto: str

@app.get("/")
def root():
    return {
        "service": "FinanceAI - Microservicio de clasificación",
        "status": "online",
        "endpoints": {
            "/health": "Estado del servicio",
            "/clasificar-transaccion": "Clasificar una o más transacciones",
            "/analisis-financiero": "Análisis completo de perfil financiero"
        },
        "docs": "/docs"
    }

@app.get("/health")
def health_check():
    return {
        "status": "ok",
        "cors_origins": os.getenv("BACKEND_URL")
    }

@app.post("/clasificar-transaccion", response_model=list[ClasificacionResponse])
def clasificar_transaccion(transacciones: list[Transaccion] = Body(..., min_length=1)):
    textos_limpios = [limpiar_texto(t.descripcion) for t in transacciones]
    categorias = modelo_clasificador_gastos.predict(textos_limpios)

    return [
        ClasificacionResponse(
            descripcion=t.descripcion,
            valor=t.valor,
            categoria_gasto=categoria,
        )
        for t, categoria in zip(transacciones, categorias)
    ]


# Perfil financiero


def identificar_patron_consumo(indicadores):
    """
    Identifica el patrón de consumo predominante del usuario
    a partir de la categoría principal de gasto.
    """

    categoria = indicadores["Categoría principal"].lower()

    if "ocio" in categoria:
        return "Consumidor recreativo"

    elif "alimentación" in categoria:
        return "Consumo cotidiano"

    elif "vivienda" in categoria:
        return "Alta carga de gastos fijos"

    elif "transporte" in categoria:
        return "Alta movilidad"

    elif "salud" in categoria:
        return "Prioriza bienestar"

    elif "educación" in categoria:
        return "Inversión en desarrollo"

    elif "servicios" in categoria:
        return "Gasto operativo"

    return "Consumo equilibrado"


def generar_indicadores(
    ingreso_mensual: float,
    resumen_gastos: dict,
    nivel_endeudamiento: float,
):
    """
    Calcula los principales indicadores financieros
    utilizados por el sistema.
    """

    indicadores = {}

    # ==========================
    # Liquidez
    # ==========================

    gasto_total = sum(resumen_gastos.values())

    ahorro = ingreso_mensual - gasto_total

    capacidad_ahorro = (ahorro / ingreso_mensual) * 100 if ingreso_mensual > 0 else 0

    ratio_gasto = (gasto_total / ingreso_mensual) * 100 if ingreso_mensual > 0 else 0

    # ==========================
    # Patrón de consumo
    # ==========================

    categoria_principal = max(resumen_gastos, key=resumen_gastos.get)

    gasto_principal = resumen_gastos[categoria_principal]

    concentracion_gasto = gasto_principal / gasto_total * 100 if gasto_total > 0 else 0

    # ==========================
    # Diccionario final
    # ==========================

    indicadores["Ingreso mensual"] = ingreso_mensual
    indicadores["Gasto total"] = gasto_total
    indicadores["Ahorro estimado"] = ahorro
    indicadores["Capacidad de ahorro (%)"] = capacidad_ahorro
    indicadores["Ratio gasto/ingreso (%)"] = ratio_gasto
    indicadores["Nivel de endeudamiento (%)"] = nivel_endeudamiento
    indicadores["Categoría principal"] = categoria_principal
    indicadores["Concentración del gasto (%)"] = concentracion_gasto
    indicadores["Patrón de consumo"] = identificar_patron_consumo(indicadores)

    return indicadores


def generar_alertas(indicadores):
    """
    Genera alertas financieras a partir de los indicadores
    calculados previamente.
    """

    alertas = []

    # ===============================
    # Liquidez
    # ===============================

    ratio = indicadores["Ratio gasto/ingreso (%)"]

    if ratio >= 90:
        alertas.append(
            {
                "Tipo": "Liquidez",
                "Nivel": "Crítico",
                "Mensaje": "Tus gastos representan más del 90% de tus ingresos.",
            }
        )

    elif ratio >= 70:
        alertas.append(
            {
                "Tipo": "Liquidez",
                "Nivel": "Advertencia",
                "Mensaje": "Tus gastos representan más del 70% de tus ingresos.",
            }
        )

    # ===============================
    # Ahorro
    # ===============================

    ahorro = indicadores["Capacidad de ahorro (%)"]

    if ahorro < 0:
        alertas.append(
            {
                "Tipo": "Ahorro",
                "Nivel": "Crítico",
                "Mensaje": "Tus gastos superan tus ingresos mensuales.",
            }
        )

    elif ahorro < 10:
        alertas.append(
            {
                "Tipo": "Ahorro",
                "Nivel": "Advertencia",
                "Mensaje": "Tu capacidad de ahorro es menor al 10%.",
            }
        )

    # ===============================
    # Endeudamiento
    # ===============================

    deuda = indicadores["Nivel de endeudamiento (%)"]

    if deuda >= 80:
        alertas.append(
            {
                "Tipo": "Endeudamiento",
                "Nivel": "Crítico",
                "Mensaje": "El nivel de endeudamiento es muy elevado.",
            }
        )

    elif deuda >= 60:
        alertas.append(
            {
                "Tipo": "Endeudamiento",
                "Nivel": "Advertencia",
                "Mensaje": "El nivel de endeudamiento es alto.",
            }
        )

    # ===============================
    # Concentración del gasto
    # ===============================

    concentracion = indicadores["Concentración del gasto (%)"]

    if concentracion >= 40:

        alertas.append(
            {
                "Tipo": "Patrón de consumo",
                "Nivel": "Información",
                "Mensaje": (
                    f"El {concentracion:.1f}% de tus gastos "
                    f"se concentra en '{indicadores['Categoría principal']}'."
                ),
            }
        )

    return alertas


def generar_recomendaciones(perfil, indicadores, alertas):
    """
    Genera recomendaciones financieras personalizadas a partir del
    perfil financiero, los indicadores calculados y las alertas detectadas.
    """

    recomendaciones = []

    # ==================================================
    # Recomendaciones generales según el perfil financiero
    # ==================================================

    perfil = perfil.lower()

    if perfil == "en riesgo":

        recomendaciones.append(
            "Prioriza la reducción de gastos no esenciales para recuperar estabilidad financiera."
        )

        recomendaciones.append(
            "Evita adquirir nuevas deudas hasta mejorar tu capacidad de ahorro."
        )

        recomendaciones.append(
            "Construye un fondo de emergencia equivalente a por lo menos tres meses de gastos."
        )

    elif perfil == "en observación":

        recomendaciones.append(
            "Mantén un seguimiento mensual de tus gastos para evitar deteriorar tu situación financiera."
        )

        recomendaciones.append(
            "Procura destinar al menos el 10% de tus ingresos al ahorro."
        )

    else:

        recomendaciones.append("Mantén tus hábitos financieros actuales.")

        recomendaciones.append(
            "Considera diversificar parte de tu ahorro mediante instrumentos de inversión de bajo riesgo."
        )

    # ==================================================
    # Recomendaciones según alertas
    # ==================================================

    tipos_alerta = [a["Tipo"] for a in alertas]

    if "Liquidez" in tipos_alerta:

        recomendaciones.append(
            "Reduce temporalmente los gastos variables hasta recuperar un margen de ahorro saludable."
        )

    if "Ahorro" in tipos_alerta:

        recomendaciones.append(
            "Establece una meta de ahorro automática al inicio de cada mes."
        )

    if "Endeudamiento" in tipos_alerta:

        recomendaciones.append(
            "Prioriza el pago de las deudas con mayor tasa de interés."
        )

    # ==================================================
    # Recomendaciones según patrón de consumo
    # ==================================================

    patron = indicadores["Patrón de consumo"]

    if patron == "Consumidor recreativo":

        recomendaciones.append(
            "Define un presupuesto mensual para actividades recreativas y entretenimiento."
        )

    elif patron == "Consumo cotidiano":

        recomendaciones.append(
            "Planifica las compras del hogar para reducir gastos impulsivos."
        )

    elif patron == "Alta carga de gastos fijos":

        recomendaciones.append(
            "Revisa periódicamente los costos asociados a vivienda y servicios para identificar oportunidades de ahorro."
        )

    elif patron == "Alta movilidad":

        recomendaciones.append(
            "Evalúa alternativas de transporte que reduzcan el costo de tus desplazamientos."
        )

    elif patron == "Prioriza bienestar":

        recomendaciones.append(
            "Mantén el equilibrio entre el cuidado de la salud y el resto de tus objetivos financieros."
        )

    elif patron == "Inversión en desarrollo":

        recomendaciones.append(
            "Continúa invirtiendo en educación procurando mantener un presupuesto equilibrado."
        )

    elif patron == "Gasto operativo":

        recomendaciones.append(
            "Revisa periódicamente tus suscripciones y servicios contratados."
        )

    # ==================================================
    # Recomendaciones específicas según indicadores
    # ==================================================

    if indicadores["Capacidad de ahorro (%)"] >= 20:

        recomendaciones.append(
            "Tu capacidad de ahorro es alta; podrías considerar invertir parte del excedente para alcanzar objetivos financieros de largo plazo."
        )

    elif indicadores["Capacidad de ahorro (%)"] < 10:

        recomendaciones.append(
            "Incrementar tu capacidad de ahorro debería ser una prioridad durante los próximos meses."
        )

    if indicadores["Nivel de endeudamiento (%)"] >= 60:

        recomendaciones.append(
            "Evita utilizar nuevas líneas de crédito mientras reduces tu nivel de endeudamiento."
        )

    return recomendaciones


class PerfilRequest(BaseModel):
    ingreso_mensual: float = Field(..., ge=0)
    nivel_endeudamiento: float = Field(..., ge=0, le=100)
    frecuencia_ahorro: str
    transacciones: list[Transaccion] = Field(..., min_length=1)


class PerfilResponse(BaseModel):
    perfil_financiero: str
    probabilidad: float
    resumen_gastos: dict
    recomendaciones: list[str]


@app.post("/analisis-financiero", response_model=PerfilResponse)
def analisis_financiero(request: PerfilRequest):
    # Clasificar todas las transacciones
    textos_limpios = [limpiar_texto(t.descripcion) for t in request.transacciones]
    categorias = modelo_clasificador_gastos.predict(textos_limpios)

    # Acumular gasto por categoría
    resumen_gastos: dict[str, float] = {}
    for transaccion, categoria in zip(request.transacciones, categorias):
        resumen_gastos[categoria] = resumen_gastos.get(categoria, 0) + transaccion.valor

    # Ordenar de mayor a menor gasto
    resumen_gastos = dict(
        sorted(resumen_gastos.items(), key=lambda item: item[1], reverse=True)
    )

    # Ratio gasto/ingreso
    gasto_total = sum(resumen_gastos.values())
    ratio_gasto_ingreso = gasto_total / request.ingreso_mensual

    # Armar fila para el modelo de perfil
    fila_usuario = pd.DataFrame(
        [
            {
                "ingreso_mensual": request.ingreso_mensual,
                "nivel_endeudamiento": request.nivel_endeudamiento,
                "ratio_gasto_ingreso": ratio_gasto_ingreso,
                "frecuencia_ahorro": request.frecuencia_ahorro,
            }
        ]
    )

    perfil = modelo_perfil_financiero.predict(fila_usuario)[0]
    probabilidad = modelo_perfil_financiero.predict_proba(fila_usuario).max()

    indicadores = generar_indicadores(
        request.ingreso_mensual, resumen_gastos, request.nivel_endeudamiento
    )
    alertas = generar_alertas(indicadores)
    recomendaciones = generar_recomendaciones(perfil, indicadores, alertas)

    return PerfilResponse(
        perfil_financiero=perfil,
        probabilidad=round(float(probabilidad), 2),
        resumen_gastos=resumen_gastos,
        recomendaciones=recomendaciones,
    )