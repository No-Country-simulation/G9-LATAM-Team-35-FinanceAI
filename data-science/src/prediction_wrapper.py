from fastapi import FastAPI
from pydantic import BaseModel
from pathlib import Path
import joblib, re, unicodedata
import pandas as pd

app = FastAPI(title="FinanceAI - Microservicio de clasificación y perfil financiero")

modelo_perfil_financiero = joblib.load(
    Path("../models/modelo_perfil_financiero.joblib")
)
modelo_clasificador_gastos = joblib.load(Path("../models/clasificador_gastos.joblib"))

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
    descripcion: str
    valor: float


class ClasificacionResponse(BaseModel):
    descripcion: str
    valor: float
    categoria_gasto: str


@app.post("/clasificar-transaccion", response_model=list[ClasificacionResponse])
def clasificar_transaccion(transacciones: list[Transaccion]):
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
        if pct_top >= 30:
            recomendaciones.append(
                f"Alerta: Tu mayor rubro de gasto es '{categoria_top}' con ${monto_top:,.2f}, "
                f"equivalente al {pct_top:.1f}% de las transacciones que registraste en este análisis."
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
    ingreso_mensual: float
    nivel_endeudamiento: float
    frecuencia_ahorro: str
    transacciones: list[Transaccion]


class PerfilResponse(BaseModel):
    perfil_financiero: str
    probabilidad: float
    resumen_gastos: dict
    recomendaciones: list[str]


@app.post("/analisis-financiero", response_model=PerfilResponse)
def analisis_financiero(request: PerfilRequest):
    textos_limpios = [limpiar_texto(t.descripcion) for t in request.transacciones]
    categorias = modelo_clasificador_gastos.predict(textos_limpios)

    resumen_gastos: dict[str, float] = {}
    for transaccion, categoria in zip(request.transacciones, categorias):
        resumen_gastos[categoria] = resumen_gastos.get(categoria, 0) + transaccion.valor

    resumen_gastos = dict(
        sorted(resumen_gastos.items(), key=lambda item: item[1], reverse=True)
    )

    gasto_total = sum(resumen_gastos.values())
    ratio_gasto_ingreso = gasto_total / request.ingreso_mensual

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
