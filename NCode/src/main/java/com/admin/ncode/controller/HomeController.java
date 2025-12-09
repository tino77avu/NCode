package com.admin.ncode.controller;

import com.admin.ncode.entity.Usuario;
import com.admin.ncode.repository.UsuarioRepository;
import com.admin.ncode.service.PlanLicenciaService;
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
import java.util.Map;
import java.util.Optional;

@Controller
public class HomeController {

    @Autowired
    private PlanLicenciaService planLicenciaService;
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @GetMapping("/")
    public String index(Model model, HttpSession session) {
        Boolean isAuthenticated = (Boolean) session.getAttribute("isAuthenticated");
        model.addAttribute("isAuthenticated", isAuthenticated != null && isAuthenticated);
        return "index";
    }

    @GetMapping("/contacto")
    public String contacto(Model model) {
        model.addAttribute("isAuthenticated", false);
        return "contacto";
    }

    @PostMapping("/contacto")
    public String procesarContacto(@RequestParam String nombre,
                                   @RequestParam String email,
                                   @RequestParam(required = false) String telefono,
                                   @RequestParam String asunto,
                                   @RequestParam String mensaje,
                                   Model model) {
        // Aquí puedes procesar el formulario (enviar email, guardar en BD, etc.)
        // Por ahora solo redirigimos con un mensaje de éxito
        model.addAttribute("isAuthenticated", false);
        model.addAttribute("mensajeExito", "¡Gracias por contactarnos! Te responderemos pronto.");
        return "contacto";
    }

    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("isAuthenticated", false);
        return "login";
    }
    
    @PostMapping("/login")
    public String procesarLogin(@RequestParam String username,
                                @RequestParam String password,
                                HttpSession session,
                                RedirectAttributes redirectAttributes,
                                Model model) {
        try {
            // Buscar usuario por email (el campo username del formulario es el email)
            Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(username);
            
            if (usuarioOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("mensajeError", "Login fallido: Usuario no encontrado.");
                return "redirect:/login";
            }
            
            Usuario usuario = usuarioOpt.get();
            
            // Validar que el usuario esté activo
            if (usuario.getEstado() != Usuario.EstadoUsuario.ACTIVO) {
                redirectAttributes.addFlashAttribute("mensajeError", "Login fallido: Usuario inactivo.");
                return "redirect:/login";
            }
            
            // Validar la contraseña usando BCrypt
            if (!passwordEncoder.matches(password, usuario.getHashPassword())) {
                redirectAttributes.addFlashAttribute("mensajeError", "Login fallido: Contraseña incorrecta.");
                return "redirect:/login";
            }
            
            // Actualizar último login usando consulta nativa para evitar problemas con enum
            LocalDateTime ahora = LocalDateTime.now();
            usuarioRepository.updateUltimoLogin(usuario.getUsuarioId(), ahora);
            
            // Guardar usuario en sesión (por ahora solo para indicar que está autenticado)
            session.setAttribute("usuario", usuario);
            session.setAttribute("isAuthenticated", true);
            
            redirectAttributes.addFlashAttribute("mensajeExito", "Login exitoso. Bienvenido, " + usuario.getNombres() + "!");
            return "redirect:/";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", "Login fallido: Error al procesar la solicitud.");
            return "redirect:/login";
        }
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
    
    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        // Invalidar la sesión
        session.invalidate();
        redirectAttributes.addFlashAttribute("mensajeExito", "Sesión cerrada exitosamente");
        return "redirect:/";
    }
}
