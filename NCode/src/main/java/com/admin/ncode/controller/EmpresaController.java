package com.admin.ncode.controller;

import com.admin.ncode.entity.Empresa;
import com.admin.ncode.repository.EmpresaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/gestion/empresas")
public class EmpresaController {

    @Autowired
    private EmpresaRepository empresaRepository;

    @GetMapping
    public String listarEmpresas(Model model) {
        model.addAttribute("isAuthenticated", true);
        List<Empresa> empresas = empresaRepository.findAllByOrderByEmpresaIdAsc();
        model.addAttribute("empresas", empresas);
        model.addAttribute("empresa", new Empresa());
        return "gestion/empresas";
    }

    @PostMapping("/crear")
    public String crearEmpresa(@RequestParam String ruc,
                               @RequestParam String razonSocial,
                               @RequestParam(required = false) String nombreComercial,
                               @RequestParam(required = false) String pais,
                               @RequestParam String estado,
                               RedirectAttributes redirectAttributes) {
        try {
            Empresa empresa = new Empresa();
            empresa.setRuc(ruc);
            empresa.setRazonSocial(razonSocial);
            empresa.setNombreComercial(nombreComercial != null ? nombreComercial : "");
            empresa.setPais(pais != null ? pais : "");
            empresa.setEstado(Empresa.EstadoEmpresa.valueOf(estado));
            empresaRepository.save(empresa);
            redirectAttributes.addFlashAttribute("mensajeExito", "Empresa creada exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al crear la empresa: " + e.getMessage());
        }
        return "redirect:/gestion/empresas";
    }

    @PostMapping("/editar/{id}")
    public String editarEmpresa(@PathVariable Long id,
                                @RequestParam String ruc,
                                @RequestParam String razonSocial,
                                @RequestParam(required = false) String nombreComercial,
                                @RequestParam(required = false) String pais,
                                @RequestParam String estado,
                                RedirectAttributes redirectAttributes) {
        try {
            Empresa empresaExistente = empresaRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Empresa no encontrada"));
            empresaExistente.setRuc(ruc);
            empresaExistente.setRazonSocial(razonSocial);
            empresaExistente.setNombreComercial(nombreComercial != null ? nombreComercial : "");
            empresaExistente.setPais(pais != null ? pais : "");
            empresaExistente.setEstado(Empresa.EstadoEmpresa.valueOf(estado));
            empresaRepository.save(empresaExistente);
            redirectAttributes.addFlashAttribute("mensajeExito", "Empresa actualizada exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al actualizar la empresa: " + e.getMessage());
        }
        return "redirect:/gestion/empresas";
    }

    @PostMapping("/bloquear/{id}")
    public String bloquearEmpresa(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            // Usar SUSPENDIDA ya que BLOQUEADA no existe en el enum de PostgreSQL
            empresaRepository.updateEstado(id, "SUSPENDIDA");
            redirectAttributes.addFlashAttribute("mensajeExito", "Bloqueo Satisfactorio");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al bloquear la empresa: " + e.getMessage());
        }
        return "redirect:/gestion/empresas";
    }

    @PostMapping("/activar/{id}")
    public String activarEmpresa(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            empresaRepository.updateEstado(id, "ACTIVA");
            redirectAttributes.addFlashAttribute("mensajeExito", "Activación Satisfactoria");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al activar la empresa: " + e.getMessage());
        }
        return "redirect:/gestion/empresas";
    }
}

