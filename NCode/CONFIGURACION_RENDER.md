# Configuración de Render para Mejoras de Seguridad

## 🔐 Variables de Entorno Requeridas en Render

Cuando despliegues a Render, necesitas configurar las siguientes variables de entorno en el panel de Render:

### Variables Obligatorias

1. **`MAIL_PASSWORD`** (NUEVA - Crítica)
   - **Valor**: Tu contraseña de email
   - **Por qué**: Ya no está hardcodeada en el código por seguridad
   - **Dónde configurar**: Render Dashboard → Tu Servicio → Environment → Add Environment Variable

2. **`SESSION_SECURE`** (NUEVA - Recomendada)
   - **Valor**: `true`
   - **Por qué**: Habilita cookies seguras (requiere HTTPS)
   - **Nota**: Render proporciona HTTPS automáticamente, así que usa `true`

### Variables Existentes (Verificar que estén configuradas)

3. **`PORT`**
   - **Valor**: Render lo asigna automáticamente
   - **Nota**: No necesitas configurarla manualmente

4. **`SERVER_CONTEXT_PATH`**
   - **Valor**: `/NCod3`
   - **Por qué**: Define el path base de tu aplicación

5. **`DATABASE_URL`**
   - **Valor**: Tu URL de conexión a PostgreSQL
   - **Formato**: `jdbc:postgresql://host:port/database`

6. **`DATABASE_USERNAME`**
   - **Valor**: Usuario de la base de datos

7. **`DATABASE_PASSWORD`**
   - **Valor**: Contraseña de la base de datos

8. **`MAIL_HOST`** (Opcional)
   - **Valor**: `mail.indigo-negocios.com` (o tu servidor SMTP)
   - **Nota**: Si no se configura, usa el valor por defecto

9. **`MAIL_PORT`** (Opcional)
   - **Valor**: `465` (o tu puerto SMTP)
   - **Nota**: Si no se configura, usa el valor por defecto

10. **`MAIL_USERNAME`** (Opcional)
    - **Valor**: `ncodeactive@indigo-negocios.com` (o tu email)
    - **Nota**: Si no se configura, usa el valor por defecto

---

## 📋 Pasos para Configurar en Render

### Paso 1: Acceder a Variables de Entorno

1. Ve a tu dashboard de Render: https://dashboard.render.com
2. Selecciona tu **Web Service**
3. En el menú lateral, haz clic en **Environment**
4. Verás la lista de variables de entorno actuales

### Paso 2: Agregar Variables Nuevas

#### Variable: `MAIL_PASSWORD`
1. Haz clic en **Add Environment Variable**
2. **Key**: `MAIL_PASSWORD`
3. **Value**: Tu contraseña de email (la que estaba en application.properties)
4. Haz clic en **Save Changes**

#### Variable: `SESSION_SECURE`
1. Haz clic en **Add Environment Variable**
2. **Key**: `SESSION_SECURE`
3. **Value**: `true`
4. Haz clic en **Save Changes**

### Paso 3: Verificar Variables Existentes

Asegúrate de que estas variables estén configuradas correctamente:

- ✅ `SERVER_CONTEXT_PATH` = `/NCod3` (actualizado del cambio anterior)
- ✅ `DATABASE_URL` = Tu URL de PostgreSQL
- ✅ `DATABASE_USERNAME` = Tu usuario
- ✅ `DATABASE_PASSWORD` = Tu contraseña

---

## 🔒 Configuración de HTTPS (Automática en Render)

**¡Buenas noticias!** Render proporciona HTTPS automáticamente:

1. **SSL/TLS**: Render maneja los certificados SSL automáticamente
2. **HTTPS Redirect**: Render redirige HTTP → HTTPS automáticamente
3. **HSTS Header**: Ya está configurado en tu código (se activará con HTTPS)

**No necesitas configurar nada adicional** para HTTPS en Render.

---

## 🛡️ Headers de Seguridad (Ya Configurados)

Los headers de seguridad que implementamos se aplicarán automáticamente:

- ✅ `X-Content-Type-Options: nosniff`
- ✅ `X-Frame-Options: DENY`
- ✅ `X-XSS-Protection: 1; mode=block`
- ✅ `Strict-Transport-Security` (se activará con HTTPS)
- ✅ `Referrer-Policy`

**No necesitas configurar nada adicional** en Render para estos headers.

