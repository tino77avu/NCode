package com.admin.ncode.controller;

import com.admin.ncode.entity.SolicitudDemo;
import com.admin.ncode.repository.SolicitudDemoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/gestion/demos")
public class DemosController {

    @Autowired
    private SolicitudDemoRepository solicitudDemoRepository;

    @GetMapping
    public String listarDemos(Model model) {
        try {
            model.addAttribute("isAuthenticated", true);
            List<SolicitudDemo> demos = solicitudDemoRepository.findAll();
            // Ordenar por fecha de solicitud descendente (más recientes primero)
            if (demos != null && !demos.isEmpty()) {
                demos.sort((a, b) -> {
                    if (a.getFechaSolicitud() == null && b.getFechaSolicitud() == null) return 0;
                    if (a.getFechaSolicitud() == null) return 1;
                    if (b.getFechaSolicitud() == null) return -1;
                    return b.getFechaSolicitud().compareTo(a.getFechaSolicitud());
                });
            }
            model.addAttribute("demos", demos != null ? demos : java.util.Collections.emptyList());
        } catch (Exception e) {
            model.addAttribute("demos", java.util.Collections.emptyList());
            model.addAttribute("mensajeError", "Error al cargar las solicitudes de demo: " + e.getMessage());
        }
        return "gestion/demos";
    }

    @PostMapping("/procesar/{id}")
    public String procesarDemo(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            SolicitudDemo solicitud = solicitudDemoRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Solicitud de demo no encontrada"));
            solicitud.setEstado("PROCESADO");
            solicitud.setFechaProcesado(java.time.LocalDateTime.now());
            solicitudDemoRepository.save(solicitud);
            redirectAttributes.addFlashAttribute("mensajeExito", "Solicitud de demo procesada exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al procesar la solicitud: " + e.getMessage());
        }
        return "redirect:/gestion/demos";
    }

    @PostMapping("/cancelar/{id}")
    public String cancelarDemo(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            SolicitudDemo solicitud = solicitudDemoRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Solicitud de demo no encontrada"));
            solicitud.setEstado("CANCELADO");
            solicitud.setFechaProcesado(java.time.LocalDateTime.now());
            solicitudDemoRepository.save(solicitud);
            redirectAttributes.addFlashAttribute("mensajeExito", "Solicitud de demo cancelada exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al cancelar la solicitud: " + e.getMessage());
        }
        return "redirect:/gestion/demos";
    }
}

