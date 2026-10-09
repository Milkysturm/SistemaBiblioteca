package cl.duoc.biblioteca.modelo;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/** Préstamo de un libro a un estudiante (tabla prestamos). */
public class Prestamo {

    /** Días que dura un préstamo. Coincide con los datos de ejemplo (+7 días). */
    public static final int DIAS_PRESTAMO = 7;

    private int id;
    private int idEstudiante;
    private int idLibro;
    private LocalDate fechaPrestamo;
    private LocalDate fechaDevolucion;   // fecha de VENCIMIENTO (cuándo debe devolverlo)
    private boolean devuelto;

    // Datos solo para mostrar en tablas y reportes (vienen de un JOIN)
    private String nombreEstudiante;
    private String tituloLibro;

    public Prestamo() {
    }

    /** Crea un préstamo nuevo con fecha de hoy y vencimiento calculado automáticamente. */
    public Prestamo(int idEstudiante, int idLibro) {
        this.idEstudiante = idEstudiante;
        this.idLibro = idLibro;
        this.fechaPrestamo = LocalDate.now();
        this.fechaDevolucion = calcularVencimiento(fechaPrestamo);
        this.devuelto = false;
    }

    public Prestamo(int id, int idEstudiante, int idLibro, LocalDate fechaPrestamo,
                    LocalDate fechaDevolucion, boolean devuelto) {
        this.id = id;
        this.idEstudiante = idEstudiante;
        this.idLibro = idLibro;
        this.fechaPrestamo = fechaPrestamo;
        this.fechaDevolucion = fechaDevolucion;
        this.devuelto = devuelto;
    }

    /** Calcula la fecha de vencimiento a partir de la fecha de préstamo. */
    public static LocalDate calcularVencimiento(LocalDate desde) {
        return desde.plusDays(DIAS_PRESTAMO);
    }

    /** Está atrasado si no se ha devuelto y ya pasó la fecha de vencimiento. */
    public boolean estaAtrasado() {
        return !devuelto && LocalDate.now().isAfter(fechaDevolucion);
    }

    /** Cuántos días lleva de atraso (0 si no está atrasado). */
    public long getDiasAtraso() {
        return estaAtrasado() ? ChronoUnit.DAYS.between(fechaDevolucion, LocalDate.now()) : 0;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getIdEstudiante() { return idEstudiante; }
    public void setIdEstudiante(int idEstudiante) { this.idEstudiante = idEstudiante; }

    public int getIdLibro() { return idLibro; }
    public void setIdLibro(int idLibro) { this.idLibro = idLibro; }

    public LocalDate getFechaPrestamo() { return fechaPrestamo; }
    public void setFechaPrestamo(LocalDate fechaPrestamo) { this.fechaPrestamo = fechaPrestamo; }

    public LocalDate getFechaDevolucion() { return fechaDevolucion; }
    public void setFechaDevolucion(LocalDate fechaDevolucion) { this.fechaDevolucion = fechaDevolucion; }

    public boolean isDevuelto() { return devuelto; }
    public void setDevuelto(boolean devuelto) { this.devuelto = devuelto; }

    public String getNombreEstudiante() { return nombreEstudiante; }
    public void setNombreEstudiante(String nombreEstudiante) { this.nombreEstudiante = nombreEstudiante; }

    public String getTituloLibro() { return tituloLibro; }
    public void setTituloLibro(String tituloLibro) { this.tituloLibro = tituloLibro; }
}
