package cl.duoc.biblioteca.modelo;

/**
 * Roles de usuario, tal como estan en el ENUM de la tabla usuarios.
 *
 * @author Olga Rivas
 * @version 1.0
 */
public enum Rol {

    /** Gestion total del sistema. */
    BIBLIOTECARIO("bibliotecario"),

    /** Solo prestamo, devolucion y consulta. */
    ESTUDIANTE("estudiante");

    /** Texto que se guarda en la base de datos. */
    private final String valorBD;

    /**
     * @param valorBD texto que se guarda en la base de datos
     */
    Rol(String valorBD) {
        this.valorBD = valorBD;
    }

    /**
     * @return texto que se guarda en la base de datos
     */
    public String getValorBD() {
        return valorBD;
    }

    /**
     * Convierte el texto de la base de datos en un Rol.
     *
     * @param valor texto leido de la base
     * @return el rol correspondiente
     * @throws IllegalArgumentException si el texto no corresponde a ningun rol
     */
    public static Rol desdeBD(String valor) {
        for (Rol rol : values()) {
            if (rol.valorBD.equalsIgnoreCase(valor)) {
                return rol;
            }
        }
        throw new IllegalArgumentException("Rol desconocido: " + valor);
    }
}
