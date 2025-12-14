package com.admin.ncode.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro para mejorar la seguridad de las sesiones
 * - Regenera el ID de sesión después del login
 * - Valida que las rutas protegidas tengan sesión válida
 */
@Component
public class SessionSecurityFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                   HttpServletResponse response, 
                                   FilterChain filterChain) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        
        // Si hay una sesión y el usuario está autenticado, asegurar que la sesión sea segura
        if (session != null) {
            Boolean isAuthenticated = (Boolean) session.getAttribute("isAuthenticated");
            
            // Si está autenticado y accediendo a rutas protegidas, validar sesión
            if (isAuthenticated != null && isAuthenticated) {
                String requestPath = request.getRequestURI();
                
                // Regenerar ID de sesión después del login (se hace en el controlador)
                // Aquí solo validamos que la sesión sea válida
                if (requestPath.startsWith("/gestion/") && session.getAttribute("usuario") == null) {
                    // Sesión inválida, redirigir a login
                    session.invalidate();
                    response.sendRedirect(request.getContextPath() + "/login");
                    return;
                }
            }
        }
        
        filterChain.doFilter(request, response);
    }
}

