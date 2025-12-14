# Cómo Verificar las Mejoras de Seguridad

## 🔍 Verificaciones Visuales y Funcionales

### 1. **Headers de Seguridad HTTP** ✅

**Cómo verificar:**
1. Abre tu aplicación en el navegador (Chrome, Firefox, Edge)
2. Presiona `F12` para abrir las herramientas de desarrollador
3. Ve a la pestaña **Network** (Red)
4. Recarga la página (`F5`)
5. Selecciona cualquier solicitud (por ejemplo, la página principal)
6. Ve a la pestaña **Headers** (Encabezados)
7. Busca en **Response Headers** (Encabezados de respuesta):

Deberías ver:
- ✅ `X-Content-Type-Options: nosniff`
- ✅ `X-Frame-Options: DENY`
- ✅ `X-XSS-Protection: 1; mode=block`
- ✅ `Strict-Transport-Security: max-age=31536000; includeSubDomains`
- ✅ `Referrer-Policy: strict-origin-when-cross-origin`

**Herramienta alternativa:**
- Visita: https://securityheaders.com/
- Ingresa tu URL y verifica la calificación

---

### 2. **Protección CSRF** ✅

**Cómo verificar:**
1. Abre cualquier formulario (login, contacto)
2. Presiona `F12` → **Elements** (Elementos)
3. Busca el formulario en el HTML
4. Deberías ver un campo oculto:
   ```html
   <input type="hidden" name="_csrf" value="...">
   ```

**Prueba funcional:**
- Intenta enviar un formulario sin el token CSRF (deshabilitando JavaScript o modificando el HTML)
- Debería rechazar la solicitud con error 403

---

### 3. **Validación de Entrada** ✅

**Cómo verificar en Login:**
1. Ve a `/login`
2. Intenta enviar el formulario vacío
3. Deberías ver mensajes de error:
   - "El email es requerido"
   - "La contraseña es requerida"
4. Intenta con un email inválido (ej: "test")
5. Deberías ver: "El email debe tener un formato válido"
6. Los campos con error deberían tener borde rojo

**Cómo verificar en Contacto:**
1. Ve a `/contacto`
2. Intenta enviar con campos vacíos o inválidos
3. Deberías ver mensajes de error específicos para cada campo
4. Intenta con un mensaje muy largo (>2000 caracteres)
5. Debería mostrar error de validación

---

### 4. **Rate Limiting en Login** ✅

**Cómo verificar:**
1. Ve a `/login`
2. Intenta hacer login con credenciales incorrectas **5 veces seguidas**
3. En el **6to intento**, deberías ver:
   - Mensaje: "Demasiados intentos fallidos. Intenta de nuevo en X minutos."
4. Espera 15 minutos o reinicia la aplicación para resetear el contador

**Prueba avanzada:**
- Abre la consola del navegador (`F12` → Console)
- Observa los logs del servidor (si tienes acceso)
- Deberías ver mensajes de advertencia sobre intentos bloqueados

---

### 5. **Sesiones Seguras** ✅

**Cómo verificar:**
1. Abre las herramientas de desarrollador (`F12`)
2. Ve a **Application** (Aplicación) → **Cookies**
3. Busca la cookie de sesión (generalmente `JSESSIONID`)
4. Verifica que tenga:
   - ✅ **HttpOnly**: true (no accesible desde JavaScript)
   - ✅ **Secure**: false en desarrollo local, true en producción con HTTPS
   - ✅ **SameSite**: Strict

**Prueba funcional:**
- Intenta acceder a `/gestion/usuarios` sin estar autenticado
- Debería redirigirte a `/login` o mostrar error 403

---

### 6. **Protección de Rutas** ✅

**Cómo verificar:**
1. **Sin autenticación:**
   - Intenta acceder directamente a: `http://localhost:8080/NCod3/gestion/usuarios`
   - Debería redirigirte o mostrar error de acceso denegado

2. **Con autenticación:**
   - Haz login correctamente
   - Ahora deberías poder acceder a las rutas de gestión

---

### 7. **Logging de Seguridad** ✅

**Cómo verificar:**
1. Revisa los logs de la aplicación
2. Intenta hacer login con credenciales incorrectas
3. Deberías ver en los logs:
   ```
   WARN - Intento de login fallido. IP: 127.0.0.1, Email: test@example.com
   ```
