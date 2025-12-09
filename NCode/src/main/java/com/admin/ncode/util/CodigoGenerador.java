package com.admin.ncode.util;

import java.security.SecureRandom;

public class CodigoGenerador {

    private static final String MAYUSCULAS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String MINUSCULAS = "abcdefghijklmnopqrstuvwxyz";
    private static final String NUMEROS = "0123456789";
    private static final String ESPECIALES = "!@#$%^&*";
    private static final String TODOS_CARACTERES = MAYUSCULAS + MINUSCULAS + NUMEROS + ESPECIALES;
    
    private static final SecureRandom random = new SecureRandom();

    /**
     * Genera un código de 12 caracteres que incluye:
     * - Al menos una mayúscula
     * - Al menos una minúscula
     * - Al menos un número
     * - Al menos un carácter especial
     */
    public static String generarCodigo() {
        StringBuilder codigo = new StringBuilder(12);
        
        // Asegurar al menos un carácter de cada tipo
        codigo.append(MAYUSCULAS.charAt(random.nextInt(MAYUSCULAS.length())));
        codigo.append(MINUSCULAS.charAt(random.nextInt(MINUSCULAS.length())));
        codigo.append(NUMEROS.charAt(random.nextInt(NUMEROS.length())));
        codigo.append(ESPECIALES.charAt(random.nextInt(ESPECIALES.length())));
        
        // Completar los 8 caracteres restantes de forma aleatoria
        for (int i = 4; i < 12; i++) {
            codigo.append(TODOS_CARACTERES.charAt(random.nextInt(TODOS_CARACTERES.length())));
        }
        
        // Mezclar los caracteres para que no estén siempre en el mismo orden
        return mezclarCaracteres(codigo.toString());
    }
    
    /**
     * Mezcla los caracteres del código de forma aleatoria
     */
    private static String mezclarCaracteres(String codigo) {
        char[] caracteres = codigo.toCharArray();
        for (int i = caracteres.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char temp = caracteres[i];
            caracteres[i] = caracteres[j];
            caracteres[j] = temp;
        }
        return new String(caracteres);
    }
}

