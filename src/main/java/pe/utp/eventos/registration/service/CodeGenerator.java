package pe.utp.eventos.registration.service;

import java.security.SecureRandom;

public final class CodeGenerator {

    private static final SecureRandom RND = new SecureRandom();

    private CodeGenerator() {
        // constructor privado para que no se pueda instanciar
    }

    public static String numeric6() {
        int n = RND.nextInt(1_000_000); // genera un número entre 0 y 999999
        return String.format("%06d", n); // lo formatea con ceros a la izquierda (ej: 000123)
    }
}
