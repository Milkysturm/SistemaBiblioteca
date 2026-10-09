package cl.duoc.biblioteca.modelo;

/**
 * Clase ABSTRACTA con los datos que comparten Usuario y Estudiante.
 * No se puede instanciar directamente: siempre se crea un Usuario o un Estudiante.
 */
public abstract class Persona {

    protected int id;
    protected String nombre;
    protected String rut;
    protected String correo;

    public Persona() {
    }

    public Persona(int id, String nombre, String rut, String correo) {
        this.id = id;
        this.nombre = nombre;
        this.rut = rut;
        this.correo = correo;
    }

    /**
     * Método abstracto: cada subclase DEBE decir qué tipo de persona es.
     * Es la base del polimorfismo: el mismo llamado responde distinto según el objeto.
     */
    public abstract String getTipo();

    /** Resumen que usa el método polimórfico getTipo(). */
    public String getResumen() {
        return getTipo() + ": " + nombre + " (" + rut + ")";
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getRut() { return rut; }
    public void setRut(String rut) { this.rut = rut; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    @Override
    public String toString() {
        return nombre;
    }
}
