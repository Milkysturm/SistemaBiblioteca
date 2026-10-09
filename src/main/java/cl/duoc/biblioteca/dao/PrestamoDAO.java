package cl.duoc.biblioteca.dao;

import cl.duoc.biblioteca.modelo.Prestamo;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** Acceso a datos de la tabla prestamos. */
public class PrestamoDAO extends BaseDAO implements CrudDAO<Prestamo> {

    // JOIN para mostrar el nombre del estudiante y el título del libro
    private static final String SELECT_BASE =
            "SELECT p.id, p.id_estudiante, p.id_libro, p.fecha_prestamo, p.fecha_devolucion, p.devuelto, "
            + "e.nombre AS nombre_estudiante, l.titulo AS titulo_libro "
            + "FROM prestamos p "
            + "JOIN estudiantes e ON p.id_estudiante = e.id "
            + "JOIN libros l ON p.id_libro = l.id ";

    @Override
    public boolean insertar(Prestamo p) throws SQLException {
        String sql = "INSERT INTO prestamos (id_estudiante, id_libro, fecha_prestamo, fecha_devolucion, devuelto) "
                   + "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = getConexion().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, p.getIdEstudiante());
            ps.setInt(2, p.getIdLibro());
            ps.setDate(3, Date.valueOf(p.getFechaPrestamo()));     // LocalDate -> java.sql.Date
            ps.setDate(4, Date.valueOf(p.getFechaDevolucion()));
            ps.setBoolean(5, p.isDevuelto());
            int filas = ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) p.setId(keys.getInt(1));
            }
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
        return consultar(SELECT_BASE + "ORDER BY p.fecha_prestamo DESC");
    }

    /** Préstamos que aún no se devuelven (libros actualmente en préstamo). */
    public List<Prestamo> listarActivos() throws SQLException {
        return consultar(SELECT_BASE + "WHERE p.devuelto = FALSE ORDER BY p.fecha_devolucion");
    }

    /** Historial de préstamos de un estudiante. */
    public List<Prestamo> listarPorEstudiante(int idEstudiante) throws SQLException {
        List<Prestamo> lista = new ArrayList<>();
        String sql = SELECT_BASE + "WHERE p.id_estudiante = ? ORDER BY p.fecha_prestamo DESC";
        try (PreparedStatement ps = getConexion().prepareStatement(sql)) {
            ps.setInt(1, idEstudiante);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        }
        return lista;
    }

    /** Marca un préstamo como devuelto. Solo afecta préstamos que no estaban devueltos. */
    public boolean marcarDevuelto(int idPrestamo) throws SQLException {
        String sql = "UPDATE prestamos SET devuelto = TRUE WHERE id = ? AND devuelto = FALSE";
        try (PreparedStatement ps = getConexion().prepareStatement(sql)) {
            ps.setInt(1, idPrestamo);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean actualizar(Prestamo p) throws SQLException {
        String sql = "UPDATE prestamos SET id_estudiante = ?, id_libro = ?, fecha_prestamo = ?, "
                   + "fecha_devolucion = ?, devuelto = ? WHERE id = ?";
        try (PreparedStatement ps = getConexion().prepareStatement(sql)) {
            ps.setInt(1, p.getIdEstudiante());
            ps.setInt(2, p.getIdLibro());
            ps.setDate(3, Date.valueOf(p.getFechaPrestamo()));
            ps.setDate(4, Date.valueOf(p.getFechaDevolucion()));
            ps.setBoolean(5, p.isDevuelto());
            ps.setInt(6, p.getId());
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

    private List<Prestamo> consultar(String sql) throws SQLException {
        List<Prestamo> lista = new ArrayList<>();
        try (PreparedStatement ps = getConexion().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    private Prestamo mapear(ResultSet rs) throws SQLException {
        Prestamo p = new Prestamo(
                rs.getInt("id"),
                rs.getInt("id_estudiante"),
                rs.getInt("id_libro"),
                rs.getDate("fecha_prestamo").toLocalDate(),     // java.sql.Date -> LocalDate
                rs.getDate("fecha_devolucion").toLocalDate(),
                rs.getBoolean("devuelto"));
        p.setNombreEstudiante(rs.getString("nombre_estudiante"));
        p.setTituloLibro(rs.getString("titulo_libro"));
        return p;
    }
}
