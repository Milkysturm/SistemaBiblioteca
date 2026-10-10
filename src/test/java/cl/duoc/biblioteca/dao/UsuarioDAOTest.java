package cl.duoc.biblioteca.dao;

import cl.duoc.biblioteca.modelo.Rol;
import cl.duoc.biblioteca.modelo.Usuario;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Pruebas de la autenticacion con UsuarioDAO, usando los datos de ejemplo.
 *
 * @author Olga Rivas
 * @version 1.0
 */
class UsuarioDAOTest {

    /** Objeto que se esta probando. */
    private final UsuarioDAO dao = new UsuarioDAO();

    /**
     * Omite las pruebas si no hay base de datos.
     */
    @BeforeEach
    void preparar() {
        Assumptions.assumeTrue(DatosDePrueba.hayBaseDeDatos(), "se necesita MySQL con la base biblioteca");
    }

    @Test
    @DisplayName("autenticar con datos correctos devuelve el usuario con su rol")
    void autenticarConDatosCorrectos() throws SQLException {
        Usuario antonia = dao.autenticar("antonia@correo.cl", "clave123");

        assertNotNull(antonia);
        assertEquals("Antonia P\u00e9rez", antonia.getNombre());
        assertEquals(Rol.BIBLIOTECARIO, antonia.getRol());
    }

    @Test
    @DisplayName("autenticar con la contrasena incorrecta devuelve null")
    void autenticarConClaveIncorrecta() throws SQLException {
        assertNull(dao.autenticar("antonia@correo.cl", "otraClave"));
    }

    @Test
    @DisplayName("autenticar con un correo que no existe devuelve null")
    void autenticarConCorreoInexistente() throws SQLException {
        assertNull(dao.autenticar("nadie@correo.cl", "clave123"));
    }

    @Test
    @DisplayName("Un usuario estudiante se reconoce por su rol")
    void usuarioEstudianteTieneRolEstudiante() throws SQLException {
        Usuario carlos = dao.autenticar("carlos@correo.cl", "clave123");

        assertNotNull(carlos);
        assertEquals(Rol.ESTUDIANTE, carlos.getRol());
    }
}
