package cl.duoc.biblioteca.controlador;

import cl.duoc.biblioteca.dao.DatosDePrueba;
import cl.duoc.biblioteca.dao.EstudianteDAO;
import cl.duoc.biblioteca.dao.LibroDAO;
import cl.duoc.biblioteca.dao.PrestamoDAO;
import cl.duoc.biblioteca.modelo.Categoria;
import cl.duoc.biblioteca.modelo.Estudiante;
import cl.duoc.biblioteca.modelo.Libro;
import cl.duoc.biblioteca.modelo.Prestamo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas del servicio de prestamos, incluida la concurrencia: varios hilos piden
 * el mismo libro al mismo tiempo y el stock nunca debe quedar inconsistente.
 *
 * @author Olga Rivas
 * @version 1.0
 */
class ServicioPrestamosTest {

    /** Ejemplares del libro de prueba. */
    private static final int STOCK_INICIAL = 3;

    /** Hilos que piden el libro al mismo tiempo (mas que el stock). */
    private static final int HILOS = 10;

    /** Segundos maximos de espera: si se cumplen, hubo un bloqueo. */
    private static final int ESPERA_MAXIMA_SEGUNDOS = 30;

    /** Objeto que se esta probando. */
    private final ServicioPrestamos servicio = new ServicioPrestamos();

    /** Acceso a libros, para revisar el stock. */
    private final LibroDAO libroDAO = new LibroDAO();

    /** Libro de prueba. */
    private Libro libro;

    /** Estudiantes de prueba, uno por hilo. */
    private final List<Estudiante> estudiantes = new ArrayList<>();

