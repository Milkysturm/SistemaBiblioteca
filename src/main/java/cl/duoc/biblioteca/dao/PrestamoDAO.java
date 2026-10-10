package cl.duoc.biblioteca.dao;

import cl.duoc.biblioteca.modelo.LibroMasPrestado;
import cl.duoc.biblioteca.modelo.Prestamo;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a la tabla prestamos.
 *
 * @author Olga Rivas
 * @version 1.0
 */
public class PrestamoDAO extends BaseDAO implements CrudDAO<Prestamo> {

    /** Consulta base: los JOIN traen el nombre del estudiante y el titulo del libro. */
    private static final String SELECT_BASE =
            "SELECT p.id, p.id_estudiante, p.id_libro, p.fecha_prestamo, p.fecha_devolucion, p.devuelto, "
            + "p.fecha_entrega, "
            + "e.nombre AS nombre_estudiante, l.titulo AS titulo_libro "
            + "FROM prestamos p "
            + "JOIN estudiantes e ON p.id_estudiante = e.id "
            + "JOIN libros l ON p.id_libro = l.id ";

    /**
     * Agrega la columna fecha_entrega si la base todavia no la tiene (script 03).
     * Asi la aplicacion funciona aunque solo se hayan ejecutado los dos scripts de Duoc.
     *
     * @return true si tuvo que agregar la columna
     * @throws SQLException si falla la consulta o la modificacion
     */
    public boolean asegurarColumnaFechaEntrega() throws SQLException {
        String consulta = "SELECT COUNT(*) FROM information_schema.COLUMNS "
                + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'prestamos' AND COLUMN_NAME = 'fecha_entrega'";
        try (PreparedStatement ps = getConexion().prepareStatement(consulta);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next() && rs.getInt(1) > 0) {
                return false;
            }
        }
        try (Statement sentencia = getConexion().createStatement()) {
            sentencia.executeUpdate("ALTER TABLE prestamos ADD COLUMN fecha_entrega DATE NULL AFTER fecha_devolucion");
        }
        return true;
    }

    @Override
    public boolean insertar(Prestamo prestamo) throws SQLException {
        String sql = "INSERT INTO prestamos (id_estudiante, id_libro, fecha_prestamo, fecha_devolucion, "
                + "devuelto, fecha_entrega) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = getConexion().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            llenarParametros(ps, prestamo);
            int filas = ps.executeUpdate();
            prestamo.setId(idGenerado(ps));
            return filas > 0;
        }
    }

    @Override
    public Prestamo buscarPorId(int id) throws SQLException {
        try (PreparedStatement ps = getConexion().prepareStatement(SELECT_BASE + "WHERE p.id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    @Override
    public List<Prestamo> listar() throws SQLException {
        return consultar(SELECT_BASE + "ORDER BY p.fecha_prestamo DESC", 0);
    }

    /**
     * @return prestamos aun no devueltos (libros actualmente en prestamo)
     * @throws SQLException si falla la consulta
     */
    public List<Prestamo> listarActivos() throws SQLException {
        return consultar(SELECT_BASE + "WHERE p.devuelto = FALSE ORDER BY p.fecha_devolucion", 0);
    }

    /**
     * @return prestamos devueltos despues de su vencimiento (constancia de atrasos)
     * @throws SQLException si falla la consulta
     */
    public List<Prestamo> listarDevueltosConAtraso() throws SQLException {
        return consultar(SELECT_BASE + "WHERE p.devuelto = TRUE AND p.fecha_entrega > p.fecha_devolucion "
                + "ORDER BY p.fecha_entrega DESC", 0);
    }

    /**
     * @param idEstudiante estudiante a consultar
     * @return historial de prestamos del estudiante, del mas reciente al mas antiguo
     * @throws SQLException si falla la consulta
     */
    public List<Prestamo> listarPorEstudiante(int idEstudiante) throws SQLException {
        return consultar(SELECT_BASE + "WHERE p.id_estudiante = ? ORDER BY p.fecha_prestamo DESC", idEstudiante);
    }

    /**
     * Marca un prestamo como devuelto y guarda el dia de entrega, que deja
     * constancia de si hubo atraso. Solo afecta prestamos que no estaban devueltos.
     *
     * @param idPrestamo prestamo a cerrar
     * @param entrega    dia en que se devolvio el libro
     * @return true si se marco; false si no existe o ya estaba devuelto
     * @throws SQLException si falla la actualizacion
     */
    public boolean marcarDevuelto(int idPrestamo, LocalDate entrega) throws SQLException {
        String sql = "UPDATE prestamos SET devuelto = TRUE, fecha_entrega = ? WHERE id = ? AND devuelto = FALSE";
        try (PreparedStatement ps = getConexion().prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(entrega));
            ps.setInt(2, idPrestamo);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Reporte: libros ordenados por cantidad de prestamos, de mayor a menor.
     *
     * @param limite cantidad maxima de libros a devolver
     * @return los libros mas prestados con su cantidad de prestamos
     * @throws SQLException si falla la consulta
     */
    public List<LibroMasPrestado> librosMasPrestados(int limite) throws SQLException {
        String sql = "SELECT l.titulo, l.autor, COUNT(p.id) AS veces "
                + "FROM prestamos p JOIN libros l ON p.id_libro = l.id "
                + "GROUP BY l.id, l.titulo, l.autor "
                + "ORDER BY veces DESC, l.titulo "
                + "LIMIT ?";
        List<LibroMasPrestado> lista = new ArrayList<>();
        try (PreparedStatement ps = getConexion().prepareStatement(sql)) {
            ps.setInt(1, limite);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new LibroMasPrestado(rs.getString("titulo"), rs.getString("autor"),
                            rs.getInt("veces")));
                }
            }
        }
        return lista;
    }

    /**
     * @param idLibro libro a consultar
     * @return cantidad de prestamos (devueltos o no) registrados para el libro
     * @throws SQLException si falla la consulta
     */
    public int contarPorLibro(int idLibro) throws SQLException {
        return contar("SELECT COUNT(*) FROM prestamos WHERE id_libro = ?", idLibro);
    }

    /**
     * @param idEstudiante estudiante a consultar
     * @return cantidad de prestamos (devueltos o no) registrados para el estudiante
     * @throws SQLException si falla la consulta
     */
    public int contarPorEstudiante(int idEstudiante) throws SQLException {
        return contar("SELECT COUNT(*) FROM prestamos WHERE id_estudiante = ?", idEstudiante);
    }

    /**
     * @param idEstudiante estudiante a consultar
     * @param idLibro      libro a consultar
     * @return true si el estudiante tiene ese libro prestado y aun no lo devuelve
     * @throws SQLException si falla la consulta
     */
    public boolean tienePrestamoActivo(int idEstudiante, int idLibro) throws SQLException {
        String sql = "SELECT COUNT(*) FROM prestamos WHERE id_estudiante = ? AND id_libro = ? AND devuelto = FALSE";
        try (PreparedStatement ps = getConexion().prepareStatement(sql)) {
            ps.setInt(1, idEstudiante);
            ps.setInt(2, idLibro);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    @Override
    public boolean actualizar(Prestamo prestamo) throws SQLException {
        String sql = "UPDATE prestamos SET id_estudiante = ?, id_libro = ?, fecha_prestamo = ?, "
                + "fecha_devolucion = ?, devuelto = ?, fecha_entrega = ? WHERE id = ?";
        try (PreparedStatement ps = getConexion().prepareStatement(sql)) {
            llenarParametros(ps, prestamo);
            ps.setInt(7, prestamo.getId());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean eliminar(int id) throws SQLException {
        String sql = "DELETE FROM prestamos WHERE id = ?";
        try (PreparedStatement ps = getConexion().prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Ejecuta una consulta de prestamos con un parametro entero opcional.
     *
     * @param sql       consulta a ejecutar
     * @param parametro valor del primer "?", ignorado si la consulta no tiene
     * @return prestamos encontrados
     * @throws SQLException si falla la consulta
     */
    private List<Prestamo> consultar(String sql, int parametro) throws SQLException {
        List<Prestamo> lista = new ArrayList<>();
        try (PreparedStatement ps = getConexion().prepareStatement(sql)) {
            if (sql.contains("?")) {
                ps.setInt(1, parametro);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    /**
     * Ejecuta un SELECT COUNT(*) con un parametro entero.
     *
     * @param sql       consulta de conteo
     * @param parametro valor del "?"
     * @return el conteo
     * @throws SQLException si falla la consulta
     */
    private int contar(String sql, int parametro) throws SQLException {
        try (PreparedStatement ps = getConexion().prepareStatement(sql)) {
            ps.setInt(1, parametro);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    /**
     * Copia los datos del prestamo en los seis primeros parametros de la sentencia.
     *
     * @param ps       sentencia de INSERT o UPDATE
     * @param prestamo prestamo con los datos
     * @throws SQLException si falla la asignacion
     */
    private void llenarParametros(PreparedStatement ps, Prestamo prestamo) throws SQLException {
        ps.setInt(1, prestamo.getIdEstudiante());
        ps.setInt(2, prestamo.getIdLibro());
        ps.setDate(3, Date.valueOf(prestamo.getFechaPrestamo()));
        ps.setDate(4, Date.valueOf(prestamo.getFechaDevolucion()));
        ps.setBoolean(5, prestamo.isDevuelto());
        if (prestamo.getFechaEntrega() != null) {
            ps.setDate(6, Date.valueOf(prestamo.getFechaEntrega()));
        } else {
            ps.setNull(6, Types.DATE);
        }
    }

    /**
     * Convierte la fila actual en un Prestamo.
     *
     * @param rs resultado posicionado en una fila
     * @return el prestamo leido
     * @throws SQLException si falla la lectura
     */
    private Prestamo mapear(ResultSet rs) throws SQLException {
        Prestamo prestamo = new Prestamo(
                rs.getInt("id"),
                rs.getInt("id_estudiante"),
                rs.getInt("id_libro"),
                rs.getDate("fecha_prestamo").toLocalDate(),
                rs.getDate("fecha_devolucion").toLocalDate(),
                rs.getBoolean("devuelto"));
        Date entrega = rs.getDate("fecha_entrega");   // NULL mientras no se devuelva
        prestamo.setFechaEntrega(entrega != null ? entrega.toLocalDate() : null);
        prestamo.setNombreEstudiante(rs.getString("nombre_estudiante"));
        prestamo.setTituloLibro(rs.getString("titulo_libro"));
        return prestamo;
    }
}
