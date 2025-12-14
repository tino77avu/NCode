package com.admin.ncode.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Rate limiter simple para prevenir ataques de fuerza bruta
 * Limita intentos de login por IP y email
 */
public class RateLimiter {
    
    private static final int MAX_ATTEMPTS = 5;
    private static final long WINDOW_MS = 15 * 60 * 1000; // 15 minutos
    
    private final Map<String, AttemptInfo> attempts = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    
    public RateLimiter() {
        // Limpiar intentos expirados cada 5 minutos
        scheduler.scheduleAtFixedRate(this::cleanExpiredAttempts, 5, 5, TimeUnit.MINUTES);
    }
    
    /**
     * Verifica si se permite un intento
     * @param key Clave única (IP o email)
     * @return true si se permite, false si se excedió el límite
     */
    public boolean isAllowed(String key) {
        AttemptInfo info = attempts.get(key);
        
        if (info == null) {
            attempts.put(key, new AttemptInfo(1, System.currentTimeMillis()));
            return true;
        }
        
        long now = System.currentTimeMillis();
        
        // Si pasó la ventana de tiempo, resetear
        if (now - info.firstAttempt > WINDOW_MS) {
            attempts.put(key, new AttemptInfo(1, now));
            return true;
        }
        
        // Si excedió el límite, bloquear
        if (info.count >= MAX_ATTEMPTS) {
            return false;
        }
        
        // Incrementar contador
        info.count++;
        return true;
    }
    
    /**
     * Obtiene el tiempo restante hasta que se pueda intentar de nuevo
     * @param key Clave única
     * @return Tiempo en milisegundos, 0 si está permitido
     */
    public long getRemainingTime(String key) {
        AttemptInfo info = attempts.get(key);
        
        if (info == null) {
            return 0;
        }
        
        long now = System.currentTimeMillis();
        long elapsed = now - info.firstAttempt;
        
        if (elapsed >= WINDOW_MS) {
            return 0;
        }
        
        if (info.count >= MAX_ATTEMPTS) {
            return WINDOW_MS - elapsed;
        }
        
        return 0;
    }
    
    /**
     * Resetea los intentos para una clave (útil después de login exitoso)
     */
    public void reset(String key) {
        attempts.remove(key);
    }
    
    private void cleanExpiredAttempts() {
        long now = System.currentTimeMillis();
        attempts.entrySet().removeIf(entry -> 
            now - entry.getValue().firstAttempt > WINDOW_MS
        );
    }
    
    private static class AttemptInfo {
        int count;
        long firstAttempt;
        
        AttemptInfo(int count, long firstAttempt) {
            this.count = count;
            this.firstAttempt = firstAttempt;
        }
    }
}

