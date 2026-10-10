package cl.duoc.biblioteca.dao;

import cl.duoc.biblioteca.modelo.Categoria;
import cl.duoc.biblioteca.modelo.Libro;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas del CRUD de LibroDAO contra la base de datos.
 *
 * Si MySQL no esta encendido las pruebas se omiten en lugar de fallar.
 *
 * @author Olga Rivas
 * @version 1.0
 */
class LibroDAOTest {

    /** Objeto que se esta probando. */
    private final LibroDAO dao = new LibroDAO();

    /**
     * Comprueba la base y borra restos de pruebas anteriores.
     *
     * @throws SQLException si falla la limpieza
     */
    @BeforeEach
    void preparar() throws SQLException {
        Assumptions.assumeTrue(DatosDePrueba.hayBaseDeDatos(), "se necesita MySQL con la base biblioteca");
        DatosDePrueba.limpiar();
    }

    /**
     * Borra lo que creo la prueba.
     *
     * @throws SQLException si falla la limpieza
     */
    @AfterEach
    void limpiar() throws SQLException {
        if (DatosDePrueba.hayBaseDeDatos()) {
            DatosDePrueba.limpiar();
        }
    }

    /**
     * @param stock stock inicial
     * @return libro de prueba ya guardado en la base
     * @throws SQLException si falla la insercion
     */
    private Libro crearLibroDePrueba(int stock) throws SQLException {
        Libro libro = new Libro(0, "Libro de prueba JUnit", "Autor Prueba", DatosDePrueba.ISBN,
                "Editorial Prueba", stock, new Categoria(DatosDePrueba.ID_CATEGORIA_EJEMPLO, null));
        dao.insertar(libro);
        return libro;
    }

    @Test
    @DisplayName("insertar guarda el libro y le asigna el id de la base")
    void insertarAsignaId() throws SQLException {
        Libro libro = crearLibroDePrueba(3);

        assertTrue(libro.getId() > 0);
        Libro leido = dao.buscarPorId(libro.getId());
        assertNotNull(leido);
        assertEquals("Libro de prueba JUnit", leido.getTitulo());
        assertEquals(3, leido.getStock());
        assertEquals(DatosDePrueba.ID_CATEGORIA_EJEMPLO, leido.getCategoria().getId());
    }

    @Test
    @DisplayName("listar trae los 10 libros de ejemplo con el nombre de su categoria")
    void listarTraeLosLibrosConCategoria() throws SQLException {
        List<Libro> libros = dao.listar();

        assertTrue(libros.size() >= 10);
        for (Libro libro : libros) {
            assertNotNull(libro.getCategoria().getNombre());
        }
    }

    @Test
    @DisplayName("buscar encuentra por parte del titulo o del autor")
    void buscarPorTituloOAutor() throws SQLException {
        crearLibroDePrueba(1);

        assertEquals(1, dao.buscar("prueba JUnit").size());
        assertEquals(1, dao.buscar("Autor Prueba").size());
        assertTrue(dao.buscar("texto que no existe xyz").isEmpty());
    }

    @Test
    @DisplayName("actualizar cambia los datos y el cambio queda guardado")
    void actualizarCambiaLosDatos() throws SQLException {
        Libro libro = crearLibroDePrueba(1);
        libro.setTitulo("Titulo modificado");
        libro.setStock(8);

        assertTrue(dao.actualizar(libro));
        Libro leido = dao.buscarPorId(libro.getId());
        assertEquals("Titulo modificado", leido.getTitulo());
        assertEquals(8, leido.getStock());
    }

    @Test
    @DisplayName("cambiarStock resta y suma, pero nunca deja el stock negativo")
    void cambiarStockNoDejaNegativo() throws SQLException {
        Libro libro = crearLibroDePrueba(1);

        assertTrue(dao.cambiarStock(libro.getId(), -1));
        assertEquals(0, dao.buscarPorId(libro.getId()).getStock());

        assertFalse(dao.cambiarStock(libro.getId(), -1));
        assertEquals(0, dao.buscarPorId(libro.getId()).getStock());

        assertTrue(dao.cambiarStock(libro.getId(), 1));
        assertEquals(1, dao.buscarPorId(libro.getId()).getStock());
    }

    @Test
    @DisplayName("eliminar borra el libro y buscarPorId devuelve null")
    void eliminarBorraElLibro() throws SQLException {
        Libro libro = crearLibroDePrueba(1);

        assertTrue(dao.eliminar(libro.getId()));
        assertNull(dao.buscarPorId(libro.getId()));
        assertFalse(dao.eliminar(libro.getId()));
    }
}
