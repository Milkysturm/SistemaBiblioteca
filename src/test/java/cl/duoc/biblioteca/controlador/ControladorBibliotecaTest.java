package cl.duoc.biblioteca.controlador;

import cl.duoc.biblioteca.dao.DatosDePrueba;
import cl.duoc.biblioteca.modelo.Categoria;
import cl.duoc.biblioteca.modelo.Estudiante;
import cl.duoc.biblioteca.modelo.Libro;
import cl.duoc.biblioteca.modelo.Usuario;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas de las reglas de negocio del controlador.
 *
 * @author Olga Rivas
 * @version 1.0
 */
class ControladorBibliotecaTest {

    /** Id del libro "Dune" en los datos de ejemplo, que tiene un prestamo. */
    private static final int ID_LIBRO_CON_PRESTAMO = 1;

    /** Id de Carlos Ruiz en los datos de ejemplo, que tiene un prestamo. */
    private static final int ID_ESTUDIANTE_CON_PRESTAMO = 1;

    /** ISBN de "Dune" en los datos de ejemplo. */
    private static final String ISBN_EXISTENTE = "9780441013593";

    /** Objeto que se esta probando. */
    private final ControladorBiblioteca controlador = new ControladorBiblioteca();

    /**
     * @throws SQLException si falla la limpieza
     */
    @BeforeEach
    void preparar() throws SQLException {
        Assumptions.assumeTrue(DatosDePrueba.hayBaseDeDatos(), "se necesita MySQL con la base biblioteca");
        DatosDePrueba.limpiar();
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
    @DisplayName("No se puede registrar un libro con un ISBN que ya existe")
    void isbnRepetidoSeRechaza() {
        Libro repetido = new Libro(0, "Copia", "Autor", ISBN_EXISTENTE, "Editorial", 1,
                new Categoria(DatosDePrueba.ID_CATEGORIA_EJEMPLO, null));

        assertThrows(IllegalStateException.class, () -> controlador.crearLibro(repetido));
    }

    @Test
    @DisplayName("No se puede eliminar un libro que tiene prestamos en su historial")
    void libroConPrestamosNoSeElimina() {
        assertThrows(IllegalStateException.class, () -> controlador.eliminarLibro(ID_LIBRO_CON_PRESTAMO));
    }

    @Test
    @DisplayName("No se puede eliminar un estudiante que tiene prestamos en su historial")
    void estudianteConPrestamosNoSeElimina() {
        assertThrows(IllegalStateException.class,
                () -> controlador.eliminarEstudiante(ID_ESTUDIANTE_CON_PRESTAMO));
    }

    @Test
    @DisplayName("Registrar un estudiante con contrasena le crea una cuenta para iniciar sesion")
    void estudianteConClavePuedeIniciarSesion() throws SQLException {
        Estudiante estudiante = new Estudiante(0, "Estudiante Prueba", DatosDePrueba.RUT,
                "1ro Medio A", DatosDePrueba.CORREO);

        controlador.crearEstudiante(estudiante, "claveJUnit");

        Usuario cuenta = controlador.iniciarSesion(DatosDePrueba.CORREO, "claveJUnit");
        assertNotNull(cuenta);
        assertEquals(DatosDePrueba.RUT, cuenta.getRut());
        assertTrue(controlador.rutsConCuenta().contains(DatosDePrueba.RUT));
        assertEquals(estudiante.getId(), controlador.fichaDe(cuenta).getId());
    }

    @Test
    @DisplayName("Eliminar un estudiante sin prestamos borra tambien su cuenta")
    void eliminarEstudianteBorraSuCuenta() throws SQLException {
        Estudiante estudiante = new Estudiante(0, "Estudiante Prueba", DatosDePrueba.RUT,
                "1ro Medio A", DatosDePrueba.CORREO);
        controlador.crearEstudiante(estudiante, "claveJUnit");

        assertTrue(controlador.eliminarEstudiante(estudiante.getId()));
        assertNull(controlador.iniciarSesion(DatosDePrueba.CORREO, "claveJUnit"));
    }

    @Test
    @DisplayName("No se puede registrar un estudiante con un RUT que ya existe")
    void rutRepetidoSeRechaza() {
        Estudiante repetido = new Estudiante(0, "Otro", "98765432-1", "1ro Medio A", "otro@correo.cl");

        assertThrows(IllegalStateException.class, () -> controlador.crearEstudiante(repetido, ""));
    }
}
