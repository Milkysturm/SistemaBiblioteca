package cl.duoc.biblioteca.dao;

import cl.duoc.biblioteca.modelo.Prestamo;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
            + "e.nombre AS nombre_estudiante, l.titulo AS titulo_libro "
            + "FROM prestamos p "
            + "JOIN estudiantes e ON p.id_estudiante = e.id "
            + "JOIN libros l ON p.id_libro = l.id ";

    @Override
    public boolean insertar(Prestamo prestamo) throws SQLException {
        String sql = "INSERT INTO prestamos (id_estudiante, id_libro, fecha_prestamo, fecha_devolucion, devuelto) "
                + "VALUES (?, ?, ?, ?, ?)";
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
     * @param idEstudiante estudiante a consultar
     * @return historial de prestamos del estudiante, del mas reciente al mas antiguo
     * @throws SQLException si falla la consulta
     */
    public List<Prestamo> listarPorEstudiante(int idEstudiante) throws SQLException {
        return consultar(SELECT_BASE + "WHERE p.id_estudiante = ? ORDER BY p.fecha_prestamo DESC", idEstudiante);
    }

    /**
     * Marca un prestamo como devuelto. Solo afecta prestamos que no estaban devueltos.
     *
     * @param idPrestamo prestamo a cerrar
     * @return true si se marco; false si no existe o ya estaba devuelto
     * @throws SQLException si falla la actualizacion
     */
    public boolean marcarDevuelto(int idPrestamo) throws SQLException {
        String sql = "UPDATE prestamos SET devuelto = TRUE WHERE id = ? AND devuelto = FALSE";
        try (PreparedStatement ps = getConexion().prepareStatement(sql)) {
            ps.setInt(1, idPrestamo);
            return ps.executeUpdate() > 0;
        }
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

    @Override
    public boolean actualizar(Prestamo prestamo) throws SQLException {
        String sql = "UPDATE prestamos SET id_estudiante = ?, id_libro = ?, fecha_prestamo = ?, "
                + "fecha_devolucion = ?, devuelto = ? WHERE id = ?";
        try (PreparedStatement ps = getConexion().prepareStatement(sql)) {
            llenarParametros(ps, prestamo);
            ps.setInt(6, prestamo.getId());
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
     * Copia los datos del prestamo en los cinco primeros parametros de la sentencia.
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
        prestamo.setNombreEstudiante(rs.getString("nombre_estudiante"));
        prestamo.setTituloLibro(rs.getString("titulo_libro"));
        return prestamo;
    }
}
