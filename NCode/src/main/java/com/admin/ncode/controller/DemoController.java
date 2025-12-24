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

    @GetMapping
    public String mostrarDemo(Model model, HttpSession session) {
        try {
            logger.debug("Accediendo a GET /demo");
            Boolean isAuthenticated = (Boolean) session.getAttribute("isAuthenticated");
            model.addAttribute("isAuthenticated", isAuthenticated != null && isAuthenticated);
            logger.debug("isAuthenticated: {}", isAuthenticated);
            
            // Siempre crear un nuevo DemoRequest vacío para asegurar que los campos estén limpios
            // Solo mantener el demoRequest del modelo si hay errores de validación (viene de POST)
            // Si viene de un redirect exitoso, no habrá demoRequest en el modelo, así que creamos uno vacío
            if (!model.containsAttribute("demoRequest")) {
                model.addAttribute("demoRequest", new DemoRequest());
                logger.debug("demoRequest vacío agregado al modelo (nueva solicitud)");
            } else {
                // Si existe, es porque hay errores de validación, así que lo mantenemos
                logger.debug("demoRequest mantenido del modelo (hay errores de validación)");
            }
            
            // Los mensajes flash se agregan automáticamente por Spring si existen
            logger.debug("Retornando vista 'demo'");
            return "demo";
        } catch (Exception e) {
            logger.error("Error en GET /demo: {}", e.getMessage(), e);
            throw e; // Re-lanzar para que Spring maneje el error
        }
    }

    @PostMapping
    public String solicitarDemo(@Valid @ModelAttribute("demoRequest") DemoRequest demoRequest,
                                BindingResult bindingResult,
                                HttpServletRequest request,
                                HttpSession session,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        
        logger.info("POST /demo - Iniciando procesamiento. RUC: {}, Email: {}", 
                   demoRequest != null ? demoRequest.getRuc() : "null", 
                   demoRequest != null ? demoRequest.getCorreo() : "null");
        
        // Validación
        if (bindingResult.hasErrors()) {
            logger.warn("Error de validación en solicitud de demo desde IP: {}. Errores: {}", 
                       obtenerIpCliente(request), bindingResult.getAllErrors());
            // Mantener el objeto en el modelo para mostrar errores
            Boolean isAuthenticated = (Boolean) session.getAttribute("isAuthenticated");
            model.addAttribute("isAuthenticated", isAuthenticated != null && isAuthenticated);
            model.addAttribute("demoRequest", demoRequest);
            model.addAttribute("mensajeError", "Por favor, completa todos los campos correctamente.");
            logger.debug("Retornando vista demo con errores de validación");
            return "demo";
        }

        try {
            // Validar si la empresa ya tiene una demo vigente o en proceso
            List<SolicitudDemo> demosActivas;
            try {
                demosActivas = solicitudDemoRepository.findActivasByRuc(demoRequest.getRuc());
                logger.debug("Búsqueda de demos activas para RUC: {}. Encontradas: {}", demoRequest.getRuc(), demosActivas.size());
            } catch (Exception e) {
                logger.error("Error al buscar demos activas para RUC: {}", demoRequest.getRuc(), e);
                demosActivas = List.of(); // Continuar sin validación si hay error
            }
            if (!demosActivas.isEmpty()) {
                SolicitudDemo demoActiva = demosActivas.get(0); // La más reciente
                String estadoMensaje = demoActiva.getEstado().equals("PENDIENTE") ? "en proceso" : "vigente";
                logger.warn("Intento de solicitar demo para empresa con demo {} existente. RUC: {}, IP: {}", 
                           estadoMensaje, demoRequest.getRuc(), obtenerIpCliente(request));
                Boolean isAuthenticated = (Boolean) session.getAttribute("isAuthenticated");
                model.addAttribute("isAuthenticated", isAuthenticated != null && isAuthenticated);
                model.addAttribute("demoRequest", demoRequest);
                model.addAttribute("mensajeError", 
                    "La empresa con RUC " + demoRequest.getRuc() + " ya tiene una demo " + estadoMensaje + ". " +
                    "Por favor, contacta con soporte si necesitas ayuda.");
                return "demo";
            }

            String codigo = null;
            Long empresaId = null;
            Long codigoId = null;
            String ipCliente = null;
            
            try {
                logger.info("Iniciando procesamiento de solicitud de demo. RUC: {}, Email: {}", 
                           demoRequest.getRuc(), demoRequest.getCorreo());
                
            // Generar código de verificación
                codigo = CodigoGenerador.generarCodigo();
                logger.debug("Código de verificación generado: {}", codigo);
                
                // Buscar o crear empresa por RUC
                try {
                    empresaId = obtenerOcrearEmpresa(demoRequest);
                    logger.debug("Empresa ID obtenido: {}", empresaId);
                } catch (Exception e) {
                    logger.error("Error al obtener o crear empresa. RUC: {}", demoRequest.getRuc(), e);
                    throw new RuntimeException("Error al procesar la información de la empresa: " + e.getMessage(), e);
                }
            
            // Obtener IP del cliente
                ipCliente = obtenerIpCliente(request);
                logger.debug("IP del cliente: {}", ipCliente);
            
            // Guardar código de verificación en la base de datos
                // Para el demo, el código tiene validez de 1 día (24 horas)
                LocalDateTime fechaExpiracion = LocalDateTime.now().plusDays(1);
                try {
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
                    logger.debug("Código de verificación guardado en BD. Fecha de expiración: {} (1 día desde ahora)", fechaExpiracion);
                } catch (Exception e) {
                    logger.error("Error al guardar código de verificación en BD: {}", e.getMessage(), e);
                    throw new RuntimeException("Error al guardar el código de verificación: " + e.getMessage(), e);
                }
                
                // Buscar el código recién insertado para obtener su ID
                try {
                    Optional<CodigoVerificacion> codigoOpt = codigoVerificacionRepository.findByCodigoAndTipoDemo(codigo);
                    if (codigoOpt.isPresent()) {
                        codigoId = codigoOpt.get().getCodigoId();
                        logger.debug("ID del código de verificación obtenido: {}", codigoId);
                    }
                } catch (Exception e) {
                    logger.warn("No se pudo obtener el ID del código de verificación: {}", e.getMessage());
                    // Continuar sin el ID, no es crítico
                }
                
                // Guardar solicitud de demo en la tabla dedicada
                try {
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
                    logger.debug("Solicitud de demo guardada en BD");
                } catch (Exception e) {
                    logger.error("Error al guardar solicitud de demo en BD: {}", e.getMessage(), e);
                    throw new RuntimeException("Error al guardar la solicitud de demo: " + e.getMessage(), e);
                }
                
                // Enviar email con el código (en un try-catch separado para no fallar si el email falla)
                boolean emailEnviado = false;
                try {
            emailService.enviarCodigoDemo(demoRequest.getCorreo(), codigo, demoRequest.getNombre());
                    emailEnviado = true;
                    logger.info("Email de demo enviado exitosamente a: {}", demoRequest.getCorreo());
                } catch (Exception emailException) {
                    logger.error("Error al enviar email de demo a: {}. El código ya está guardado: {}", 
                                demoRequest.getCorreo(), codigo, emailException);
                    // No lanzar la excepción, solo loguear el error
                    // El código ya está guardado, así que el usuario puede usarlo
                }
            
            // Guardar información en sesión para mostrar en la página
                try {
            session.setAttribute("demoSolicitado", true);
            session.setAttribute("demoEmail", demoRequest.getCorreo());
            session.setAttribute("demoNombre", demoRequest.getNombre());
                    session.setAttribute("demoCodigo", codigo); // Guardar código en sesión por si el email falla
                    logger.debug("Información guardada en sesión");
                } catch (Exception e) {
                    logger.warn("Error al guardar información en sesión: {}", e.getMessage());
                    // Continuar aunque falle, no es crítico
                }
                
                logger.info("Solicitud de demo recibida. Empresa: {}, RUC: {}, Email: {}, IP: {}, Email enviado: {}", 
                           demoRequest.getEmpresa(), demoRequest.getRuc(), demoRequest.getCorreo(), ipCliente, emailEnviado);
            
                // Agregar mensaje de éxito
                try {
                    if (emailEnviado) {
                        redirectAttributes.addFlashAttribute("mensajeExito", 
                            "¡Solicitud recibida! Hemos enviado un código de verificación a tu correo electrónico. Por favor, revisa tu bandeja de entrada.");
                    } else {
            redirectAttributes.addFlashAttribute("mensajeExito", 
                            "¡Solicitud recibida! Tu código de verificación es: " + codigo + 
                            ". Por favor, guárdalo ya que hubo un problema al enviar el email.");
                    }
                    logger.debug("Flash attributes agregados exitosamente");
                } catch (Exception e) {
                    logger.error("Error al agregar flash attributes: {}", e.getMessage(), e);
                    // Continuar con el redirect aunque falle
                }
                
                logger.info("Redirigiendo a página de demo después de procesar solicitud");
                return "redirect:/demo";
            
            } catch (Throwable innerException) {
                // Capturar errores internos y re-lanzarlos para que el catch externo los maneje
                logger.error("Error interno al procesar solicitud de demo. RUC: {}, Email: {}, Error: {}", 
                           demoRequest.getRuc(), 
                           demoRequest.getCorreo(), 
                           innerException.getMessage(), 
                           innerException);
                throw innerException; // Re-lanzar para que el catch externo lo maneje
            }
            
        } catch (Throwable e) {
            // Capturar cualquier error, incluyendo Error y RuntimeException
            logger.error("Error al procesar solicitud de demo. Email: {}, IP: {}, Error: {}, StackTrace: {}", 
                        demoRequest.getCorreo() != null ? demoRequest.getCorreo() : "N/A", 
                        obtenerIpCliente(request), 
                        e.getMessage(),
                        java.util.Arrays.toString(e.getStackTrace()),
                        e);
            
            // Asegurar que siempre se haga redirect, incluso si hay error
            try {
                logger.debug("Intentando agregar flash attributes para redirect de error");
                Boolean isAuthenticated = (Boolean) session.getAttribute("isAuthenticated");
                model.addAttribute("isAuthenticated", isAuthenticated != null && isAuthenticated);
                if (demoRequest != null) {
                    model.addAttribute("demoRequest", demoRequest);
                } else {
                    model.addAttribute("demoRequest", new DemoRequest());
                }
                String mensajeError = "Error al procesar la solicitud. Por favor, intenta nuevamente.";
                // codigo solo existe en el scope del try principal, no aquí
                model.addAttribute("mensajeError", mensajeError);
                logger.info("Retornando vista demo después de error");
                return "demo";
            } catch (Throwable redirectException) {
                logger.error("Error crítico al intentar redirigir: {}, StackTrace: {}", 
                           redirectException.getMessage(),
                           java.util.Arrays.toString(redirectException.getStackTrace()),
                           redirectException);
                // Si el redirect falla, al menos intentar mostrar un mensaje de error
                model.addAttribute("isAuthenticated", false);
                model.addAttribute("mensajeError", "Error crítico al procesar la solicitud. Por favor, contacta con soporte.");
                if (!model.containsAttribute("demoRequest")) {
                    model.addAttribute("demoRequest", demoRequest != null ? demoRequest : new DemoRequest());
                }
                return "demo";
            }
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
                    demoRequest.getDireccion(),
                    demoRequest.getTelefono(),
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

