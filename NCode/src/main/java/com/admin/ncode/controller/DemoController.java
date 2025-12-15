package com.admin.ncode.controller;

import com.admin.ncode.dto.DemoRequest;
import com.admin.ncode.entity.CodigoVerificacion;
import com.admin.ncode.entity.Empresa;
import com.admin.ncode.repository.CodigoVerificacionRepository;
import com.admin.ncode.repository.EmpresaRepository;
import com.admin.ncode.service.EmailService;
import com.admin.ncode.util.CodigoGenerador;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/demo")
public class DemoController {

    private static final Logger logger = LoggerFactory.getLogger(DemoController.class);

    @Autowired
    private EmailService emailService;

    @Autowired
    private CodigoVerificacionRepository codigoVerificacionRepository;

    @Autowired
    private EmpresaRepository empresaRepository;

    @PostMapping("/solicitar")
    public String solicitarDemo(@Valid @ModelAttribute("demoRequest") DemoRequest demoRequest,
                                BindingResult bindingResult,
                                HttpServletRequest request,
                                HttpSession session,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        
        // Validación
        if (bindingResult.hasErrors()) {
            logger.warn("Error de validación en solicitud de demo desde IP: {}", obtenerIpCliente(request));
            // Mantener el objeto en el modelo para mostrar errores
            model.addAttribute("isAuthenticated", false);
            redirectAttributes.addFlashAttribute("demoRequest", demoRequest);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.demoRequest", bindingResult);
            redirectAttributes.addFlashAttribute("mensajeError", "Por favor, completa todos los campos correctamente.");
            return "redirect:/#demo";
        }

        try {
            // Generar código de verificación
            String codigo = CodigoGenerador.generarCodigo();
            
            // Obtener empresaId (usar la primera empresa activa o crear una por defecto)
            Long empresaId = obtenerEmpresaId();
            
            // Obtener IP del cliente
            String ipCliente = obtenerIpCliente(request);
            
            // Guardar código de verificación en la base de datos
            LocalDateTime fechaExpiracion = LocalDateTime.now().plusMinutes(15);
            codigoVerificacionRepository.insertCodigoVerificacion(
                null, // usuarioId es null para solicitudes de demo (no hay usuario registrado aún)
                empresaId,
                codigo,
                "DEMO",
                "GENERADO",
                0,
                3, // Máximo 3 intentos
                fechaExpiracion,
                ipCliente
            );
            
            // Enviar email con el código
            emailService.enviarCodigoDemo(demoRequest.getCorreo(), codigo, demoRequest.getNombre());
            
            // Guardar información en sesión para mostrar en la página
            session.setAttribute("demoSolicitado", true);
            session.setAttribute("demoEmail", demoRequest.getCorreo());
            session.setAttribute("demoNombre", demoRequest.getNombre());
            
            logger.info("Solicitud de demo recibida. Empresa: {}, RUC: {}, Email: {}, IP: {}", 
                       demoRequest.getEmpresa(), demoRequest.getRuc(), demoRequest.getCorreo(), ipCliente);
            
            redirectAttributes.addFlashAttribute("mensajeExito", 
                "¡Solicitud recibida! Hemos enviado un código de verificación a " + demoRequest.getCorreo());
            
            return "redirect:/#demo";
            
        } catch (Exception e) {
            logger.error("Error al procesar solicitud de demo. Email: {}, IP: {}", 
                        demoRequest.getCorreo(), obtenerIpCliente(request), e);
            redirectAttributes.addFlashAttribute("demoRequest", demoRequest);
            redirectAttributes.addFlashAttribute("mensajeError", 
                "Error al procesar la solicitud. Por favor, intenta nuevamente.");
            return "redirect:/#demo";
        }
    }

    private String obtenerIpCliente(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
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
        // (asumiendo que existe una empresa con ID 1, o se creará una)
        return 1L;
    }
}

