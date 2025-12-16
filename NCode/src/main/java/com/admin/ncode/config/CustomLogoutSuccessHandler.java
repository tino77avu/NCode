package com.admin.ncode.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.FlashMapManager;
import org.springframework.web.servlet.support.RequestContextUtils;

import java.io.IOException;

@Component
public class CustomLogoutSuccessHandler extends SimpleUrlLogoutSuccessHandler implements LogoutSuccessHandler {

    public CustomLogoutSuccessHandler() {
        super();
        setDefaultTargetUrl("/");
    }

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        // Agregar mensaje como flash attribute antes de redirigir
        // Usar RequestContextUtils para obtener el FlashMapManager
        FlashMapManager flashMapManager = RequestContextUtils.getFlashMapManager(request);
        
        if (flashMapManager != null) {
            FlashMap flashMap = new FlashMap();
            flashMap.put("mensajeExito", "Sesion cerrada exitosamente");
            flashMapManager.saveOutputFlashMap(flashMap, request, response);
        }
        
        super.onLogoutSuccess(request, response, authentication);
    }
}

