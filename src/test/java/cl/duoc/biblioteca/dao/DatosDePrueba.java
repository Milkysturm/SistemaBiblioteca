package cl.duoc.biblioteca.dao;

import cl.duoc.biblioteca.conexion.DatabaseConnection;

import java.sql.SQLException;
import java.sql.Statement;

/**
 * Apoyo de las pruebas que necesitan la base de datos.
 *
 * Las pruebas crean sus propios registros, marcados con ISBN "TEST-", RUT
 * "9999999..." o nombre "Categoria JUnit", y los borran al terminar. Asi nunca
 * tocan los datos de ejemplo de la biblioteca.
 *
 * @author Olga Rivas
 * @version 1.0
 */
public final class DatosDePrueba {

    /** ISBN de los libros de prueba. */
    public static final String ISBN = "TEST-0001";

    /** RUT de los estudiantes de prueba. */
    public static final String RUT = "99999999-9";

    /** Correo de las cuentas de prueba. */
    public static final String CORREO = "prueba.junit@correo.cl";

    /** Nombre de las categorias de prueba. */
    public static final String CATEGORIA = "Categoria JUnit";

    /** Id de una categoria que existe en los datos de ejemplo (Ciencia Ficcion). */
    public static final int ID_CATEGORIA_EJEMPLO = 1;

    /** Constructor privado: es una clase de utilidad y no se instancia. */
    private DatosDePrueba() {
    }

    /**
     * @return true si MySQL responde; si no, las pruebas de base de datos se omiten
     */
    public static boolean hayBaseDeDatos() {
        try {
            DatabaseConnection.comprobarConexion();
            new PrestamoDAO().asegurarColumnaFechaEntrega();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Borra todo lo que hayan creado las pruebas, en orden para respetar las claves foraneas.
     *
     * @throws SQLException si falla alguna sentencia
     */
    public static void limpiar() throws SQLException {
        try (Statement sentencia = DatabaseConnection.getInstance().getConnection().createStatement()) {
            sentencia.executeUpdate("DELETE FROM prestamos WHERE "
                    + "id_libro IN (SELECT id FROM libros WHERE isbn LIKE 'TEST-%') "
                    + "OR id_estudiante IN (SELECT id FROM estudiantes WHERE rut LIKE '9999999%')");
            sentencia.executeUpdate("DELETE FROM libros WHERE isbn LIKE 'TEST-%'");
            sentencia.executeUpdate("DELETE FROM estudiantes WHERE rut LIKE '9999999%'");
            sentencia.executeUpdate("DELETE FROM usuarios WHERE rut LIKE '9999999%'");
            sentencia.executeUpdate("DELETE FROM categorias WHERE nombre LIKE 'Categoria JUnit%'");
        }
    }
}
