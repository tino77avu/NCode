package com.admin.ncode.controller;

import com.admin.ncode.entity.RolGlobal;
import com.admin.ncode.repository.RolGlobalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/gestion/roles")
public class RolGlobalController {

    @Autowired
    private RolGlobalRepository rolGlobalRepository;

    @GetMapping
    public String listarRoles(Model model) {
        List<RolGlobal> roles = rolGlobalRepository.findAllByOrderByRolGlobalIdAsc();
        model.addAttribute("roles", roles);
        model.addAttribute("rol", new RolGlobal());
        return "gestion/roles";
    }

    @PostMapping("/crear")
    public String crearRol(@ModelAttribute RolGlobal rol, RedirectAttributes redirectAttributes) {
        try {
            rolGlobalRepository.save(rol);
            redirectAttributes.addFlashAttribute("mensajeExito", "Rol creado exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al crear el rol: " + e.getMessage());
        }
        return "redirect:/gestion/roles";
    }

    @PostMapping("/editar/{id}")
    public String editarRol(@PathVariable Integer id, 
                           @RequestParam String nombreRol,
                           @RequestParam(required = false) String descripcion,
                           RedirectAttributes redirectAttributes) {
        try {
            RolGlobal rolExistente = rolGlobalRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Rol no encontrado"));
            rolExistente.setNombreRol(nombreRol);
            rolExistente.setDescripcion(descripcion != null ? descripcion : "");
            rolGlobalRepository.save(rolExistente);
            redirectAttributes.addFlashAttribute("mensajeExito", "Rol actualizado exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al actualizar el rol: " + e.getMessage());
        }
        return "redirect:/gestion/roles";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminarRol(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            rolGlobalRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("mensajeExito", "Rol eliminado exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al eliminar el rol: " + e.getMessage());
        }
        return "redirect:/gestion/roles";
    }
}

