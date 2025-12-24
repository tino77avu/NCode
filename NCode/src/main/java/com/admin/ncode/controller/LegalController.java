package com.admin.ncode.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpSession;

@Controller
public class LegalController {

    private static final Logger logger = LoggerFactory.getLogger(LegalController.class);

    @GetMapping("/legal/politica-privacidad")
    public String politicaPrivacidad(Model model, HttpSession session) {
        Boolean isAuthenticated = (Boolean) session.getAttribute("isAuthenticated");
        model.addAttribute("isAuthenticated", isAuthenticated != null && isAuthenticated);
        return "legal/politica-privacidad";
    }

    @GetMapping("/legal/politicas-cookies")
    public String politicasCookies(Model model, HttpSession session) {
        Boolean isAuthenticated = (Boolean) session.getAttribute("isAuthenticated");
        model.addAttribute("isAuthenticated", isAuthenticated != null && isAuthenticated);
        return "legal/politicas-cookies";
    }

    @GetMapping("/legal/terminos-condiciones")
    public String terminosCondiciones(Model model, HttpSession session) {
        Boolean isAuthenticated = (Boolean) session.getAttribute("isAuthenticated");
        model.addAttribute("isAuthenticated", isAuthenticated != null && isAuthenticated);
        return "legal/terminos-condiciones";
    }

    @GetMapping("/legal/libro-reclamaciones")
    public String libroReclamaciones(Model model, HttpSession session) {
        Boolean isAuthenticated = (Boolean) session.getAttribute("isAuthenticated");
        model.addAttribute("isAuthenticated", isAuthenticated != null && isAuthenticated);
        return "legal/libro-reclamaciones";
    }
}


