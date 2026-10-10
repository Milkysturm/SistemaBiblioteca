package cl.duoc.biblioteca.modelo;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Prestamo de un libro a un estudiante (tabla prestamos).
 *
 * @author Olga Rivas
 * @version 1.0
 */
public class Prestamo {

    /** Dias que dura un prestamo, igual que en los datos de ejemplo. */
    public static final int DIAS_PRESTAMO = 7;

    /** Identificador en la base de datos. */
    private int id;

    /** Estudiante que pidio el libro. */
    private int idEstudiante;

    /** Libro prestado. */
    private int idLibro;

    /** Dia en que se presto. */
    private LocalDate fechaPrestamo;

    /** Fecha de vencimiento: dia en que se debe devolver. */
    private LocalDate fechaDevolucion;

    /** Indica si el libro ya se devolvio. */
    private boolean devuelto;

    /** Dia en que el libro se devolvio de verdad, o null si aun no se devuelve. */
    private LocalDate fechaEntrega;

    /** Nombre del estudiante, solo para mostrar en tablas. */
    private String nombreEstudiante;

    /** Titulo del libro, solo para mostrar en tablas. */
    private String tituloLibro;

    /**
     * Crea un prestamo vacio.
     */
    public Prestamo() {
    }

    /**
     * Crea un prestamo nuevo con fecha de hoy y vencimiento calculado automaticamente.
     *
     * @param idEstudiante estudiante que pide el libro
     * @param idLibro      libro prestado
     */
    public Prestamo(int idEstudiante, int idLibro) {
        this.idEstudiante = idEstudiante;
        this.idLibro = idLibro;
        this.fechaPrestamo = LocalDate.now();
        this.fechaDevolucion = calcularVencimiento(fechaPrestamo);
        this.devuelto = false;
    }

    /**
     * Crea un prestamo con todos sus datos, tal como viene de la base.
     *
     * @param id              identificador en la base
     * @param idEstudiante    estudiante que pidio el libro
     * @param idLibro         libro prestado
     * @param fechaPrestamo   dia del prestamo
     * @param fechaDevolucion fecha de vencimiento
     * @param devuelto        si ya se devolvio
     */
    public Prestamo(int id, int idEstudiante, int idLibro, LocalDate fechaPrestamo,
                    LocalDate fechaDevolucion, boolean devuelto) {
        this.id = id;
        this.idEstudiante = idEstudiante;
        this.idLibro = idLibro;
        this.fechaPrestamo = fechaPrestamo;
        this.fechaDevolucion = fechaDevolucion;
        this.devuelto = devuelto;
    }

    /**
     * Calcula la fecha de vencimiento a partir del dia del prestamo.
     *
     * @param desde dia del prestamo
     * @return dia en que se debe devolver
     */
    public static LocalDate calcularVencimiento(LocalDate desde) {
        return desde.plusDays(DIAS_PRESTAMO);
    }

    /**
     * @return true si no se ha devuelto y ya paso la fecha de vencimiento
     */
    public boolean estaAtrasado() {
        return !devuelto && LocalDate.now().isAfter(fechaDevolucion);
    }

    /**
     * Dias de atraso: si sigue prestado, hasta hoy; si ya se devolvio, hasta el
     * dia de entrega.
     *
     * @return dias de atraso, o 0 si no hubo atraso o no se conoce la fecha de entrega
     */
    public long getDiasAtraso() {
        LocalDate hasta = devuelto ? fechaEntrega : LocalDate.now();
        if (hasta == null || !hasta.isAfter(fechaDevolucion)) {
            return 0;
        }
        return ChronoUnit.DAYS.between(fechaDevolucion, hasta);
    }

    /**
     * @return estado del prestamo en palabras, para mostrar en tablas y reportes
     */
    public String getEstado() {
        long dias = getDiasAtraso();
        if (devuelto) {
            return (dias > 0) ? "Devuelto con " + dias + " dia(s) de atraso" : "Devuelto";
        }
        return (dias > 0) ? "Atrasado " + dias + " dia(s)" : "En prestamo";
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdEstudiante() {
        return idEstudiante;
    }

    public void setIdEstudiante(int idEstudiante) {
        this.idEstudiante = idEstudiante;
    }

    public int getIdLibro() {
        return idLibro;
    }

    public void setIdLibro(int idLibro) {
        this.idLibro = idLibro;
    }

    public LocalDate getFechaPrestamo() {
        return fechaPrestamo;
    }

    public void setFechaPrestamo(LocalDate fechaPrestamo) {
        this.fechaPrestamo = fechaPrestamo;
    }

    public LocalDate getFechaDevolucion() {
        return fechaDevolucion;
    }

    public void setFechaDevolucion(LocalDate fechaDevolucion) {
        this.fechaDevolucion = fechaDevolucion;
    }

    public boolean isDevuelto() {
        return devuelto;
    }

    public void setDevuelto(boolean devuelto) {
        this.devuelto = devuelto;
    }

    public LocalDate getFechaEntrega() {
        return fechaEntrega;
    }

    public void setFechaEntrega(LocalDate fechaEntrega) {
        this.fechaEntrega = fechaEntrega;
    }

    public String getNombreEstudiante() {
        return nombreEstudiante;
    }

    public void setNombreEstudiante(String nombreEstudiante) {
        this.nombreEstudiante = nombreEstudiante;
    }

    public String getTituloLibro() {
        return tituloLibro;
    }

    public void setTituloLibro(String tituloLibro) {
        this.tituloLibro = tituloLibro;
    }
}
