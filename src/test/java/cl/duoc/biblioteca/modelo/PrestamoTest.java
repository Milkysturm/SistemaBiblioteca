package cl.duoc.biblioteca.modelo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas del calculo de vencimiento y de atraso de un prestamo.
 *
 * @author Olga Rivas
 * @version 1.0
 */
class PrestamoTest {

    @Test
    @DisplayName("Un prestamo nuevo parte hoy y vence en 7 dias")
    void prestamoNuevoVenceEnSieteDias() {
        Prestamo prestamo = new Prestamo(1, 1);

        assertEquals(LocalDate.now(), prestamo.getFechaPrestamo());
        assertEquals(LocalDate.now().plusDays(Prestamo.DIAS_PRESTAMO), prestamo.getFechaDevolucion());
        assertFalse(prestamo.isDevuelto());
    }

    @Test
    @DisplayName("Un prestamo nuevo no esta atrasado")
    void prestamoNuevoNoEstaAtrasado() {
        Prestamo prestamo = new Prestamo(1, 1);

        assertFalse(prestamo.estaAtrasado());
        assertEquals(0, prestamo.getDiasAtraso());
    }

    @Test
    @DisplayName("Si vencio hace 3 dias y no se devolvio, tiene 3 dias de atraso")
    void prestamoVencidoTieneDiasDeAtraso() {
        LocalDate hoy = LocalDate.now();
        Prestamo prestamo = new Prestamo(1, 1, 1, hoy.minusDays(10), hoy.minusDays(3), false);

        assertTrue(prestamo.estaAtrasado());
        assertEquals(3, prestamo.getDiasAtraso());
    }

    @Test
    @DisplayName("Un prestamo devuelto nunca cuenta como atrasado")
    void prestamoDevueltoNoEstaAtrasado() {
        LocalDate hoy = LocalDate.now();
        Prestamo prestamo = new Prestamo(1, 1, 1, hoy.minusDays(30), hoy.minusDays(23), true);

        assertFalse(prestamo.estaAtrasado());
        assertEquals(0, prestamo.getDiasAtraso());
    }

    @Test
    @DisplayName("El dia del vencimiento todavia no hay atraso")
    void elDiaDelVencimientoNoHayAtraso() {
        LocalDate hoy = LocalDate.now();
        Prestamo prestamo = new Prestamo(1, 1, 1, hoy.minusDays(7), hoy, false);

        assertFalse(prestamo.estaAtrasado());
    }

    @Test
    @DisplayName("Si se devolvio 4 dias despues del vencimiento, queda con 4 dias de atraso")
    void devueltoTardeConservaElAtraso() {
        LocalDate hoy = LocalDate.now();
        Prestamo prestamo = new Prestamo(1, 1, 1, hoy.minusDays(20), hoy.minusDays(13), true);
        prestamo.setFechaEntrega(hoy.minusDays(9));

        assertFalse(prestamo.estaAtrasado());
        assertEquals(4, prestamo.getDiasAtraso());
        assertEquals("Devuelto con 4 dia(s) de atraso", prestamo.getEstado());
    }

    @Test
    @DisplayName("getEstado describe cada situacion del prestamo")
    void getEstadoDescribeCadaCaso() {
        LocalDate hoy = LocalDate.now();

        assertEquals("En prestamo", new Prestamo(1, 1).getEstado());
        assertEquals("Atrasado 2 dia(s)",
                new Prestamo(1, 1, 1, hoy.minusDays(9), hoy.minusDays(2), false).getEstado());

        Prestamo aTiempo = new Prestamo(1, 1, 1, hoy.minusDays(5), hoy.plusDays(2), true);
        aTiempo.setFechaEntrega(hoy);
        assertEquals("Devuelto", aTiempo.getEstado());
    }
}
