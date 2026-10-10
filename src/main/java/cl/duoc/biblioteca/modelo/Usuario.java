package cl.duoc.biblioteca.modelo;

/**
 * Persona que inicia sesion en el sistema (tabla usuarios).
 *
 * @author Olga Rivas
 * @version 1.0
 */
public class Usuario extends Persona {

    /** Contrasena de acceso. */
    private String contrasena;

    /** Rol que define que puede hacer en el sistema. */
    private Rol rol;

    /**
     * Crea un usuario vacio.
     */
    public Usuario() {
    }

    /**
     * Crea un usuario con todos sus datos.
     *
     * @param id         identificador en la base
     * @param nombre     nombre completo
     * @param rut        RUT con guion
     * @param correo     correo, que se usa para iniciar sesion
     * @param contrasena contrasena de acceso
     * @param rol        rol del usuario
     */
    public Usuario(int id, String nombre, String rut, String correo, String contrasena, Rol rol) {
        super(id, nombre, rut, correo);
        this.contrasena = contrasena;
        this.rol = rol;
    }

    /**
     * Sobrescritura: el usuario se describe segun su rol.
     *
     * @return "Bibliotecario" o "Usuario estudiante"
     */
    @Override
    public String getTipo() {
        return rol == Rol.BIBLIOTECARIO ? "Bibliotecario" : "Usuario estudiante";
    }

    /**
     * @return true si tiene gestion total del sistema
     */
    public boolean esBibliotecario() {
        return rol == Rol.BIBLIOTECARIO;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }
}
