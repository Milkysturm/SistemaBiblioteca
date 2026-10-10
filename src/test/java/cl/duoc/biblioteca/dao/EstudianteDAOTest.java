package cl.duoc.biblioteca.dao;

import cl.duoc.biblioteca.modelo.Estudiante;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas del CRUD de EstudianteDAO contra la base de datos.
 *
 * @author Olga Rivas
 * @version 1.0
 */
class EstudianteDAOTest {

    /** Objeto que se esta probando. */
    private final EstudianteDAO dao = new EstudianteDAO();

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
    @DisplayName("CRUD completo: insertar, buscar por RUT, actualizar y eliminar")
    void crudCompleto() throws SQLException {
        Estudiante estudiante = new Estudiante(0, "Estudiante Prueba", DatosDePrueba.RUT,
                "1ro Medio A", DatosDePrueba.CORREO);

        assertTrue(dao.insertar(estudiante));
        assertTrue(estudiante.getId() > 0);

        Estudiante leido = dao.buscarPorRut(DatosDePrueba.RUT);
        assertNotNull(leido);
        assertEquals(estudiante.getId(), leido.getId());

        estudiante.setCurso("2do Medio B");
        assertTrue(dao.actualizar(estudiante));
        assertEquals("2do Medio B", dao.buscarPorId(estudiante.getId()).getCurso());

        assertTrue(dao.eliminar(estudiante.getId()));
        assertNull(dao.buscarPorId(estudiante.getId()));
    }

    @Test
    @DisplayName("buscarPorRut relaciona al usuario Carlos Ruiz con su ficha de estudiante")
    void buscarPorRutDeLosDatosDeEjemplo() throws SQLException {
        Estudiante carlos = dao.buscarPorRut("98765432-1");

        assertNotNull(carlos);
        assertEquals("Carlos Ruiz", carlos.getNombre());
        assertEquals("3ro Medio A", carlos.getCurso());
    }
}
