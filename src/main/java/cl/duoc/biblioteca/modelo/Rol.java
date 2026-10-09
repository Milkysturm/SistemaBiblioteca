package cl.duoc.biblioteca.modelo;

/** Roles posibles de un usuario, tal como están en el ENUM de la tabla usuarios. */
public enum Rol {
    BIBLIOTECARIO("bibliotecario"),
    ESTUDIANTE("estudiante");

    private final String valorBD;

    Rol(String valorBD) {
        this.valorBD = valorBD;
    }

    /** Texto que se guarda en la base de datos. */
    public String getValorBD() {
        return valorBD;
    }

    /** Convierte el texto de la base de datos en un Rol de Java. */
    public static Rol desdeBD(String valor) {
        for (Rol r : values()) {
            if (r.valorBD.equalsIgnoreCase(valor)) {
                return r;
            }
        }
        throw new IllegalArgumentException("Rol desconocido: " + valor);
    }
}
