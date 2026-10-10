package cl.duoc.biblioteca.modelo;

/**
 * Categoria a la que pertenece un libro (tabla categorias).
 *
 * @author Olga Rivas
 * @version 1.0
 */
public class Categoria {

    /** Identificador en la base de datos. */
    private int id;

    /** Nombre de la categoria. */
    private String nombre;

    /**
     * Crea una categoria vacia.
     */
    public Categoria() {
    }

    /**
     * @param id     identificador en la base
     * @param nombre nombre de la categoria
     */
    public Categoria(int id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /**
     * @return el nombre, para mostrarlo en los JComboBox
     */
    @Override
    public String toString() {
        return nombre;
    }
}
