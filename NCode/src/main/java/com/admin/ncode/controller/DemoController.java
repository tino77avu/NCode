package com.admin.ncode.controller;

import com.admin.ncode.dto.DemoRequest;
import com.admin.ncode.entity.CodigoVerificacion;
import com.admin.ncode.entity.Empresa;
import com.admin.ncode.entity.SolicitudDemo;
import com.admin.ncode.repository.CodigoVerificacionRepository;
import com.admin.ncode.repository.EmpresaRepository;
import com.admin.ncode.repository.SolicitudDemoRepository;
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
import java.util.Optional;

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
    
    @Autowired
    private SolicitudDemoRepository solicitudDemoRepository;

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
            
            // Buscar o crear empresa por RUC
            Long empresaId = obtenerOcrearEmpresa(demoRequest);
            
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
            
            // Buscar el código recién insertado para obtener su ID
            Long codigoId = null;
            try {
                Optional<CodigoVerificacion> codigoOpt = codigoVerificacionRepository.findByCodigoAndTipoDemo(codigo);
                if (codigoOpt.isPresent()) {
                    codigoId = codigoOpt.get().getCodigoId();
                }
            } catch (Exception e) {
                logger.warn("No se pudo obtener el ID del código de verificación: {}", e.getMessage());
            }
            
            // Guardar solicitud de demo en la tabla dedicada
            SolicitudDemo solicitudDemo = new SolicitudDemo();
            solicitudDemo.setNombre(demoRequest.getNombre());
            solicitudDemo.setCorreo(demoRequest.getCorreo());
            solicitudDemo.setEmpresa(demoRequest.getEmpresa());
            solicitudDemo.setRuc(demoRequest.getRuc());
            solicitudDemo.setDireccion(demoRequest.getDireccion());
            solicitudDemo.setCodigoId(codigoId);
            solicitudDemo.setCodigoVerificacion(codigo);
            solicitudDemo.setEstado("PENDIENTE");
            solicitudDemo.setIpCliente(ipCliente);
            solicitudDemoRepository.save(solicitudDemo);
            
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

    private Long obtenerOcrearEmpresa(DemoRequest demoRequest) {
        // Buscar empresa por RUC
        Optional<Empresa> empresaOpt = empresaRepository.findByRuc(demoRequest.getRuc());
        
        if (empresaOpt.isPresent()) {
            // Si la empresa ya existe, retornar su ID
            logger.info("Empresa encontrada por RUC: {} - ID: {}", demoRequest.getRuc(), empresaOpt.get().getEmpresaId());
            return empresaOpt.get().getEmpresaId();
        } else {
            try {
                // Si no existe, crear una nueva empresa usando consulta nativa con CAST
                empresaRepository.insertEmpresa(
                    demoRequest.getRuc(),
                    demoRequest.getEmpresa(),
                    demoRequest.getEmpresa(),
                    "Peru",
                    "ACTIVA"
                );
                
                // Buscar la empresa recién creada para obtener su ID
                Optional<Empresa> empresaCreada = empresaRepository.findByRuc(demoRequest.getRuc());
                if (empresaCreada.isPresent()) {
                    Long empresaId = empresaCreada.get().getEmpresaId();
                    logger.info("Nueva empresa creada. RUC: {} - ID: {}", demoRequest.getRuc(), empresaId);
                    return empresaId;
                } else {
                    // Si no se encontró después de insertar, podría ser un problema de sincronización
                    // Intentar buscar nuevamente después de un breve delay
                    logger.warn("No se encontró la empresa inmediatamente después de insertar. Reintentando búsqueda...");
                    empresaCreada = empresaRepository.findByRuc(demoRequest.getRuc());
                    if (empresaCreada.isPresent()) {
                        return empresaCreada.get().getEmpresaId();
                    }
                    throw new RuntimeException("Error al crear la empresa: no se pudo encontrar después de insertar");
                }
            } catch (org.springframework.dao.DataIntegrityViolationException e) {
                // Si hay un error de integridad (por ejemplo, RUC duplicado o ID duplicado),
                // intentar buscar la empresa nuevamente
                logger.warn("Error de integridad al insertar empresa. Reintentando búsqueda por RUC: {}", demoRequest.getRuc());
                Optional<Empresa> empresaExistente = empresaRepository.findByRuc(demoRequest.getRuc());
                if (empresaExistente.isPresent()) {
                    logger.info("Empresa encontrada después del error. RUC: {} - ID: {}", demoRequest.getRuc(), empresaExistente.get().getEmpresaId());
                    return empresaExistente.get().getEmpresaId();
                }
                throw new RuntimeException("Error al crear la empresa: " + e.getMessage(), e);
            }
        }
    }
}

