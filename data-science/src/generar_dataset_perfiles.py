import numpy as np
import pandas as pd
from pathlib import Path

np.random.seed(42)  # Tener los mismos resultados

n_samples = 400  # Cambiar para modificar el tamaño del dataset

# Definición de rangos a usar
ingreso_min, ingreso_max = 9582, 120000
rango_gasto_min, rango_gasto_max = 0.30, 1.10

rango_nivel_endeudamiento = np.random.uniform(0, 100, n_samples)

ingreso_mensual = np.random.uniform(ingreso_min, ingreso_max, n_samples)
porcentaje_gasto = np.random.uniform(rango_gasto_min, rango_gasto_max, n_samples)
gasto_total = ingreso_mensual * porcentaje_gasto

rango_frecuencia_ahorro = np.random.choice(
    [
        "Baja",
        "Media",
        "Alta",
    ],
    size=n_samples,
    p=[0.4, 0.4, 0.2],
)

# Etiquetas del perfil "Saludable/ En Observación / En Riesgo"
condicion_riesgo = (
    (porcentaje_gasto > 0.90)
    | (rango_nivel_endeudamiento > 70)
    | ((rango_nivel_endeudamiento > 50) & (rango_frecuencia_ahorro == "Baja"))
)

condicion_saludable = (
    (porcentaje_gasto <= 0.70)
    & (rango_nivel_endeudamiento <= 40)
    & ((rango_frecuencia_ahorro != "Baja") | (porcentaje_gasto <= 0.50))
)

condiciones = [condicion_riesgo, condicion_saludable]
opciones = ["En Riesgo", "Saludable"]

perfil_financiero = np.select(condiciones, opciones, "En Observación")

# Ruido: cambiar aleatoriamente el 8% de las etiquetas a una clase distinta
porcentaje_ruido = 0.08
n_ruido = int(n_samples * porcentaje_ruido)

indices_ruido = np.random.choice(n_samples, size=n_ruido, replace=False)

opciones_perfil = ["Saludable", "En Observación", "En Riesgo"]

for idx in indices_ruido:
    etiqueta_actual = perfil_financiero[idx]
    otras_opciones = [op for op in opciones_perfil if op != etiqueta_actual]
    perfil_financiero[idx] = np.random.choice(otras_opciones)

# Visualizar los resultados en consola
# for i in range(n_samples):
#     print(f"Cliente {i+1}:")
#     print(f"  - Ingreso Mensual: ${ingreso_mensual[i]:,.2f}")
#     print(f"  - Proporción de Gasto: {porcentaje_gasto[i]*100:.1f}%")
#     print(f"  - Gasto Total: ${gasto_total[i]:,.2f}")
#     print(f"  - Nivel Endeudamiento: {rango_nivel_endeudamiento[i]:.1f}%")
#     print(f"  - Frecuencia Ahorro:   {rango_frecuencia_ahorro[i]}")
#     print(f"  - PERFIL ASIGNADO:    {perfil_financiero[i]}\n")

# Estructura del csv en formato diccionario
datos = {
    "Ingreso_Mensual": np.round(ingreso_mensual, 2),
    "Proporcion_Gasto": np.round(porcentaje_gasto * 100, 2),
    "Gasto_Total": np.round(gasto_total, 2),
    "Nivel_Endeudamiento": np.round(rango_nivel_endeudamiento, 2),
    "Frecuencia_Ahorro": rango_frecuencia_ahorro,
    "Perfil_Financiero": perfil_financiero,
}

df = pd.DataFrame(datos)

# Guardar los resultados en csv
output_dir = Path("data-science/data/raw")
output_dir.mkdir(parents=True, exist_ok=True)
output_path = output_dir / "perfiles_simulados.csv"

df.to_csv(output_path, sep=",", encoding="utf-8", index=False)
