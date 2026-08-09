import oci, io, tempfile, joblib
from pathlib import Path

BASE_DIR = Path(__file__).resolve().parent


class OCIService:

    def __init__(self, bucket_name=None, config_path=None):
        if config_path is None:
            config_path = BASE_DIR / ".." / ".config"
        self.config = oci.config.from_file(str(config_path), "DEFAULT")
        self.object_storage_client = oci.object_storage.ObjectStorageClient(self.config)
        self.namespace = self.object_storage_client.get_namespace().data
        self.bucket_name = bucket_name

    def subir_archivo(self, nombre_objeto, ruta_nube):
        with open(ruta_nube, "rb") as f:
            response = self.object_storage_client.put_object(
                namespace_name=self.namespace,
                bucket_name=self.bucket_name,
                object_name=nombre_objeto,
                put_object_body=f,
            )
        print(f"Archivo subido {response.status}")

    def descargar_archivo(self, nombre_objeto):
        output_dir = BASE_DIR / ".." / "models" / "descargados"
        output_dir.mkdir(parents=True, exist_ok=True)
        ruta_archivo = output_dir / nombre_objeto

        response = self.object_storage_client.get_object(
            namespace_name=self.namespace,
            bucket_name=self.bucket_name,
            object_name=nombre_objeto,
        )

        with open(ruta_archivo, "wb") as f:
            for chunk in response.data.raw.stream(1024 * 1024, decode_content=False):
                f.write(chunk)
        print(f"Archivo descargado en {ruta_archivo}")
        return ruta_archivo

    # Cargar en memoria
    def cargar_en_memoria(self, nombre_objeto):
        """
        Descarga el objeto y lo retorna como buffer en memoria (RAM)
        - Ideal para (Docker/Compute/Serverless)
        """
        response = self.object_storage_client.get_object(
            namespace_name=self.namespace,
            bucket_name=self.bucket_name,
            object_name=nombre_objeto,
        )

        buffer_memoria = io.BytesIO(response.data.content)
        return buffer_memoria

    def cargar_modelo_joblib(self, nombre_objeto):
        """Carga un modelo .joblib directamente en RAM sin tocar el disco"""
        buffer = self.cargar_en_memoria(nombre_objeto)
        modelo = joblib.load(buffer)
        print(f"Modelo cargado: {modelo}")
        return modelo

    # Cargar mediante cache en carpeta temporal
    def cargar_modelo_con_cache(self, nombre_objeto):
        """
        Descarga el modelo a la carpeta /tmp del sistema solo si no existe ahi
        - util en Docker/Compute para evitar re-descargar por red en cada consulta
        """
        temp_dir = Path(tempfile.gettempdir()) / "oci_models"
        temp_dir.mkdir(parents=True, exist_ok=True)

        local_filename = nombre_objeto.replace("/", "_")
        ruta_temp = temp_dir / local_filename

        if not ruta_temp.exist():
            response = self.object_storage_client.get_object(
                namespace_name=self.namespace,
                bucket_name=self.bucket_name,
                object_name=nombre_objeto,
            )

        with open(ruta_temp, "wb") as f:
            for chunk in response.data.raw.stream(1024 * 1024, decode_content=False):
                f.write(chunk)
        print(f"Archivo descargado en {ruta_temp}")
        return ruta_temp
