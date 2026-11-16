package conexion;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Conexión a SQL Server.
 * El servidor, el usuario y la contraseña se leen de config.properties (en la carpeta del
 * proyecto), que no se sube al repositorio. Ver config.example.properties.
 */
public class Conexion {

    private static final String ARCHIVO = "config.properties";

    public Connection conectar() {

        Connection con = null;

        try {

            Properties config = leerConfiguracion();

            con = DriverManager.getConnection(
                    config.getProperty("db.url"),
                    config.getProperty("db.user"),
                    config.getProperty("db.password"));

            System.out.println("Conexión exitosa");

        } catch (IOException e) {

            System.out.println("Falta el archivo " + ARCHIVO
                    + ": copia config.example.properties con ese nombre y pon tus datos. (" + e.getMessage() + ")");

        } catch (SQLException e) {

            System.out.println("Error de conexión: " + e.getMessage());

        }

        return con;
    }

    private Properties leerConfiguracion() throws IOException {
        Properties config = new Properties();
        try (InputStream in = new FileInputStream(ARCHIVO)) {
            config.load(in);
        }
        return config;
    }

}
