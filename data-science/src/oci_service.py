import oci, io, tempfile, joblib
from pathlib import Path
import os
from dotenv import load_dotenv  # 👈 Agregar

# 📌 Cargar variables desde .env (solo en desarrollo local)
load_dotenv()  # Esto cargará las variables de entorno desde el archivo .env en el directorio raíz del proyecto

BASE_DIR = Path(__file__).resolve().parent


class OCIService:

    def __init__(self, bucket_name=None, config_path=None):
        # Cargar desde variables de entorno (para Render/Railway)
        user = os.getenv("OCI_USER_OCID")
        fingerprint = os.getenv("OCI_FINGERPRINT")
        tenancy = os.getenv("OCI_TENANCY_OCID")
        region = os.getenv("OCI_REGION", "us-ashburn-1")
        key_content = os.getenv("OCI_PRIVATE_KEY")

        print("🔵 DEBUG - Credenciales OCI:")
        print(f"  - OCI_USER_OCID: {'✅ Encontrado' if user else '❌ No encontrado'}")
        print(f"  - OCI_FINGERPRINT: {'✅ Encontrado' if fingerprint else '❌ No encontrado'}")
        print(f"  - OCI_TENANCY_OCID: {'✅ Encontrado' if tenancy else '❌ No encontrado'}")
        print(f"  - OCI_PRIVATE_KEY: {'✅ Encontrado' if key_content else '❌ No encontrado'}")

        # ✅ Verificar que TODAS las credenciales existen
        if not all([user, fingerprint, tenancy, key_content]):
            raise Exception(
                "❌ Credenciales OCI incompletas. "
                "Asegúrate de que .env tenga: OCI_USER_OCID, OCI_FINGERPRINT, "
                "OCI_TENANCY_OCID, OCI_PRIVATE_KEY"
            )

        if user and fingerprint and tenancy and key_content:
            print("🔵 Credenciales cargadas desde variables de entorno")
            self.config = {
                "user": user,
                "fingerprint": fingerprint,
                "tenancy": tenancy,
                "region": region,
                "key_content": key_content,
            }
        else:
            print("🔵 Cargando credenciales desde archivo config...")
            if config_path is None:
                config_path = BASE_DIR / ".." / ".config"
            self.config = oci.config.from_file(str(config_path), "DEFAULT")

        self.object_storage_client = oci.object_storage.ObjectStorageClient(self.config)
        self.namespace = self.object_storage_client.get_namespace().data
        self.bucket_name = bucket_name
        print(f"🔵 Namespace: {self.namespace}")
        print(f"🔵 Bucket: {self.bucket_name}")

    def subir_archivo(self, nombre_objeto, ruta_local):
        with open(ruta_local, "rb") as f:
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
        print(f"Modelo cargado")
        #print(f"Modelo cargado: {modelo}")
        return modelo

    # Agregar este método a la clase OCIService
    def listar_archivos(self):
        """Lista todos los archivos en el bucket"""
        response = self.object_storage_client.list_objects(
            namespace_name=self.namespace,
            bucket_name=self.bucket_name
        )
        archivos = [obj.name for obj in response.data.objects]
        print(f"Archivos en bucket: {archivos}")
        return archivos

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

        if not ruta_temp.exists():
            response = self.object_storage_client.get_object(
                namespace_name=self.namespace,
                bucket_name=self.bucket_name,
                object_name=nombre_objeto,
            )

            with open(ruta_temp, "wb") as f:
                for chunk in response.data.raw.stream(1024 * 1024, decode_content=False):
                    f.write(chunk)
            print(f"Archivo descargado en {ruta_temp}")
        else:
            print(f"Archivo ya existe en cache: {ruta_temp}")
        return ruta_temp
