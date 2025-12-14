# Dockerización de NCode

Este proyecto está dockerizado y listo para desplegarse en Render.

## Archivos Docker

- `Dockerfile`: Imagen multi-etapa para construir y ejecutar la aplicación
- `.dockerignore`: Archivos excluidos del contexto de Docker
- `docker-compose.yml`: Configuración para ejecutar localmente con MySQL

## Construcción Local

### Opción 1: Usando Docker directamente

```bash
# Construir la imagen
docker build -t ncode-app .

# Ejecutar el contenedor
docker run -p 8080:8080 \
  -e DATABASE_URL=jdbc:mysql://host.docker.internal:3306/ncodegestion \
  -e DATABASE_USERNAME=root \
  -e DATABASE_PASSWORD=admin. \
  ncode-app
```

### Opción 2: Usando Docker Compose

```bash
# Construir y ejecutar todo (aplicación + MySQL)
docker-compose up --build

# Ejecutar en segundo plano
docker-compose up -d

# Ver logs
docker-compose logs -f

# Detener
docker-compose down
```

## Despliegue en Render

### Configuración en Render

1. **Crear un nuevo Web Service** en Render
2. **Conectar tu repositorio** (GitHub, GitLab, etc.)
3. **Configuración del servicio:**
   - **Build Command**: `mvn clean package -DskipTests` (o dejar vacío si usas Dockerfile)
   - **Start Command**: (dejar vacío, Render detectará el Dockerfile automáticamente)
   - **Dockerfile Path**: `NCode/Dockerfile` (si el Dockerfile está en la raíz del proyecto)

### Variables de Entorno en Render

Configura las siguientes variables de entorno en el panel de Render:

```
PORT=8080
SERVER_CONTEXT_PATH=/NCod3
DATABASE_URL=jdbc:mysql://tu-host-mysql:3306/ncodegestion?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
DATABASE_USERNAME=tu_usuario
DATABASE_PASSWORD=tu_contraseña
```

### Base de Datos MySQL en Render

1. **Crear un MySQL Database** en Render
2. **Obtener la conexión interna** (para usar dentro de Render) o externa
3. **Configurar las variables de entorno** con la URL de conexión proporcionada

**Nota**: Si usas la base de datos interna de Render, usa la conexión interna (hostname interno). Si usas una base de datos externa, usa la conexión externa.

### Ejemplo de URL de conexión para Render

Si Render te proporciona una conexión como:
```
mysql://usuario:password@dpg-xxxxx-a.oregon-postgres.render.com/ncodegestion
```

Conviértela a formato JDBC:
```
jdbc:mysql://dpg-xxxxx-a.oregon-postgres.render.com:3306/ncodegestion?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
```

## Variables de Entorno Disponibles

- `PORT`: Puerto donde corre la aplicación (por defecto: 8080)
- `SERVER_CONTEXT_PATH`: Ruta del contexto (por defecto: /NCod3)
- `DATABASE_URL`: URL completa de conexión a MySQL
- `DATABASE_USERNAME`: Usuario de la base de datos
- `DATABASE_PASSWORD`: Contraseña de la base de datos
- `JAVA_OPTS`: Opciones JVM (por defecto: -Xmx512m -Xms256m)

## Verificación

Una vez desplegado, accede a:
- `https://tu-app.onrender.com/NCod3/` - Página principal
- `https://tu-app.onrender.com/NCod3/login` - Login
- `https://tu-app.onrender.com/NCod3/planes` - Planes

## Troubleshooting

### La aplicación no inicia
- Verifica que todas las variables de entorno estén configuradas
- Revisa los logs en Render: `Logs` en el panel de tu servicio

### Error de conexión a la base de datos
- Verifica que la URL de conexión sea correcta
- Asegúrate de usar la conexión interna si la BD está en Render
- Verifica que el usuario y contraseña sean correctos

### Puerto no disponible
- Render asigna automáticamente el puerto, asegúrate de usar `${PORT}` en la configuración
- La aplicación ya está configurada para usar la variable `PORT` automáticamente

