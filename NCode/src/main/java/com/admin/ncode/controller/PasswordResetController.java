package com.admin.ncode.controller;

import com.admin.ncode.entity.CodigoVerificacion;
import com.admin.ncode.entity.Empresa;
import com.admin.ncode.entity.Usuario;
import com.admin.ncode.repository.CodigoVerificacionRepository;
import com.admin.ncode.repository.EmpresaRepository;
import com.admin.ncode.repository.UsuarioRepository;
import com.admin.ncode.service.EmailService;
import com.admin.ncode.util.CodigoGenerador;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Controller
public class PasswordResetController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private CodigoVerificacionRepository codigoVerificacionRepository;

    @Autowired
    private EmpresaRepository empresaRepository;
    
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private static final String CODIGO_SESION_KEY = "codigoVerificacion";
    private static final String EMAIL_SESION_KEY = "emailRecuperacion";
    private static final long CODIGO_EXPIRACION_MS = 15 * 60 * 1000; // 15 minutos
    private static final String TIMESTAMP_SESION_KEY = "codigoTimestamp";

    @GetMapping("/olvidar-contrasena")
    public String mostrarFormularioOlvidoContrasena(Model model) {
        model.addAttribute("isAuthenticated", false);
        return "olvidar-contrasena";
    }

    @PostMapping("/olvidar-contrasena")
    public String validarEmail(@RequestParam(required = false) String email, 
                               Model model, 
                               RedirectAttributes redirectAttributes,
                               HttpSession session,
                               HttpServletRequest request) {
        model.addAttribute("isAuthenticated", false);
        
        // Validar que el email esté presente
        if (email == null || email.trim().isEmpty()) {
            model.addAttribute("mensajeError", 
                "Por favor, ingresa tu correo electrónico.");
            return "olvidar-contrasena";
        }
        
        email = email.trim();
        
        // Validar que el correo exista y el usuario esté ACTIVO
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmailAndEstado(
            email, 
            "ACTIVO"
        );
        
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            
            // Generar código de verificación
            String codigo = CodigoGenerador.generarCodigo();
            
            // Guardar código y email en sesión
            session.setAttribute(CODIGO_SESION_KEY, codigo);
            session.setAttribute(EMAIL_SESION_KEY, email);
            session.setAttribute(TIMESTAMP_SESION_KEY, System.currentTimeMillis());
            
            // Obtener IP del cliente
            String ipCliente = obtenerIpCliente(request);
            
            // Obtener empresaId (usar la primera empresa activa o un valor por defecto)
            Long empresaId = obtenerEmpresaId();
            
            // Guardar registro en la tabla codigoverificacion ANTES de enviar el email
            // para asegurar que el registro se guarde incluso si falla el envío
            try {
                LocalDateTime fechaExpiracion = LocalDateTime.now().plusMinutes(15);
                codigoVerificacionRepository.insertCodigoVerificacion(
                    usuario.getUsuarioId(),
                    empresaId,
                    codigo,
                    "RESET_CLAVE",
                    "GENERADO",
                    0,
                    3, // Máximo 3 intentos
                    fechaExpiracion,
                    ipCliente
                );
            } catch (Exception dbException) {
                // Si falla el insert, loguear y mostrar error
                System.err.println("Error al guardar código de verificación en BD: " + dbException.getMessage());
                dbException.printStackTrace();
                model.addAttribute("mensajeError", 
                    "Error al procesar la solicitud. Por favor, intenta nuevamente.");
                return "olvidar-contrasena";
            }
            
            // Enviar email con el código DESPUÉS de guardar en BD
            try {
                emailService.enviarCodigoVerificacion(email, codigo);
                
                redirectAttributes.addFlashAttribute("email", email);
                redirectAttributes.addFlashAttribute("mensajeExito", "✓ Correo enviado satisfactoriamente");
                return "redirect:/cambiar-contrasena";
            } catch (Exception e) {
                // Si falla el envío, loguear el error pero el código ya está guardado
                System.err.println("Error al enviar el correo electrónico: " + e.getMessage());
                e.printStackTrace();
                model.addAttribute("mensajeError", 
                    "Error al enviar el correo electrónico. Por favor, intenta nuevamente.");
                return "olvidar-contrasena";
            }
        } else {
            // Usuario no encontrado o no está activo
            model.addAttribute("mensajeError", 
                "El correo ingresado no existe o el usuario no está activo.");
            return "olvidar-contrasena";
        }
    }
    
    private String obtenerIpCliente(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // Si hay múltiples IPs, tomar la primera
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip != null ? ip : "unknown";
    }
    
    private Long obtenerEmpresaId() {
        // Intentar obtener la primera empresa activa
        List<Empresa> empresas = empresaRepository.findAllByOrderByEmpresaIdAsc();
        if (!empresas.isEmpty()) {
            return empresas.get(0).getEmpresaId();
        }
        // Si no hay empresas, retornar 1 como valor por defecto
        return 1L;
    }

    @GetMapping("/cambiar-contrasena")
    public String mostrarFormularioCambioContrasena(Model model, HttpSession session) {
        model.addAttribute("isAuthenticated", false);
        
        // Verificar que el email esté en los atributos flash o en sesión
        String email = (String) model.asMap().get("email");
        if (email == null) {
            email = (String) session.getAttribute(EMAIL_SESION_KEY);
        }
        
        if (email == null) {
            return "redirect:/olvidar-contrasena";
        }
        
        model.addAttribute("email", email);
        return "cambiar-contrasena";
    }

    @PostMapping("/cambiar-contrasena")
    public String cambiarContrasena(@RequestParam(required = false) String email,
                                   @RequestParam(required = false) String emailBackup,
                                   @RequestParam String codigo,
                                   @RequestParam String nuevaContrasena,
                                   @RequestParam String confirmarContrasena,
                                   Model model,
                                   RedirectAttributes redirectAttributes,
                                   HttpSession session) {
        model.addAttribute("isAuthenticated", false);
        
        // Si no hay email en el request, usar el backup
        if (email == null || email.isEmpty()) {
            if (emailBackup != null && !emailBackup.isEmpty()) {
                email = emailBackup;
            } else {
                model.addAttribute("email", "");
                model.addAttribute("mensajeError", 
                    "Por favor, ingresa tu correo electrónico.");
                return "cambiar-contrasena";
            }
        }
        
        // Validar que las contraseñas coincidan
        if (!nuevaContrasena.equals(confirmarContrasena)) {
            model.addAttribute("email", email);
            model.addAttribute("codigo", codigo);
            model.addAttribute("mensajeError", "Las contraseñas no coinciden.");
            return "cambiar-contrasena";
        }
        
        // Validar longitud mínima de contraseña
        if (nuevaContrasena.length() < 6) {
            model.addAttribute("email", email);
            model.addAttribute("codigo", codigo);
            model.addAttribute("mensajeError", "La contraseña debe tener al menos 6 caracteres.");
            return "cambiar-contrasena";
        }
        
        // DEBUG: Mostrar datos ingresados
        System.out.println("=== DEBUG VALIDACIÓN CÓDIGO ===");
        System.out.println("Email ingresado: " + email);
        System.out.println("Código ingresado: " + codigo);
        
        // Paso 1: Buscar el usuario por email para obtener el usuarioId
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
        
        if (!usuarioOpt.isPresent()) {
            System.out.println("ERROR: Usuario no encontrado con email: " + email);
            model.addAttribute("email", email);
            model.addAttribute("codigo", codigo);
            model.addAttribute("mensajeError", 
                "El correo ingresado no existe.");
            return "cambiar-contrasena";
        }
        
        Usuario usuario = usuarioOpt.get();
        System.out.println("Usuario encontrado - ID: " + usuario.getUsuarioId() + ", Estado: " + usuario.getEstado());
        
        // Validar que el usuario esté activo
        if (usuario.getEstado() != Usuario.EstadoUsuario.ACTIVO) {
            System.out.println("ERROR: Usuario no está activo. Estado actual: " + usuario.getEstado());
            model.addAttribute("email", email);
            model.addAttribute("codigo", codigo);
            model.addAttribute("mensajeError", 
                "El usuario no está activo.");
            return "cambiar-contrasena";
        }
        
        // Paso 2: Buscar el código de verificación en la base de datos por usuarioId y código
        System.out.println("Buscando código en BD - UsuarioId: " + usuario.getUsuarioId() + ", Código: " + codigo);
        Optional<CodigoVerificacion> codigoVerificacionOpt = codigoVerificacionRepository.findByUsuarioIdAndCodigo(
            usuario.getUsuarioId(), 
            codigo
        );
        
        if (!codigoVerificacionOpt.isPresent()) {
            System.out.println("ERROR: Código no encontrado en BD para UsuarioId: " + usuario.getUsuarioId() + ", Código: " + codigo);
            model.addAttribute("email", email);
            model.addAttribute("codigo", codigo);
            model.addAttribute("mensajeError", 
                "El código de verificación es incorrecto o no existe para este correo.");
            return "cambiar-contrasena";
        }
        
        CodigoVerificacion codigoVerificacion = codigoVerificacionOpt.get();
        System.out.println("Código encontrado - ID: " + codigoVerificacion.getCodigoId());
        System.out.println("  - Tipo: " + codigoVerificacion.getTipo());
        System.out.println("  - Estado: " + codigoVerificacion.getEstado());
        System.out.println("  - FechaExpiracion: " + codigoVerificacion.getFechaExpiracion());
        System.out.println("  - FechaActual: " + LocalDateTime.now());
        System.out.println("  - Codigo en BD: " + codigoVerificacion.getCodigo());
        System.out.println("  - Codigo ingresado: " + codigo);
        
        // Paso 3: Validar que el tipo sea RESET_CLAVE
        if (codigoVerificacion.getTipo() != CodigoVerificacion.TipoCodigo.RESET_CLAVE) {
            System.out.println("ERROR: Tipo incorrecto. Tipo encontrado: " + codigoVerificacion.getTipo() + ", Esperado: RESET_CLAVE");
            model.addAttribute("email", email);
            model.addAttribute("codigo", codigo);
            model.addAttribute("mensajeError", 
                "El código de verificación no es válido para restablecer contraseña.");
            return "cambiar-contrasena";
        }
        
        // Paso 4: Validar que el código no haya expirado (fechaExpiracion)
        if (codigoVerificacion.getFechaExpiracion() == null) {
            System.out.println("ERROR: FechaExpiracion es NULL");
            model.addAttribute("email", email);
            model.addAttribute("codigo", codigo);
            model.addAttribute("mensajeError", 
                "El código ha expirado. Por favor, solicita un nuevo código.");
            return "cambiar-contrasena";
        }
        
        if (codigoVerificacion.getFechaExpiracion().isBefore(LocalDateTime.now())) {
            System.out.println("ERROR: Código expirado. FechaExpiracion: " + codigoVerificacion.getFechaExpiracion() + 
                             ", FechaActual: " + LocalDateTime.now());
            model.addAttribute("email", email);
            model.addAttribute("codigo", codigo);
            model.addAttribute("mensajeError", 
                "El código ha expirado. Por favor, solicita un nuevo código.");
            return "cambiar-contrasena";
        }
        
        // Validar que el código no haya sido usado
        if (codigoVerificacion.getEstado() != CodigoVerificacion.EstadoCodigo.GENERADO) {
            System.out.println("ERROR: Código ya usado. Estado actual: " + codigoVerificacion.getEstado() + ", Esperado: GENERADO");
            model.addAttribute("email", email);
            model.addAttribute("codigo", codigo);
            model.addAttribute("mensajeError", 
                "Este código ya ha sido utilizado. Por favor, solicita un nuevo código.");
            return "cambiar-contrasena";
        }
        
        System.out.println("✓ Validación exitosa - Código válido");
        System.out.println("=== FIN DEBUG ===");
        
        // Encriptar la nueva contraseña con BCrypt
        String hashPassword = passwordEncoder.encode(nuevaContrasena);
        
        // Actualizar solo la contraseña en la base de datos usando consulta nativa
        usuarioRepository.updatePassword(usuario.getUsuarioId(), hashPassword);
        
        // Marcar el código como usado usando consulta nativa
        LocalDateTime ahora = LocalDateTime.now();
        codigoVerificacionRepository.marcarComoUsado(
            codigoVerificacion.getCodigoId(), 
            "USADO", 
            ahora
        );
        
        // Limpiar sesión
        session.removeAttribute(CODIGO_SESION_KEY);
        session.removeAttribute(EMAIL_SESION_KEY);
        session.removeAttribute(TIMESTAMP_SESION_KEY);
        
        redirectAttributes.addFlashAttribute("mensajeExito", 
            "Contraseña actualizada exitosamente. Por favor, inicia sesión.");
        return "redirect:/login";
    }
}

