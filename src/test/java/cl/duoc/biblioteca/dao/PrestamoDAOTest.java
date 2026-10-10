package cl.duoc.biblioteca.dao;

import cl.duoc.biblioteca.modelo.Categoria;
import cl.duoc.biblioteca.modelo.Estudiante;
import cl.duoc.biblioteca.modelo.Libro;
import cl.duoc.biblioteca.modelo.Prestamo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas de PrestamoDAO con un libro y un estudiante creados para la prueba.
 *
 * @author Olga Rivas
 * @version 1.0
 */
class PrestamoDAOTest {

    /** Objeto que se esta probando. */
    private final PrestamoDAO dao = new PrestamoDAO();

    /** Libro creado para la prueba. */
    private Libro libro;

    /** Estudiante creado para la prueba. */
    private Estudiante estudiante;

    /**
     * Crea un libro y un estudiante de prueba.
     *
     * @throws SQLException si falla la preparacion
     */
    @BeforeEach
    void preparar() throws SQLException {
        Assumptions.assumeTrue(DatosDePrueba.hayBaseDeDatos(), "se necesita MySQL con la base biblioteca");
        DatosDePrueba.limpiar();

        libro = new Libro(0, "Libro de prueba JUnit", "Autor Prueba", DatosDePrueba.ISBN,
                "Editorial Prueba", 2, new Categoria(DatosDePrueba.ID_CATEGORIA_EJEMPLO, null));
        new LibroDAO().insertar(libro);

        estudiante = new Estudiante(0, "Estudiante Prueba", DatosDePrueba.RUT, "1ro Medio A",
                DatosDePrueba.CORREO);
        new EstudianteDAO().insertar(estudiante);
    }

    /**
     * @throws SQLException si falla la limpieza
     */
    @AfterEach
    void limpiar() throws SQLException {
        if (DatosDePrueba.hayBaseDeDatos()) {
            DatosDePrueba.limpiar();
        }
    }

    @Test
    @DisplayName("insertar guarda el prestamo y aparece en el historial del estudiante")
    void insertarApareceEnElHistorial() throws SQLException {
        Prestamo prestamo = new Prestamo(estudiante.getId(), libro.getId());

        assertTrue(dao.insertar(prestamo));
        assertTrue(prestamo.getId() > 0);

        List<Prestamo> historial = dao.listarPorEstudiante(estudiante.getId());
        assertEquals(1, historial.size());
        assertEquals("Libro de prueba JUnit", historial.get(0).getTituloLibro());
        assertEquals(prestamo.getFechaDevolucion(), historial.get(0).getFechaDevolucion());
    }

    @Test
    @DisplayName("marcarDevuelto cierra el prestamo una sola vez y guarda la fecha de entrega")
    void marcarDevueltoSoloUnaVez() throws SQLException {
        Prestamo prestamo = new Prestamo(estudiante.getId(), libro.getId());
        dao.insertar(prestamo);

        assertTrue(dao.marcarDevuelto(prestamo.getId(), LocalDate.now()));
        assertFalse(dao.marcarDevuelto(prestamo.getId(), LocalDate.now()));

        Prestamo leido = dao.buscarPorId(prestamo.getId());
        assertTrue(leido.isDevuelto());
        assertEquals(LocalDate.now(), leido.getFechaEntrega());
    }

    @Test
    @DisplayName("Un prestamo devuelto tarde queda registrado con sus dias de atraso")
    void devolucionTardiaQuedaRegistrada() throws SQLException {
        LocalDate hace20 = LocalDate.now().minusDays(20);
        Prestamo prestamo = new Prestamo(0, estudiante.getId(), libro.getId(), hace20,
                Prestamo.calcularVencimiento(hace20), false);
        dao.insertar(prestamo);

        dao.marcarDevuelto(prestamo.getId(), LocalDate.now());

        Prestamo leido = dao.buscarPorId(prestamo.getId());
        assertEquals(13, leido.getDiasAtraso());
        assertEquals("Devuelto con 13 dia(s) de atraso", leido.getEstado());
    }

    @Test
    @DisplayName("Un prestamo activo aparece en listarActivos y deja de aparecer al devolverlo")
    void listarActivosSoloLosNoDevueltos() throws SQLException {
        Prestamo prestamo = new Prestamo(estudiante.getId(), libro.getId());
        dao.insertar(prestamo);
        int id = prestamo.getId();

        assertTrue(dao.listarActivos().stream().anyMatch(p -> p.getId() == id));
        dao.marcarDevuelto(id, LocalDate.now());
        assertFalse(dao.listarActivos().stream().anyMatch(p -> p.getId() == id));
    }

    @Test
    @DisplayName("contarPorLibro y contarPorEstudiante cuentan los prestamos registrados")
    void contarPrestamos() throws SQLException {
        assertEquals(0, dao.contarPorLibro(libro.getId()));

        dao.insertar(new Prestamo(estudiante.getId(), libro.getId()));

        assertEquals(1, dao.contarPorLibro(libro.getId()));
        assertEquals(1, dao.contarPorEstudiante(estudiante.getId()));
    }
}