    /**
     * Crea el libro y los estudiantes de prueba.
     *
     * @throws SQLException si falla la preparacion
     */
    @BeforeEach
    void preparar() throws SQLException {
        Assumptions.assumeTrue(DatosDePrueba.hayBaseDeDatos(), "se necesita MySQL con la base biblioteca");
        DatosDePrueba.limpiar();

        libro = new Libro(0, "Libro de prueba JUnit", "Autor Prueba", DatosDePrueba.ISBN,
                "Editorial Prueba", STOCK_INICIAL, new Categoria(DatosDePrueba.ID_CATEGORIA_EJEMPLO, null));
        libroDAO.insertar(libro);

        EstudianteDAO estudianteDAO = new EstudianteDAO();
        estudiantes.clear();
        for (int i = 0; i < HILOS; i++) {
            Estudiante estudiante = new Estudiante(0, "Estudiante Prueba " + i, "9999999" + i + "-0",
                    "1ro Medio A", "prueba" + i + "@correo.cl");
            estudianteDAO.insertar(estudiante);
            estudiantes.add(estudiante);
        }
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

    @RepeatedTest(3)
    @DisplayName("10 hilos piden a la vez un libro con 3 ejemplares: solo 3 lo consiguen y el stock queda en 0")
    void prestamosSimultaneosNoSuperanElStock() throws Exception {
        CountDownLatch partida = new CountDownLatch(1);
        AtomicInteger exitos = new AtomicInteger();
        AtomicInteger rechazos = new AtomicInteger();
        List<Throwable> errores = Collections.synchronizedList(new ArrayList<>());

        ExecutorService hilos = Executors.newFixedThreadPool(HILOS);
        for (Estudiante estudiante : estudiantes) {
            hilos.submit(() -> {
                try {
                    partida.await();   // todos esperan aqui y parten juntos
                    servicio.registrarPrestamo(estudiante.getId(), libro.getId());
                    exitos.incrementAndGet();
                } catch (IllegalStateException e) {
                    rechazos.incrementAndGet();   // sin stock: es lo esperado
                } catch (Exception e) {
                    errores.add(e);
                }
            });
        }
        partida.countDown();
        hilos.shutdown();

        assertTrue(hilos.awaitTermination(ESPERA_MAXIMA_SEGUNDOS, TimeUnit.SECONDS),
                "los hilos no terminaron: posible bloqueo");
        assertTrue(errores.isEmpty(), "errores inesperados: " + errores);
        assertEquals(STOCK_INICIAL, exitos.get());
        assertEquals(HILOS - STOCK_INICIAL, rechazos.get());
        assertEquals(0, libroDAO.buscarPorId(libro.getId()).getStock());
        assertEquals(STOCK_INICIAL, new PrestamoDAO().contarPorLibro(libro.getId()));
    }

    @Test
    @DisplayName("Un prestamo descuenta 1 del stock y vence en 7 dias")
    void prestamoDescuentaStock() throws SQLException {
        Prestamo prestamo = servicio.registrarPrestamo(estudiantes.get(0).getId(), libro.getId());

        assertTrue(prestamo.getId() > 0);
        assertEquals(LocalDate.now().plusDays(Prestamo.DIAS_PRESTAMO), prestamo.getFechaDevolucion());
        assertEquals(STOCK_INICIAL - 1, libroDAO.buscarPorId(libro.getId()).getStock());
    }

    @Test
    @DisplayName("La devolucion repone el stock, guarda la fecha de entrega y no se puede repetir")
    void devolucionReponeStock() throws SQLException {
        Prestamo prestamo = servicio.registrarPrestamo(estudiantes.get(0).getId(), libro.getId());

        Prestamo devuelto = servicio.registrarDevolucion(prestamo.getId());

        assertTrue(devuelto.isDevuelto());
        assertEquals(LocalDate.now(), devuelto.getFechaEntrega());
        assertEquals(STOCK_INICIAL, libroDAO.buscarPorId(libro.getId()).getStock());
        assertThrows(IllegalStateException.class, () -> servicio.registrarDevolucion(prestamo.getId()));
        assertEquals(STOCK_INICIAL, libroDAO.buscarPorId(libro.getId()).getStock());
    }

    @Test
    @DisplayName("Sin stock no se presta, y no se guarda ningun prestamo")
    void sinStockNoSePresta() throws SQLException {
        libroDAO.cambiarStock(libro.getId(), -STOCK_INICIAL);

        assertThrows(IllegalStateException.class,
                () -> servicio.registrarPrestamo(estudiantes.get(0).getId(), libro.getId()));
        assertEquals(0, new PrestamoDAO().contarPorLibro(libro.getId()));
    }

    @Test
    @DisplayName("Un estudiante no puede tener dos veces el mismo libro sin devolverlo")
    void noSePrestaDosVecesAlMismoEstudiante() throws SQLException {
        int idEstudiante = estudiantes.get(0).getId();
        servicio.registrarPrestamo(idEstudiante, libro.getId());

        assertThrows(IllegalStateException.class, () -> servicio.registrarPrestamo(idEstudiante, libro.getId()));
        assertEquals(STOCK_INICIAL - 1, libroDAO.buscarPorId(libro.getId()).getStock());
    }

    @Test
    @DisplayName("Editar un libro no pisa un prestamo hecho mientras se editaba")
    void editarLibroNoPisaUnPrestamo() throws SQLException {
        int stockLeido = libroDAO.buscarPorId(libro.getId()).getStock();   // el formulario muestra 3
        servicio.registrarPrestamo(estudiantes.get(0).getId(), libro.getId());   // otro hilo presta: queda 2

        libro.setTitulo("Titulo editado");
        libro.setStock(stockLeido);
        assertThrows(IllegalStateException.class, () -> servicio.actualizarLibro(libro, stockLeido));
        assertEquals(STOCK_INICIAL - 1, libroDAO.buscarPorId(libro.getId()).getStock());

        libro.setStock(STOCK_INICIAL - 1);
        assertTrue(servicio.actualizarLibro(libro, STOCK_INICIAL - 1));
        assertEquals("Titulo editado", libroDAO.buscarPorId(libro.getId()).getTitulo());
    }
}
