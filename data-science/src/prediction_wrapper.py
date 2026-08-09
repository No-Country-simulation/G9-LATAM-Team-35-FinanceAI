from fastapi import FastAPI, Request, Body
from fastapi.responses import JSONResponse
from pydantic import BaseModel, Field
from pathlib import Path
import joblib, re, unicodedata
import pandas as pd

from oci_service import OCIService

"""
Ancla las rutas a la ubicación de este archivo, 
no al directorio desde donde se ejecuta el proceso 
(necesario porque Docker corre uvicorn)
"""
BASE_DIR = Path(__file__).resolve().parent
MODELS_DIR = BASE_DIR / ".." / "models"


app = FastAPI(title="FinanceAI - Microservicio de clasificación y perfil financiero")


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
# oci_service = OCIService(bucket_name="bucket")

"""Modelos en local"""
modelo_perfil_financiero = joblib.load(MODELS_DIR / "modelo_perfil_financiero.joblib")
modelo_clasificador_gastos = joblib.load(MODELS_DIR / "clasificador_gastos.joblib")

"""Modelo OCI en RAM"""
# modelo_perfil_financiero = oci_service.cargar_modelo_joblib(
#     "modelo_perfil_financiero.joblib"
# )
# modelo_clasificador_gastos = oci_service.cargar_modelo_joblib(
#     "clasificador_gastos.joblib"
# )

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


def generar_recomendaciones(
    perfil: str, resumen: dict, ingreso_mensual: float
) -> list[str]:
    recomendaciones = []

    if resumen:
        categoria_top = max(resumen, key=resumen.get)
        monto_top = resumen[categoria_top]
        gasto_total = sum(resumen.values())
        pct_top = (monto_top / gasto_total) * 100 if gasto_total > 0 else 0
        pct_ingreso = (monto_top / ingreso_mensual) * 100 if ingreso_mensual > 0 else 0

        # Alerta dinámica basada en la categoría predominante
        if pct_top >= 30:  # Si representa el 30% o más de sus gastos totales
            recomendaciones.append(
                f"Alerta: Tu mayor rubro de gasto es '{categoria_top}' con ${monto_top:,.2f} "
                f"({pct_top:.1f}% de tus gastos totales)."
            )

            # Reglas específicas
            cat_lower = categoria_top.lower()
            if "ocio" in cat_lower or "servicios" in cat_lower:
                recomendaciones.append(
                    "Sugerencia: Evalúa cancelar suscripciones no utilizadas o fijar un tope semanal para entretenimiento y eventos."
                )
            elif "alimentación" in cat_lower:
                recomendaciones.append(
                    "Sugerencia: Planificar las compras del súper y reducir salidas a restaurantes o entregas a domicilio."
                )
            elif "transporte" in cat_lower:
                recomendaciones.append(
                    "Sugerencia: Considerar alternativas de movilidad o consolidar viajes para optimizar el gasto de gasolina/pasajes."
                )
            elif "vivienda" in cat_lower:
                recomendaciones.append(
                    "Sugerencia: Revisa tus consumos de servicios (luz, gas) o gastos de mantenimiento periódicos."
                )

        # Alerta basada en el ingreso mensual: una sola categoría absorbiendo demasiado
        if pct_ingreso >= 15:
            if pct_ingreso > 100:
                recomendaciones.append(
                    f"Atención: '{categoria_top}' por sí solo supera por completo tu ingreso "
                    f"mensual reportado (${ingreso_mensual:,.2f}) — revisa que el ingreso "
                    f"capturado sea correcto."
                )
            else:
                recomendaciones.append(
                    f"Atención: '{categoria_top}' por sí solo absorbe el {pct_ingreso:.1f}% de tu ingreso "
                    f"mensual completo (${ingreso_mensual:,.2f}), más allá de lo reportado aquí, "
                    f"vale la pena vigilar ese porcentaje."
                )

    # Recomendaciones generales basadas en el perfil de riesgo
    perfil_clean = perfil.lower()
    if perfil_clean == "en riesgo":
        recomendaciones.append(
            "Reducir gastos no esenciales de inmediato para restaurar liquidez."
        )
        recomendaciones.append("Crear un fondo de emergencia prioritario.")
    elif perfil_clean == "en observación":
        recomendaciones.append(
            "Monitorear el balance mensual para evitar pasar al perfil de riesgo."
        )
        recomendaciones.append(
            "Destinar al menos el 10% del ingreso mensual al ahorro."
        )
    else:  # Saludable
        recomendaciones.append("Mantener el hábito de ahorro e inversión actual.")
        recomendaciones.append(
            "Considerar diversificar tu excedente en instrumentos financieros a mediano plazo."
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

    recomendaciones = generar_recomendaciones(
        perfil, resumen_gastos, request.ingreso_mensual
    )

    return PerfilResponse(
        perfil_financiero=perfil,
        probabilidad=round(float(probabilidad), 2),
        resumen_gastos=resumen_gastos,
        recomendaciones=recomendaciones,
    )
