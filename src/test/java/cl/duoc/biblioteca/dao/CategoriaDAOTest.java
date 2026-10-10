package cl.duoc.biblioteca.dao;

import cl.duoc.biblioteca.modelo.Categoria;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas del CRUD de CategoriaDAO contra la base de datos.
 *
 * @author Olga Rivas
 * @version 1.0
 */
class CategoriaDAOTest {

    /** Objeto que se esta probando. */
    private final CategoriaDAO dao = new CategoriaDAO();

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
    @DisplayName("CRUD completo: insertar, buscar, actualizar y eliminar")
    void crudCompleto() throws SQLException {
        Categoria categoria = new Categoria(0, DatosDePrueba.CATEGORIA);

        assertTrue(dao.insertar(categoria));
        assertTrue(categoria.getId() > 0);
        assertEquals(DatosDePrueba.CATEGORIA, dao.buscarPorId(categoria.getId()).getNombre());

        categoria.setNombre(DatosDePrueba.CATEGORIA + " modificada");
        assertTrue(dao.actualizar(categoria));
        assertEquals(DatosDePrueba.CATEGORIA + " modificada", dao.buscarPorId(categoria.getId()).getNombre());

        assertTrue(dao.eliminar(categoria.getId()));
        assertNull(dao.buscarPorId(categoria.getId()));
    }

    @Test
    @DisplayName("listar trae las 5 categorias de ejemplo")
    void listarTraeLasCategorias() throws SQLException {
        assertEquals(5, dao.listar().size());
    }
}