---

## ⚙️ Configuración Adicional Recomendada

### 1. Health Check Endpoint (Opcional pero Recomendado)

Render puede verificar la salud de tu aplicación. Puedes agregar un endpoint simple:

```java
@GetMapping("/health")
@ResponseBody
public Map<String, String> health() {
    return Map.of("status", "UP");
}
```

Luego en Render:
- Ve a **Settings** → **Health Check Path**
- Configura: `/NCod3/health`

### 2. Auto-Deploy Settings

Verifica que esté configurado:
- **Auto-Deploy**: `Yes` (para desplegar automáticamente desde GitHub)
- **Branch**: `master` (o tu rama principal)

### 3. Build & Start Commands

Verifica en **Settings**:
- **Build Command**: (dejar vacío si usas Dockerfile, o `mvn clean package -DskipTests`)
- **Start Command**: (dejar vacío, Render detectará el JAR automáticamente)

---

## 📝 Checklist Pre-Despliegue

Antes de desplegar, verifica:

- [ ] Variable `MAIL_PASSWORD` configurada en Render
- [ ] Variable `SESSION_SECURE` configurada como `true` en Render
- [ ] Variable `SERVER_CONTEXT_PATH` actualizada a `/NCod3` en Render
- [ ] Variables de base de datos configuradas correctamente
- [ ] Código desplegado en GitHub (con los cambios de seguridad)
- [ ] Auto-deploy habilitado en Render
- [ ] Build exitoso en Render (revisa los logs)

---

## 🧪 Verificación Post-Despliegue

Después de desplegar, verifica:

### 1. Headers de Seguridad
1. Visita tu aplicación en Render
2. `F12` → Network → Headers
3. Verifica que los headers de seguridad estén presentes

### 2. HTTPS Funcionando
1. Intenta acceder con `http://` (sin 's')
2. Debería redirigir automáticamente a `https://`

### 3. Cookies Seguras
1. `F12` → Application → Cookies
2. Verifica que `JSESSIONID` tenga:
   - ✅ `Secure: true` (ahora con HTTPS)
   - ✅ `HttpOnly: true`
   - ✅ `SameSite: Strict`

### 4. Funcionalidad
1. Prueba el login
2. Prueba el formulario de contacto
3. Verifica que el rate limiting funcione
4. Verifica que las rutas protegidas requieran autenticación

---

## 🚨 Problemas Comunes y Soluciones

### Problema 1: Error "MAIL_PASSWORD not found"
**Solución**: Agrega la variable `MAIL_PASSWORD` en Render Environment

### Problema 2: Cookies no son seguras
**Solución**: 
- Verifica que `SESSION_SECURE=true` esté configurado
- Asegúrate de que estés accediendo por HTTPS (no HTTP)

### Problema 3: Headers de seguridad no aparecen
**Solución**: 
- Verifica que el código se haya desplegado correctamente
- Revisa los logs de Render para errores
- Asegúrate de que Spring Security esté cargando correctamente

### Problema 4: Rate limiting no funciona entre reinicios
**Solución**: 
- Esto es normal, el rate limiting actual es en memoria
- Para producción, considera usar Redis (opcional, no crítico)

---

## 📊 Resumen de Configuración

### Variables de Entorno Mínimas Requeridas:
```
MAIL_PASSWORD=tu_contraseña_email
SESSION_SECURE=true
SERVER_CONTEXT_PATH=/NCod3
DATABASE_URL=jdbc:postgresql://...
DATABASE_USERNAME=tu_usuario
DATABASE_PASSWORD=tu_contraseña
```

### Configuraciones Automáticas (No requieren acción):
- ✅ HTTPS/SSL
- ✅ Headers de seguridad
- ✅ Protección CSRF
- ✅ Validación de entrada
- ✅ Rate limiting

---

## 🔗 Enlaces Útiles

- **Render Dashboard**: https://dashboard.render.com
- **Render Docs**: https://render.com/docs
- **Verificar Headers**: https://securityheaders.com/

---

## 💡 Notas Importantes

1. **Nunca commitees contraseñas** al repositorio
2. **Usa siempre variables de entorno** para datos sensibles
3. **Revisa los logs** después del despliegue
4. **Prueba todas las funcionalidades** después de desplegar
5. **Mantén las variables de entorno actualizadas** si cambias credenciales

