package cl.duoc.biblioteca.conexion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Conexión a la base de datos "biblioteca" usando el patrón Singleton.
 * Garantiza que en todo el programa exista UNA sola instancia de conexión.
 */
public class DatabaseConnection {

    private static final String URL = "jdbc:mysql://localhost:3306/biblioteca"
            + "?useSSL=false&allowPublicKeyRetrieval=true"
            + "&serverTimezone=America/Santiago&characterEncoding=UTF-8";
    private static final String USUARIO = "root";
    private static final String CLAVE = "root1234";

    // (1) La única instancia que va a existir. Parte vacía.
    private static DatabaseConnection instancia;

    private Connection conexion;

    // (2) Constructor PRIVADO: nadie fuera de esta clase puede hacer "new DatabaseConnection()"
    private DatabaseConnection() throws SQLException {
        this.conexion = DriverManager.getConnection(URL, USUARIO, CLAVE);
    }

    // (3) Única puerta de entrada. Si no existe (o se cerró), la crea; si existe, devuelve la misma.
    //     Es synchronized para que dos hilos no creen dos conexiones al mismo tiempo.
    public static synchronized DatabaseConnection getInstance() throws SQLException {
        if (instancia == null || instancia.conexion.isClosed()) {
            instancia = new DatabaseConnection();
        }
        return instancia;
    }

    public Connection getConnection() {
        return conexion;
    }

    public void cerrar() {
        try {
            if (conexion != null && !conexion.isClosed()) {
                conexion.close();
            }
        } catch (SQLException e) {
            System.err.println("Error al cerrar la conexión: " + e.getMessage());
        }
    }
}
