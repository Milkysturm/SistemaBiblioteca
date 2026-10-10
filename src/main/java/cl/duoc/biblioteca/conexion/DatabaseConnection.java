package cl.duoc.biblioteca.conexion;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Conexion a la base de datos biblioteca usando el patron Singleton.
 *
 * Garantiza que en todo el programa exista una sola instancia de conexion.
 * Los datos de acceso se leen de db.properties, asi la contrasena no queda
 * escrita en el codigo ni se sube al repositorio.
 *
 * @author Olga Rivas
 * @version 1.0
 */
public final class DatabaseConnection {

    /** Archivo con los datos de conexion, ubicado en src/main/resources. */
    private static final String ARCHIVO = "db.properties";

    /** Direccion de la base si no hay archivo de configuracion. */
    private static final String URL_POR_DEFECTO = "jdbc:mysql://localhost:3306/biblioteca"
            + "?useSSL=false&allowPublicKeyRetrieval=true"
            + "&serverTimezone=America/Santiago&characterEncoding=UTF-8";

    /** Usuario de MySQL si no hay archivo de configuracion. */
    private static final String USUARIO_POR_DEFECTO = "root";

    /** Contrasena si no hay archivo de configuracion. */
    private static final String CLAVE_POR_DEFECTO = "";

    /** Segundos de espera al comprobar si la conexion sigue viva. */
    private static final int SEGUNDOS_VALIDACION = 2;

    /** La unica instancia de la clase (patron Singleton). */
    private static DatabaseConnection instancia;

    /** Conexion real con MySQL. */
    private final Connection conexion;

    /**
     * Constructor privado: nadie fuera de esta clase puede crear otra instancia.
     *
     * @throws SQLException si no se puede conectar
     */
    private DatabaseConnection() throws SQLException {
        Properties datos = leerConfiguracion();
        this.conexion = DriverManager.getConnection(
                datos.getProperty("url", URL_POR_DEFECTO),
                datos.getProperty("usuario", USUARIO_POR_DEFECTO),
                datos.getProperty("clave", CLAVE_POR_DEFECTO));
    }

    /**
     * Devuelve la unica instancia, creandola si no existe o si la conexion se perdio.
     *
     * Es synchronized para que dos hilos no creen dos conexiones al mismo tiempo.
     *
     * @return la instancia unica
     * @throws SQLException si no se puede conectar
     */
    public static synchronized DatabaseConnection getInstance() throws SQLException {
        if (instancia == null || !instancia.conexion.isValid(SEGUNDOS_VALIDACION)) {
            instancia = new DatabaseConnection();
        }
        return instancia;
    }

    /**
     * @return la conexion abierta con MySQL
     */
    public Connection getConnection() {
        return conexion;
    }

    /**
     * Comprueba que la base de datos responda. Se usa al iniciar la aplicacion.
     *
     * @throws SQLException si no se puede conectar
     */
    public static void comprobarConexion() throws SQLException {
        getInstance();
    }

    /**
     * Cierra la conexion. Se llama al salir de la aplicacion.
     */
    public static synchronized void cerrar() {
        if (instancia == null) {
            return;
        }
        try {
            instancia.conexion.close();
        } catch (SQLException e) {
            System.err.println("Error al cerrar la conexion: " + e.getMessage());
        }
        instancia = null;
    }

    /**
     * @return direccion de la base de datos que se esta usando
     */
    public static String getUrl() {
        return leerConfiguracion().getProperty("url", URL_POR_DEFECTO);
    }

    /**
     * Lee db.properties desde el classpath.
     *
     * @return datos de conexion, vacios si no existe el archivo
     */
    private static Properties leerConfiguracion() {
        Properties datos = new Properties();
        try (InputStream entrada = DatabaseConnection.class.getClassLoader().getResourceAsStream(ARCHIVO)) {
            if (entrada != null) {
                datos.load(entrada);
            }
        } catch (IOException e) {
            System.err.println("No se pudo leer " + ARCHIVO + ": " + e.getMessage());
        }
        return datos;
    }
}
