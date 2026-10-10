package cl.duoc.biblioteca;

import cl.duoc.biblioteca.conexion.DatabaseConnection;
import cl.duoc.biblioteca.controlador.ControladorBiblioteca;
import cl.duoc.biblioteca.vista.VentanaLogin;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.sql.SQLException;

/**
 * Punto de entrada del Sistema de Gestion de Biblioteca Escolar.
 *
 * Comprueba que la base de datos responda y abre la ventana de inicio de sesion.
 * La interfaz se crea con SwingUtilities.invokeLater porque Swing debe usarse
 * desde su propio hilo grafico.
 *
 * @author Olga Rivas
 * @version 1.0
 */
public class Main {

    /**
     * Inicia la aplicacion.
     *
     * @param args no se utilizan
     */
    public static void main(String[] args) {
        // Al cerrar el programa, por cualquier via, se cierra la conexion unica.
        Runtime.getRuntime().addShutdownHook(new Thread(DatabaseConnection::cerrar));

        SwingUtilities.invokeLater(() -> {
            ControladorBiblioteca controlador = new ControladorBiblioteca();
            if (hayBaseDeDatos(controlador)) {
                new VentanaLogin(controlador);
            }
        });
    }

    /**
     * Comprueba la conexion, prepara la base y avisa si no hay base de datos disponible.
     *
     * @param controlador controlador del sistema
     * @return true si se puede trabajar
     */
    private static boolean hayBaseDeDatos(ControladorBiblioteca controlador) {
        try {
            DatabaseConnection.comprobarConexion();
            controlador.prepararBaseDeDatos();
            return true;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                    "No se pudo conectar con la base de datos.\n\n"
                            + "Direccion: " + DatabaseConnection.getUrl() + "\n"
                            + "Detalle: " + e.getMessage() + "\n\n"
                            + "Revisa que MySQL este encendido, que existan las tablas de la carpeta sql\n"
                            + "y que los datos de db.properties sean los correctos (en src/main/resources\n"
                            + "o en la misma carpeta desde donde se ejecuta el programa).",
                    "Sin conexion a la base de datos", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
}
