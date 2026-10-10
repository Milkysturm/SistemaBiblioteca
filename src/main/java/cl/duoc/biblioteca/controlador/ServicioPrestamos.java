package cl.duoc.biblioteca.controlador;

import cl.duoc.biblioteca.conexion.DatabaseConnection;
import cl.duoc.biblioteca.dao.EstudianteDAO;
import cl.duoc.biblioteca.dao.LibroDAO;
import cl.duoc.biblioteca.dao.PrestamoDAO;
import cl.duoc.biblioteca.modelo.Libro;
import cl.duoc.biblioteca.modelo.Prestamo;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;

/**
 * Operaciones de prestamo y devolucion, que son las que modifican el stock.
 *
 * Sus metodos son synchronized: si varios hilos piden prestado el mismo libro al
 * mismo tiempo, entran de a uno. Asi la revision del stock, el descuento y el
 * registro del prestamo ocurren juntos y nunca se presta un ejemplar que no hay
 * (condicion de carrera). La aplicacion usa una sola instancia de esta clase.
 *
 * @author Olga Rivas
 * @version 1.0
 */
public class ServicioPrestamos {

    /** Acceso a la tabla libros. */
    private final LibroDAO libroDAO = new LibroDAO();

    /** Acceso a la tabla prestamos. */
    private final PrestamoDAO prestamoDAO = new PrestamoDAO();

    /** Acceso a la tabla estudiantes. */
    private final EstudianteDAO estudianteDAO = new EstudianteDAO();

    /**
     * Pasos de base de datos que deben guardarse todos juntos o ninguno.
     */
    @FunctionalInterface
    private interface Pasos {

        /**
         * @throws SQLException si falla algun paso
         */
        void ejecutar() throws SQLException;
    }

    /**
     * Registra un prestamo: verifica la disponibilidad, descuenta el stock y guarda
     * el prestamo, que queda en el historial del estudiante.
     *
     * @param idEstudiante estudiante que pide el libro
     * @param idLibro      libro solicitado
     * @return el prestamo registrado, con su fecha de vencimiento
     * @throws SQLException          si falla la base de datos (no se guarda nada)
     * @throws IllegalStateException si no hay stock o el prestamo no esta permitido
     */
    public synchronized Prestamo registrarPrestamo(int idEstudiante, int idLibro) throws SQLException {
        if (estudianteDAO.buscarPorId(idEstudiante) == null) {
            throw new IllegalStateException("El estudiante ya no existe en el sistema.");
        }
        Libro libro = libroDAO.buscarPorId(idLibro);
        if (libro == null) {
            throw new IllegalStateException("El libro ya no existe en el sistema.");
        }
        if (!libro.hayStock()) {
            throw new IllegalStateException("No quedan ejemplares disponibles de \"" + libro.getTitulo() + "\".");
        }
        if (prestamoDAO.tienePrestamoActivo(idEstudiante, idLibro)) {
            throw new IllegalStateException("El estudiante ya tiene \"" + libro.getTitulo()
                    + "\" prestado y aun no lo devuelve.");
        }

        Prestamo prestamo = new Prestamo(idEstudiante, idLibro);
        ejecutarEnTransaccion(() -> {
            if (!libroDAO.cambiarStock(idLibro, -1)) {
                throw new IllegalStateException("No quedan ejemplares disponibles de \"" + libro.getTitulo() + "\".");
            }
            prestamoDAO.insertar(prestamo);
        });
        prestamo.setTituloLibro(libro.getTitulo());
        return prestamo;
    }

    /**
     * Registra la devolucion: marca el prestamo como devuelto con la fecha de hoy
     * (que deja constancia del atraso, si lo hay) y devuelve el ejemplar al stock.
     *
     * @param idPrestamo prestamo a cerrar
     * @return el prestamo cerrado, con su fecha de entrega y dias de atraso
     * @throws SQLException          si falla la base de datos (no se guarda nada)
     * @throws IllegalStateException si el prestamo no existe o ya estaba devuelto
     */
    public synchronized Prestamo registrarDevolucion(int idPrestamo) throws SQLException {
        Prestamo prestamo = prestamoDAO.buscarPorId(idPrestamo);
        if (prestamo == null) {
            throw new IllegalStateException("El prestamo ya no existe en el sistema.");
        }
        if (prestamo.isDevuelto()) {
            throw new IllegalStateException("Este prestamo ya estaba devuelto.");
        }

        LocalDate hoy = LocalDate.now();
        ejecutarEnTransaccion(() -> {
            if (!prestamoDAO.marcarDevuelto(idPrestamo, hoy)) {
                throw new IllegalStateException("Este prestamo ya estaba devuelto.");
            }
            libroDAO.cambiarStock(prestamo.getIdLibro(), 1);
        });
        prestamo.setDevuelto(true);
        prestamo.setFechaEntrega(hoy);
        return prestamo;
    }

    /**
     * Ejecuta los pasos como una transaccion: si uno falla, se deshacen todos.
     *
     * @param pasos operaciones de base de datos a ejecutar
     * @throws SQLException si falla algun paso o la confirmacion
     */
    private void ejecutarEnTransaccion(Pasos pasos) throws SQLException {
        Connection conexion = DatabaseConnection.getInstance().getConnection();
        conexion.setAutoCommit(false);
        try {
            pasos.ejecutar();
            conexion.commit();
        } catch (SQLException | RuntimeException e) {
            conexion.rollback();
            throw e;
        } finally {
            conexion.setAutoCommit(true);
        }
    }
}
