package cl.duoc.biblioteca.modelo;

/** Categoría a la que pertenece un libro (tabla categorias). */
public class Categoria {

    private int id;
    private String nombre;

    public Categoria() {
    }

    public Categoria(int id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    /** Se muestra el nombre (útil para los JComboBox de la interfaz). */
    @Override
    public String toString() {
        return nombre;
    }
}
