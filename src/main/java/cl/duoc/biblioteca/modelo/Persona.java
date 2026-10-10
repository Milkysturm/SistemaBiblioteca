package cl.duoc.biblioteca.modelo;

/**
 * Datos comunes de las personas del sistema: usuarios y estudiantes.
 *
 * Es abstracta: no se crea una Persona directamente, sino un Usuario o un Estudiante.
 *
 * @author Olga Rivas
 * @version 1.0
 */
public abstract class Persona {

    /** Identificador en la base de datos. */
    protected int id;

    /** Nombre completo. */
    protected String nombre;

    /** RUT con guion, por ejemplo 12345678-9. */
    protected String rut;

    /** Correo electronico. */
    protected String correo;

    /**
     * Crea una persona vacia.
     */
    protected Persona() {
    }

    /**
     * Crea una persona con todos sus datos.
     *
     * @param id     identificador en la base
     * @param nombre nombre completo
     * @param rut    RUT con guion
     * @param correo correo electronico
     */
    protected Persona(int id, String nombre, String rut, String correo) {
        this.id = id;
        this.nombre = nombre;
        this.rut = rut;
        this.correo = correo;
    }

    /**
     * Indica que tipo de persona es. Cada subclase lo responde a su manera (polimorfismo).
     *
     * @return descripcion del tipo de persona
     */
    public abstract String getTipo();

    /**
     * @return tipo, nombre y RUT en una sola linea
     */
    public String getResumen() {
        return getTipo() + ": " + nombre + " (" + rut + ")";
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

    public String getRut() {
        return rut;
    }

    public void setRut(String rut) {
        this.rut = rut;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    /**
     * @return el nombre, para mostrarlo en listas y combos
     */
    @Override
    public String toString() {
        return nombre;
    }
}
