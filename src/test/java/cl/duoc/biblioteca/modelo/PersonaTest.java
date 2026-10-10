package cl.duoc.biblioteca.modelo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas de la herencia y el polimorfismo de Persona, Usuario y Estudiante.
 *
 * @author Olga Rivas
 * @version 1.0
 */
class PersonaTest {

    @Test
    @DisplayName("Polimorfismo: getTipo responde distinto segun la subclase")
    void getTipoEsPolimorfico() {
        Persona bibliotecaria = new Usuario(1, "Antonia Perez", "12345678-9", "a@correo.cl",
                "clave123", Rol.BIBLIOTECARIO);
        Persona alumno = new Usuario(2, "Carlos Ruiz", "98765432-1", "c@correo.cl",
                "clave123", Rol.ESTUDIANTE);
        Persona estudiante = new Estudiante(1, "Carlos Ruiz", "98765432-1", "3ro Medio A", "c@correo.cl");

        assertEquals("Bibliotecario", bibliotecaria.getTipo());
        assertEquals("Usuario estudiante", alumno.getTipo());
        assertEquals("Estudiante de 3ro Medio A", estudiante.getTipo());
    }

    @Test
    @DisplayName("getResumen, heredado de Persona, usa el getTipo de cada subclase")
    void getResumenUsaElTipoDeLaSubclase() {
        Persona estudiante = new Estudiante(1, "Carlos Ruiz", "98765432-1", "3ro Medio A", "c@correo.cl");

        assertEquals("Estudiante de 3ro Medio A: Carlos Ruiz (98765432-1)", estudiante.getResumen());
    }

    @Test
    @DisplayName("Solo el rol bibliotecario tiene gestion total")
    void soloElBibliotecarioGestiona() {
        assertTrue(new Usuario(1, "A", "1-9", "a@c.cl", "x", Rol.BIBLIOTECARIO).esBibliotecario());
        assertFalse(new Usuario(2, "B", "2-7", "b@c.cl", "x", Rol.ESTUDIANTE).esBibliotecario());
    }

    @Test
    @DisplayName("Rol convierte el texto de la base de datos, sin importar mayusculas")
    void rolSeConvierteDesdeLaBase() {
        assertEquals(Rol.BIBLIOTECARIO, Rol.desdeBD("bibliotecario"));
        assertEquals(Rol.ESTUDIANTE, Rol.desdeBD("ESTUDIANTE"));
        assertThrows(IllegalArgumentException.class, () -> Rol.desdeBD("director"));
    }
}
