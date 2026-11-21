package seguridad;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class Claves {

    private static final String ALGORITMO = "PBKDF2WithHmacSHA256";
    private static final int ITERACIONES = 120000;
    private static final int BITS = 256;

    public static String cifrar(String contrasena) {
        byte[] sal = new byte[16];
        new SecureRandom().nextBytes(sal);
        byte[] resumen = derivar(contrasena.toCharArray(), sal, ITERACIONES);
        Base64.Encoder b64 = Base64.getEncoder();
        return "pbkdf2$" + ITERACIONES + "$" + b64.encodeToString(sal) + "$" + b64.encodeToString(resumen);
    }

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

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Uso: java seguridad.Claves <usuario> <contraseña>");
            return;
        }
        System.out.println(args[0] + "," + cifrar(args[1]));
    }
}
