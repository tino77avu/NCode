package com.admin.ncode.controller;

import com.admin.ncode.dto.ContactoRequest;
import com.admin.ncode.dto.DemoRequest;
import com.admin.ncode.dto.LoginRequest;
import com.admin.ncode.entity.Usuario;
import com.admin.ncode.repository.UsuarioRepository;
import com.admin.ncode.service.PlanLicenciaService;
import com.admin.ncode.util.RateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class HomeController {

    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);
    
    @Autowired
    private PlanLicenciaService planLicenciaService;
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    private final RateLimiter rateLimiter = new RateLimiter();

    @GetMapping("/")
    public String index(Model model, HttpSession session) {
        Boolean isAuthenticated = (Boolean) session.getAttribute("isAuthenticated");
        model.addAttribute("isAuthenticated", isAuthenticated != null && isAuthenticated);
        // Agregar demoRequest si no existe (para el formulario del modal)
        if (!model.containsAttribute("demoRequest")) {
            model.addAttribute("demoRequest", new com.admin.ncode.dto.DemoRequest());
        }
        // Los mensajes flash se agregan automáticamente por Spring si existen
        return "index";
    }

    @GetMapping("/contacto")
    public String contacto(Model model) {
        model.addAttribute("isAuthenticated", false);
        if (!model.containsAttribute("contactoRequest")) {
            model.addAttribute("contactoRequest", new ContactoRequest());
        }
        return "contacto";
    }

    @PostMapping("/contacto")
    public String procesarContacto(@Valid @ModelAttribute("contactoRequest") ContactoRequest contactoRequest,
                                   BindingResult bindingResult,
                                   Model model,
                                   HttpServletRequest request) {
        model.addAttribute("isAuthenticated", false);
        
        if (bindingResult.hasErrors()) {
            logger.warn("Error de validación en formulario de contacto desde IP: {}", getClientIp(request));
            // El objeto ya está en el modelo por @ModelAttribute, solo retornamos la vista
            return "contacto";
        }
        
        // Aquí puedes procesar el formulario (enviar email, guardar en BD, etc.)
        // Por ahora solo redirigimos con un mensaje de éxito
        logger.info("Formulario de contacto recibido de: {}", contactoRequest.getEmail());
        model.addAttribute("mensajeExito", "¡Gracias por contactarnos! Te responderemos pronto.");
        // Limpiar el objeto del formulario después de éxito
        model.addAttribute("contactoRequest", new ContactoRequest());
        return "contacto";
    }

    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("isAuthenticated", false);
        if (!model.containsAttribute("loginRequest")) {
            model.addAttribute("loginRequest", new LoginRequest());
        }
        return "login";
    }
    
    @PostMapping("/login")
    public String procesarLogin(@Valid @ModelAttribute("loginRequest") LoginRequest loginRequest,
                                BindingResult bindingResult,
                                HttpSession session,
                                HttpServletRequest request,
                                RedirectAttributes redirectAttributes,
                                Model model) {
        String clientIp = getClientIp(request);
        String email = loginRequest.getUsername() != null ? loginRequest.getUsername().trim().toLowerCase() : "";
        
        model.addAttribute("isAuthenticated", false);
        
        // Validación básica
        if (bindingResult.hasErrors() || email.isEmpty() || loginRequest.getPassword() == null) {
            logger.warn("Intento de login con datos inválidos desde IP: {}", clientIp);
            // Mantener el objeto en el modelo para mostrar errores
            return "login";
        }
        
        // Rate limiting por IP y email
        String rateLimitKey = clientIp + ":" + email;
        if (!rateLimiter.isAllowed(rateLimitKey)) {
            long remainingTime = rateLimiter.getRemainingTime(rateLimitKey);
            int minutes = (int) (remainingTime / 60000);
            logger.warn("Intento de login bloqueado por rate limiting. IP: {}, Email: {}, Tiempo restante: {} minutos", 
                       clientIp, email, minutes);
            model.addAttribute("mensajeError", 
                String.format("Demasiados intentos fallidos. Intenta de nuevo en %d minutos.", minutes));
            return "login";
        }
        
        try {
            // Buscar usuario por email
            Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
            
            // Usar tiempo constante para prevenir timing attacks
            boolean loginExitoso = false;
            Usuario usuario = null;
            
            if (usuarioOpt.isPresent()) {
                usuario = usuarioOpt.get();
                
                // Validar que el usuario esté activo
                if (usuario.getEstado() == Usuario.EstadoUsuario.ACTIVO) {
                    // Validar la contraseña usando BCrypt
                    if (passwordEncoder.matches(loginRequest.getPassword(), usuario.getHashPassword())) {
                        loginExitoso = true;
                    }
                }
            }
            
            // Si el login falló, registrar y retornar
            if (!loginExitoso) {
                logger.warn("Intento de login fallido. IP: {}, Email: {}", clientIp, email);
                redirectAttributes.addFlashAttribute("mensajeError", 
                    "Credenciales inválidas. Verifica tu email y contraseña.");
                return "redirect:/login";
            }
            
            // Login exitoso - resetear rate limiter
            rateLimiter.reset(rateLimitKey);
            
            // Actualizar último login
            LocalDateTime ahora = LocalDateTime.now();
            usuarioRepository.updateUltimoLogin(usuario.getUsuarioId(), ahora);
            
            // Crear Authentication para Spring Security ANTES de invalidar la sesión
            java.util.Collection<org.springframework.security.core.GrantedAuthority> authorities = 
                java.util.Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                usuario.getEmail(), 
                null, 
                authorities
            );
            
            // Regenerar ID de sesión para prevenir session fixation
            session.invalidate();
            session = request.getSession(true);
            
            // Establecer Authentication en SecurityContext
            org.springframework.security.core.context.SecurityContext securityContext = 
                SecurityContextHolder.createEmptyContext();
            securityContext.setAuthentication(authentication);
            SecurityContextHolder.setContext(securityContext);
            
            // Guardar SecurityContext en la sesión HTTP (Spring Security lo busca con esta clave)
            session.setAttribute("SPRING_SECURITY_CONTEXT", securityContext);
            
            // Guardar usuario en sesión
            session.setAttribute("usuario", usuario);
            session.setAttribute("isAuthenticated", true);
            
            logger.info("Login exitoso. Usuario: {}, IP: {}", email, clientIp);
            redirectAttributes.addFlashAttribute("mensajeExito", 
                "Login exitoso. Bienvenido, " + usuario.getNombres() + "!");
            return "redirect:/";
            
        } catch (Exception e) {
            logger.error("Error al procesar login. IP: {}, Email: {}", clientIp, email, e);
            redirectAttributes.addFlashAttribute("mensajeError", 
                "Error al procesar la solicitud. Por favor, intenta de nuevo.");
            return "redirect:/login";
        }
    }
    
    /**
     * Obtiene la IP real del cliente
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // Si hay múltiples IPs, tomar la primera
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    @GetMapping("/registro")
    public String registro(Model model) {
        model.addAttribute("isAuthenticated", false);
        return "registro";
    }

    @GetMapping("/planes")
    public String planes(Model model) {
        model.addAttribute("isAuthenticated", false);
        try {
            var planes = planLicenciaService.findAllPlanes();
            Map<Integer, List<com.admin.ncode.entity.PlanLicenciaDetalle>> detallesPorPlan = planLicenciaService.getDetallesPorPlan();
            
            model.addAttribute("planes", planes != null ? planes : List.of());
            model.addAttribute("detallesPorPlan", detallesPorPlan != null ? detallesPorPlan : Map.of());
            
        } catch (Exception e) {
            model.addAttribute("planes", List.of());
            model.addAttribute("detallesPorPlan", Map.of());
        }
        
        return "planes";
    }
    
}
