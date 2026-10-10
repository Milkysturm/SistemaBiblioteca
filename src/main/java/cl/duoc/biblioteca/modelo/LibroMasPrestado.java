package cl.duoc.biblioteca.modelo;

/**
 * Fila del reporte de libros mas prestados: un libro y cuantas veces se presto.
 *
 * @author Olga Rivas
 * @version 1.0
 */
public class LibroMasPrestado {

    /** Titulo del libro. */
    private final String titulo;

    /** Autor del libro. */
    private final String autor;

    /** Cantidad de prestamos registrados del libro. */
    private final int vecesPrestado;

    /**
     * @param titulo        titulo del libro
     * @param autor         autor del libro
     * @param vecesPrestado cantidad de prestamos registrados
     */
    public LibroMasPrestado(String titulo, String autor, int vecesPrestado) {
        this.titulo = titulo;
        this.autor = autor;
        this.vecesPrestado = vecesPrestado;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getAutor() {
        return autor;
    }

    public int getVecesPrestado() {
        return vecesPrestado;
    }
}
