package cl.duoc.biblioteca.modelo;

/**
 * Estudiante que pide libros prestados (tabla estudiantes).
 * HEREDA de Persona y agrega el curso.
 */
public class Estudiante extends Persona {

    private String curso;

    public Estudiante() {
    }

    public Estudiante(int id, String nombre, String rut, String curso, String correo) {
        super(id, nombre, rut, correo);
        this.curso = curso;
    }

    /** SOBRESCRITURA (polimorfismo): el estudiante se describe con su curso. */
    @Override
    public String getTipo() {
        return "Estudiante de " + curso;
    }

    public String getCurso() { return curso; }
    public void setCurso(String curso) { this.curso = curso; }
}
