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
            if (hayBaseDeDatos()) {
                new VentanaLogin(new ControladorBiblioteca());
            }
        });
    }

    /**
     * Comprueba la conexion y avisa si no hay base de datos disponible.
     *
     * @return true si se puede trabajar
     */
    private static boolean hayBaseDeDatos() {
        try {
            DatabaseConnection.comprobarConexion();
            return true;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                    "No se pudo conectar con la base de datos.\n\n"
                            + "Direccion: " + DatabaseConnection.getUrl() + "\n"
                            + "Detalle: " + e.getMessage() + "\n\n"
                            + "Revisa que MySQL este encendido y que los datos de\n"
                            + "src/main/resources/db.properties sean los correctos.",
                    "Sin conexion a la base de datos", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
}
