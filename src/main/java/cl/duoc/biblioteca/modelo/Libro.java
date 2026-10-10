package cl.duoc.biblioteca.modelo;

/**
 * Libro del inventario (tabla libros).
 *
 * @author Olga Rivas
 * @version 1.0
 */
public class Libro {

    /** Identificador en la base de datos. */
    private int id;

    /** Titulo del libro. */
    private String titulo;

    /** Autor del libro. */
    private String autor;

    /** Codigo ISBN, unico por libro. */
    private String isbn;

    /** Editorial. */
    private String editorial;

    /** Ejemplares disponibles para prestar. */
    private int stock;

    /** Categoria a la que pertenece. */
    private Categoria categoria;

    /**
     * Crea un libro vacio.
     */
    public Libro() {
    }

    /**
     * Crea un libro con todos sus datos.
     *
     * @param id        identificador en la base
     * @param titulo    titulo
     * @param autor     autor
     * @param isbn      codigo ISBN
     * @param editorial editorial
     * @param stock     ejemplares disponibles
     * @param categoria categoria del libro
     */
    public Libro(int id, String titulo, String autor, String isbn,
                 String editorial, int stock, Categoria categoria) {
        this.id = id;
        this.titulo = titulo;
        this.autor = autor;
        this.isbn = isbn;
        this.editorial = editorial;
        this.stock = stock;
        this.categoria = categoria;
    }

    /**
     * @return true si queda al menos un ejemplar para prestar
     */
    public boolean hayStock() {
        return stock > 0;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getEditorial() {
        return editorial;
    }

    public void setEditorial(String editorial) {
        this.editorial = editorial;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    /**
     * @return titulo y stock, para mostrarlo en los JComboBox
     */
    @Override
    public String toString() {
        return titulo + " (stock: " + stock + ")";
    }
}
