# Configuración de Render - Puerto 465 (SSL) para Zoho Mail

## 🔧 Problema: Timeout en Puerto 587

Si estás experimentando timeouts al conectarte a `smtp.zoho.com:587` en Render, es probable que el puerto 587 esté bloqueado o tenga restricciones de red.

**Solución**: Usar el puerto **465 con SSL directo** en lugar de 587 con STARTTLS.

---

## 📋 Variables de Entorno para Render (Puerto 465)

Configura estas variables en Render para usar SSL directo:

### Variables Requeridas:

1. **`MAIL_HOST`**
   - Valor: `smtp.zoho.com`

2. **`MAIL_PORT`**
   - Valor: `465` ⚠️ **Cambiar de 587 a 465**

3. **`MAIL_USERNAME`**
   - Valor: `albertinovillar@ncod3.com`

4. **`MAIL_PASSWORD`**
   - Valor: `Miux07MmNZUn` (App Password de Zoho)

### Variables Adicionales para Puerto 465:

5. **`MAIL_SSL_ENABLE`**
   - Valor: `true` ⚠️ **Nueva variable - habilitar SSL**

6. **`MAIL_STARTTLS_ENABLE`**
   - Valor: `false` ⚠️ **Nueva variable - deshabilitar STARTTLS**

7. **`MAIL_STARTTLS_REQUIRED`**
   - Valor: `false` ⚠️ **Nueva variable - STARTTLS no requerido**

8. **`MAIL_SOCKET_PORT`**
   - Valor: `465` ⚠️ **Nueva variable - puerto para socketFactory**

9. **`MAIL_SOCKET_CLASS`**
   - Valor: `javax.net.ssl.SSLSocketFactory` ⚠️ **Nueva variable - clase SSL**

---

## 🚀 Pasos para Configurar en Render

### Paso 1: Actualizar Variables Existentes

1. Ve a Render Dashboard → Tu Servicio → Environment
2. Actualiza `MAIL_PORT` de `587` a `465`

### Paso 2: Agregar Nuevas Variables

Agrega estas nuevas variables de entorno:

#### Variable: `MAIL_SSL_ENABLE`
- Key: `MAIL_SSL_ENABLE`
- Value: `true`
- Guardar

#### Variable: `MAIL_STARTTLS_ENABLE`
- Key: `MAIL_STARTTLS_ENABLE`
- Value: `false`
- Guardar

#### Variable: `MAIL_STARTTLS_REQUIRED`
- Key: `MAIL_STARTTLS_REQUIRED`
- Value: `false`
- Guardar

#### Variable: `MAIL_SOCKET_PORT`
- Key: `MAIL_SOCKET_PORT`
- Value: `465`
- Guardar

#### Variable: `MAIL_SOCKET_CLASS`
- Key: `MAIL_SOCKET_CLASS`
- Value: `javax.net.ssl.SSLSocketFactory`
- Guardar

---

## ✅ Resumen de Variables Finales en Render

Después de configurar, deberías tener:

```
MAIL_HOST=smtp.zoho.com
MAIL_PORT=465
MAIL_USERNAME=albertinovillar@ncod3.com
MAIL_PASSWORD=Miux07MmNZUn
MAIL_SSL_ENABLE=true
MAIL_STARTTLS_ENABLE=false
MAIL_STARTTLS_REQUIRED=false
MAIL_SOCKET_PORT=465
MAIL_SOCKET_CLASS=javax.net.ssl.SSLSocketFactory
```

---

## 🔄 Configuración Local vs Render

### Local (Desarrollo):
- Puerto: `587` (STARTTLS)
- SSL: `false`
- STARTTLS: `true`

### Render (Producción):
- Puerto: `465` (SSL directo)
- SSL: `true`
- STARTTLS: `false`

La aplicación detectará automáticamente qué configuración usar según las variables de entorno.

---

## ⚠️ Notas Importantes

1. **Render reiniciará automáticamente** después de cambiar las variables
2. **Verifica los logs** después del reinicio
3. **Prueba el envío de correo** para confirmar que funciona
4. **El puerto 465 es más confiable** en servicios de hosting como Render

---

## 🧪 Verificación

Después de configurar:

1. Revisa los logs de Render para confirmar que no hay errores de conexión
2. Prueba el formulario de "Solicitar Demo"
3. Prueba el "Olvidar Contraseña"
4. Verifica que los correos se envíen correctamente

---

## 🔍 Si Aún No Funciona

Si después de configurar el puerto 465 aún tienes problemas:

1. **Habilita el debug** temporalmente:
   - Agrega variable: `MAIL_DEBUG=true` (o cambia en código)
   - Revisa los logs detallados

2. **Verifica la App Password**:
   - Asegúrate de que la App Password sea correcta
   - Genera una nueva si es necesario

3. **Contacta soporte de Render**:
   - Puede haber restricciones de red específicas
   - Verifica si hay firewalls bloqueando conexiones salientes

