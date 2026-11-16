package seguridad;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Cifrado de contraseñas con PBKDF2 (HMAC-SHA256) y sal aleatoria.
 * En usuarios.txt no se guarda la contraseña, sino su resumen:
 *   usuario,pbkdf2$iteraciones$sal$resumen
 */
public class Claves {

    private static final String ALGORITMO = "PBKDF2WithHmacSHA256";
    private static final int ITERACIONES = 120000;
    private static final int BITS = 256;

    /** Devuelve el texto que se guarda en usuarios.txt para una contraseña. */
    public static String cifrar(String contrasena) {
        byte[] sal = new byte[16];
        new SecureRandom().nextBytes(sal);
        byte[] resumen = derivar(contrasena.toCharArray(), sal, ITERACIONES);
        Base64.Encoder b64 = Base64.getEncoder();
        return "pbkdf2$" + ITERACIONES + "$" + b64.encodeToString(sal) + "$" + b64.encodeToString(resumen);
    }

    /** Comprueba una contraseña escrita contra lo guardado en usuarios.txt. */
    public static boolean verificar(String contrasena, String guardado) {
        try {
            String[] p = guardado.trim().split("\\$");
            if (p.length != 4 || !p[0].equals("pbkdf2")) {
                return false;
            }
            byte[] sal = Base64.getDecoder().decode(p[2]);
            byte[] esperado = Base64.getDecoder().decode(p[3]);
            byte[] calculado = derivar(contrasena.toCharArray(), sal, Integer.parseInt(p[1]));
            return MessageDigest.isEqual(esperado, calculado);
        } catch (RuntimeException e) {
            return false;
        }
    }

    private static byte[] derivar(char[] contrasena, byte[] sal, int iteraciones) {
        try {
            PBEKeySpec spec = new PBEKeySpec(contrasena, sal, iteraciones, BITS);
            return SecretKeyFactory.getInstance(ALGORITMO).generateSecret(spec).getEncoded();
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo cifrar la contraseña", e);
        }
    }

    /**
     * Para crear un usuario nuevo: ejecuta esta clase con el usuario y la contraseña
     * y copia la línea que imprime a usuarios.txt.
     */
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Uso: java seguridad.Claves <usuario> <contraseña>");
            return;
        }
        System.out.println(args[0] + "," + cifrar(args[1]));
    }
}
