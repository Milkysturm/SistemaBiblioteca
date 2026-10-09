package cl.duoc.biblioteca.modelo;

/**
 * Usuario que inicia sesión en el sistema (tabla usuarios).
 * HEREDA de Persona: ya tiene id, nombre, rut y correo.
 */
public class Usuario extends Persona {

    private String contrasena;
    private Rol rol;

    public Usuario() {
    }

    public Usuario(int id, String nombre, String rut, String correo, String contrasena, Rol rol) {
        super(id, nombre, rut, correo);   // llama al constructor de Persona
        this.contrasena = contrasena;
        this.rol = rol;
    }

    /** SOBRESCRITURA (polimorfismo): un Usuario se describe según su rol. */
    @Override
    public String getTipo() {
        return rol == Rol.BIBLIOTECARIO ? "Bibliotecario" : "Usuario estudiante";
    }

    /** El bibliotecario tiene gestión total; el estudiante no. */
    public boolean esBibliotecario() {
        return rol == Rol.BIBLIOTECARIO;
    }

    public String getContrasena() { return contrasena; }
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }

    public Rol getRol() { return rol; }
    public void setRol(Rol rol) { this.rol = rol; }
}
