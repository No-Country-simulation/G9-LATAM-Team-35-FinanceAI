# frontend/Dockerfile
FROM node:22-slim AS build

WORKDIR /app
# Instalar dependencias del sistema para compilación
RUN apt-get update && apt-get install -y \
    python3 \
    make \
    g++ \
    && rm -rf /var/lib/apt/lists/*
# Copiar package.json y package-lock.json
COPY package*.json ./

# Instalar dependencias
RUN npm install --network-timeout=100000

#variable disponible durante el build de vite
ARG VITE_API_URL
ENV VITE_API_URL=$VITE_API_URL

# Copiar código fuente
COPY . .

# Construir para producción
RUN npm run build

# Segunda etapa: Servir con Nginx
FROM nginx:alpine

# Copiar los archivos construidos
COPY --from=build /app/dist /usr/share/nginx/html

# Puerto de Nginx
EXPOSE 80

# Comando para ejecutar Nginx
CMD ["nginx", "-g", "daemon off;"]