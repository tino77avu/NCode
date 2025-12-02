package com.admin.ncode.controller;

import com.admin.ncode.service.PlanLicenciaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@Controller
public class HomeController {

    @Autowired
    private PlanLicenciaService planLicenciaService;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/contacto")
    public String contacto() {
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
        model.addAttribute("mensajeExito", "¡Gracias por contactarnos! Te responderemos pronto.");
        return "contacto";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/registro")
    public String registro() {
        return "registro";
    }

    @GetMapping("/planes")
    public String planes(Model model) {
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