4. Intenta hacer login exitoso
5. Deberías ver:
   ```
   INFO - Login exitoso. Usuario: test@example.com, IP: 127.0.0.1
   ```

---

### 8. **Regeneración de ID de Sesión** ✅

**Cómo verificar:**
1. Abre las herramientas de desarrollador
2. Ve a **Application** → **Cookies**
3. Anota el valor de `JSESSIONID` antes de hacer login
4. Haz login exitosamente
5. Verifica que el `JSESSIONID` haya cambiado (regenerado)

---

## 🧪 Pruebas de Penetración Básicas

### Prueba 1: XSS (Cross-Site Scripting)
1. En el formulario de contacto, intenta ingresar:
   ```html
   <script>alert('XSS')</script>
   ```
2. El script **NO debería ejecutarse** (Thymeleaf escapa automáticamente)
3. Deberías ver el texto literal en lugar de ejecutar el script

### Prueba 2: SQL Injection
1. En el login, intenta ingresar:
   ```
   ' OR '1'='1
   ```
2. **NO debería funcionar** (usamos JPA/PreparedStatements)
3. Debería mostrar error de validación o login fallido

### Prueba 3: CSRF Attack
1. Crea un archivo HTML malicioso en tu computadora:
   ```html
   <form action="http://localhost:8080/NCod3/login" method="post">
       <input name="username" value="admin@test.com">
       <input name="password" value="password123">
   </form>
   <script>document.forms[0].submit();</script>
   ```
2. Ábrelo en el navegador
3. **Debería fallar** porque no tiene el token CSRF

---

## 📊 Herramientas de Análisis

### 1. **OWASP ZAP (Zed Attack Proxy)**
- Descarga: https://www.zaproxy.org/
- Escanea tu aplicación automáticamente
- Detecta vulnerabilidades comunes

### 2. **Burp Suite Community**
- Descarga: https://portswigger.net/burp/communitydownload
- Intercepta y modifica peticiones HTTP
- Útil para pruebas manuales

### 3. **Security Headers Checker**
- Online: https://securityheaders.com/
- Verifica headers de seguridad
- Da una calificación de seguridad

### 4. **SSL Labs (para producción)**
- Online: https://www.ssllabs.com/ssltest/
- Verifica configuración SSL/TLS
- Útil cuando tengas HTTPS en producción

---

## ✅ Checklist de Verificación

- [ ] Headers de seguridad visibles en Network tab
- [ ] Token CSRF presente en formularios
- [ ] Validación de campos funciona correctamente
- [ ] Rate limiting bloquea después de 5 intentos
- [ ] Cookies tienen HttpOnly y SameSite
- [ ] Rutas protegidas requieren autenticación
- [ ] Logs muestran eventos de seguridad
- [ ] ID de sesión se regenera después del login
- [ ] XSS no funciona (scripts no se ejecutan)
- [ ] SQL Injection no funciona

---

## 🎯 Comparación Antes/Después

### Antes:
- ❌ Sin protección CSRF
- ❌ Sin validación de entrada
- ❌ Sin rate limiting
- ❌ Sin headers de seguridad
- ❌ Sesiones no seguras
- ❌ Rutas accesibles sin autenticación
- ❌ Sin logging de seguridad

### Después:
- ✅ Protección CSRF habilitada
- ✅ Validación completa de entrada
- ✅ Rate limiting activo (5 intentos)
- ✅ Headers de seguridad configurados
- ✅ Sesiones seguras (HttpOnly, SameSite)
- ✅ Rutas protegidas con Spring Security
- ✅ Logging de eventos de seguridad

---

## 📝 Notas Importantes

1. **En desarrollo local:**
   - `Secure` flag en cookies será `false` (normal, no hay HTTPS)
   - En producción con HTTPS, debería ser `true`

2. **Rate Limiting:**
   - Se resetea al reiniciar la aplicación
   - En producción, considera usar Redis para persistencia

3. **Logs:**
   - Revisa los logs regularmente
   - Configura alertas para múltiples intentos fallidos

4. **Testing continuo:**
   - Ejecuta estas verificaciones periódicamente
   - Después de cada cambio importante
   - Antes de cada despliegue a producción

