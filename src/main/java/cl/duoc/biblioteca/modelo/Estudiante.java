package cl.duoc.biblioteca.modelo;

/**
 * Estudiante que pide libros prestados (tabla estudiantes).
 *
 * @author Olga Rivas
 * @version 1.0
 */
public class Estudiante extends Persona {

    /** Curso, por ejemplo "3ro Medio A". */
    private String curso;

    /**
     * Crea un estudiante vacio.
     */
    public Estudiante() {
    }

    /**
     * Crea un estudiante con todos sus datos.
     *
     * @param id     identificador en la base
     * @param nombre nombre completo
     * @param rut    RUT con guion
     * @param curso  curso del estudiante
     * @param correo correo electronico
     */
    public Estudiante(int id, String nombre, String rut, String curso, String correo) {
        super(id, nombre, rut, correo);
        this.curso = curso;
    }

    /**
     * Sobrescritura: el estudiante se describe con su curso.
     *
     * @return "Estudiante de" seguido del curso
     */
    @Override
    public String getTipo() {
        return "Estudiante de " + curso;
    }

    public String getCurso() {
        return curso;
    }

    public void setCurso(String curso) {
        this.curso = curso;
    }
}
