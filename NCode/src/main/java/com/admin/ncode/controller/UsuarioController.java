package com.admin.ncode.controller;

import com.admin.ncode.entity.Usuario;
import com.admin.ncode.entity.RolGlobal;
import com.admin.ncode.repository.UsuarioRepository;
import com.admin.ncode.repository.RolGlobalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/gestion/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolGlobalRepository rolGlobalRepository;

    @GetMapping
    public String listarUsuarios(Model model) {
        model.addAttribute("isAuthenticated", true);
        List<Usuario> usuarios = usuarioRepository.findAllByOrderByUsuarioIdAsc();
        List<RolGlobal> roles = rolGlobalRepository.findAllByOrderByRolGlobalIdAsc();
        model.addAttribute("usuarios", usuarios);
        model.addAttribute("roles", roles);
        model.addAttribute("usuario", new Usuario());
        return "gestion/usuarios";
    }

    @PostMapping("/crear")
    public String crearUsuario(@RequestParam String email,
                               @RequestParam String hashPassword,
                               @RequestParam String nombres,
                               @RequestParam String apellidos,
                               @RequestParam(required = false) String telefono,
                               @RequestParam(required = false) String esAdminGlobal,
                               @RequestParam String estado,
                               @RequestParam Integer rolGlobalId,
                               RedirectAttributes redirectAttributes) {
        try {
            RolGlobal rolGlobal = rolGlobalRepository.findById(rolGlobalId)
                    .orElseThrow(() -> new RuntimeException("Rol no encontrado"));
            
            Usuario usuario = new Usuario();
            usuario.setEmail(email);
            usuario.setHashPassword(hashPassword);
            usuario.setNombres(nombres);
            usuario.setApellidos(apellidos);
            usuario.setTelefono(telefono != null ? telefono : "");
            usuario.setEsAdminGlobal("true".equals(esAdminGlobal));
            usuario.setEstado(Usuario.EstadoUsuario.valueOf(estado));
            usuario.setRolGlobal(rolGlobal);
            
            usuarioRepository.save(usuario);
            redirectAttributes.addFlashAttribute("mensajeExito", "Usuario creado exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al crear el usuario: " + e.getMessage());
        }
        return "redirect:/gestion/usuarios";
    }

    @PostMapping("/editar/{id}")
    public String editarUsuario(@PathVariable Long id,
                                @RequestParam String email,
                                @RequestParam String hashPassword,
                                @RequestParam String nombres,
                                @RequestParam String apellidos,
                                @RequestParam(required = false) String telefono,
                                @RequestParam(required = false) String esAdminGlobal,
                                @RequestParam String estado,
                                @RequestParam Integer rolGlobalId,
                                RedirectAttributes redirectAttributes) {
        try {
            Usuario usuarioExistente = usuarioRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            
            RolGlobal rolGlobal = rolGlobalRepository.findById(rolGlobalId)
                    .orElseThrow(() -> new RuntimeException("Rol no encontrado"));
            
            usuarioExistente.setEmail(email);
            usuarioExistente.setHashPassword(hashPassword);
            usuarioExistente.setNombres(nombres);
            usuarioExistente.setApellidos(apellidos);
            usuarioExistente.setTelefono(telefono != null ? telefono : "");
            usuarioExistente.setEsAdminGlobal("true".equals(esAdminGlobal));
            usuarioExistente.setEstado(Usuario.EstadoUsuario.valueOf(estado));
            usuarioExistente.setRolGlobal(rolGlobal);
            
            usuarioRepository.save(usuarioExistente);
            redirectAttributes.addFlashAttribute("mensajeExito", "Usuario actualizado exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al actualizar el usuario: " + e.getMessage());
        }
        return "redirect:/gestion/usuarios";
    }

    @PostMapping("/bloquear/{id}")
    public String bloquearUsuario(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            usuarioRepository.updateEstado(id, "BLOQUEADO");
            redirectAttributes.addFlashAttribute("mensajeExito", "Bloqueo Satisfactorio");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al bloquear el usuario: " + e.getMessage());
        }
        return "redirect:/gestion/usuarios";
    }

    @PostMapping("/activar/{id}")
    public String activarUsuario(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            usuarioRepository.updateEstado(id, "ACTIVO");
            redirectAttributes.addFlashAttribute("mensajeExito", "Activación Satisfactoria");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al activar el usuario: " + e.getMessage());
        }
        return "redirect:/gestion/usuarios";
    }
}

