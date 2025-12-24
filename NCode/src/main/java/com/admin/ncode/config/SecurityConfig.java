package com.admin.ncode.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired(required = false)
    private SessionSecurityFilter sessionSecurityFilter;

    @Autowired(required = false)
    private CustomLogoutSuccessHandler logoutSuccessHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Deshabilitar CSRF para endpoints específicos si es necesario
            // Por ahora lo mantenemos habilitado para mayor seguridad
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/**") // Si tienes APIs REST, exclúyelas aquí
            )
            
            // Configuración de autorización
            .authorizeHttpRequests(auth -> auth
                // Rutas públicas
                .requestMatchers(
                    "/",
                    "/login",
                    "/registro",
                    "/contacto",
                    "/planes",
                    "/olvidar-contrasena",
                    "/cambiar-contrasena",
                    "/demo/**",
                    "/legal/**",
                    "/css/**",
                    "/images/**",
                    "/js/**",
                    "/error"
                ).permitAll()
                
                // Rutas de gestión requieren autenticación
                .requestMatchers("/gestion/**").authenticated()
                
                // Todas las demás rutas requieren autenticación
                .anyRequest().authenticated()
            )
            
            // Configuración de sesión
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .maximumSessions(1)
                .maxSessionsPreventsLogin(false)
            )
            
            // Configuración de logout
            .logout(logout -> {
                logout.logoutUrl("/logout")
                      .invalidateHttpSession(true)
                      .deleteCookies("JSESSIONID")
                      .permitAll();
                if (logoutSuccessHandler != null) {
                    logout.logoutSuccessHandler(logoutSuccessHandler);
                } else {
                    logout.logoutSuccessUrl("/");
                }
            })
            
            // Headers de seguridad
            .headers(headers -> headers
                .contentTypeOptions(contentTypeOptions -> {})
                .frameOptions(frameOptions -> frameOptions.deny())
                .xssProtection(xssProtection -> xssProtection
                    .headerValue(org.springframework.security.web.header.writers.XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK)
                )
                .referrerPolicy(referrerPolicy -> referrerPolicy
                    .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
                )
                .httpStrictTransportSecurity(hsts -> hsts
                    .maxAgeInSeconds(31536000)
                    .includeSubDomains(true)
                )
            )
            
            // Manejo de excepciones
            .exceptionHandling(exception -> exception
                .accessDeniedPage("/error")
            );
        
        // Agregar filtro de sesión personalizado si existe
        if (sessionSecurityFilter != null) {
            http.addFilterBefore(sessionSecurityFilter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class);
        }

        return http.build();
    }
}

