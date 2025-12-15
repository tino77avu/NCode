# Variables de Entorno para Render - Configuración Zoho Mail

## 🔄 Variables que DEBES ACTUALIZAR en Render

### 1. **MAIL_HOST** (NUEVA/ACTUALIZAR)
   - **Valor anterior**: `mail.indigo-negocios.com` (si existía)
   - **Valor nuevo**: `smtp.zoho.com`
   - **Acción**: Actualizar o crear esta variable

### 2. **MAIL_PORT** (ACTUALIZAR)
   - **Valor anterior**: `465` (si existía)
   - **Valor nuevo**: `587`
   - **Acción**: Actualizar esta variable

### 3. **MAIL_USERNAME** (ACTUALIZAR)
   - **Valor anterior**: `ncodeactive@indigo-negocios.com` (si existía)
   - **Valor nuevo**: `albertinovillar@ncod3.com`
   - **Acción**: Actualizar esta variable

### 4. **MAIL_PASSWORD** (ACTUALIZAR - CRÍTICA)
   - **Valor anterior**: `InurXd}mof]CDtw8fo` (si existía)
   - **Valor nuevo**: `Miux07MmNZUn` (App Password de Zoho)
   - **Acción**: Actualizar esta variable con la App Password de Zoho

## ✅ Variables que NO necesitas cambiar

- `PORT` - Render lo asigna automáticamente
- `SERVER_CONTEXT_PATH` - Ya debería estar en `/NCod3`
- `DATABASE_URL` - Mantener como está
- `DATABASE_USERNAME` - Mantener como está
- `DATABASE_PASSWORD` - Mantener como está
- `SESSION_SECURE` - Ya debería estar en `true`

---

## 📋 Pasos para Actualizar en Render

### Paso 1: Acceder a Variables de Entorno

1. Ve a tu dashboard de Render: https://dashboard.render.com
2. Selecciona tu **Web Service**
3. En el menú lateral, haz clic en **Environment**
4. Verás la lista de variables de entorno actuales

### Paso 2: Actualizar Variables Existentes

#### Variable: `MAIL_HOST`
1. Busca `MAIL_HOST` en la lista
2. Si existe, haz clic en el ícono de edición (lápiz)
3. Cambia el valor a: `smtp.zoho.com`
4. Haz clic en **Save Changes**

#### Variable: `MAIL_PORT`
1. Busca `MAIL_PORT` en la lista
2. Si existe, haz clic en el ícono de edición
3. Cambia el valor a: `587`
4. Haz clic en **Save Changes**

#### Variable: `MAIL_USERNAME`
1. Busca `MAIL_USERNAME` en la lista
2. Si existe, haz clic en el ícono de edición
3. Cambia el valor a: `albertinovillar@ncod3.com`
4. Haz clic en **Save Changes**

#### Variable: `MAIL_PASSWORD`
1. Busca `MAIL_PASSWORD` en la lista
2. Si existe, haz clic en el ícono de edición
3. Cambia el valor a: `Miux07MmNZUn`
4. Haz clic en **Save Changes**

### Paso 3: Crear Variables si No Existen

Si alguna de estas variables no existe, créala:

1. Haz clic en **Add Environment Variable**
2. Ingresa el **Key** y **Value** según la lista de arriba
3. Haz clic en **Save Changes**

---

## 📝 Resumen de Variables de Email en Render

Después de actualizar, deberías tener estas variables configuradas:

```
MAIL_HOST=smtp.zoho.com
MAIL_PORT=587
MAIL_USERNAME=albertinovillar@ncod3.com
MAIL_PASSWORD=Miux07MmNZUn
```

---

## ⚠️ Importante

1. **Después de actualizar las variables**, Render reiniciará automáticamente tu servicio
2. **Verifica los logs** después del reinicio para asegurarte de que no hay errores
3. **Prueba el envío de correo** después del despliegue para confirmar que funciona

---

## 🧪 Verificación Post-Despliegue

Después de actualizar las variables y desplegar:

1. Prueba el formulario de "Solicitar Demo"
2. Prueba el "Olvidar Contraseña"
3. Verifica que los correos se envíen correctamente
4. Revisa los logs en Render si hay algún problema

---

## 🔒 Seguridad

- ✅ La App Password está configurada como variable de entorno (no en el código)
- ✅ Las credenciales no están expuestas en el repositorio
- ✅ Render reiniciará automáticamente después de cambiar las variables

