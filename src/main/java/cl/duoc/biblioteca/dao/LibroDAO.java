package cl.duoc.biblioteca.dao;

import cl.duoc.biblioteca.modelo.Categoria;
import cl.duoc.biblioteca.modelo.Libro;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** Acceso a datos de la tabla libros (con su categoría). */
public class LibroDAO extends BaseDAO implements CrudDAO<Libro> {

    // JOIN para traer el nombre de la categoría junto con el libro
    private static final String SELECT_BASE =
            "SELECT l.id, l.titulo, l.autor, l.isbn, l.editorial, l.stock, "
            + "c.id AS cat_id, c.nombre AS cat_nombre "
            + "FROM libros l LEFT JOIN categorias c ON l.id_categoria = c.id ";

    @Override
    public boolean insertar(Libro l) throws SQLException {
        String sql = "INSERT INTO libros (titulo, autor, isbn, editorial, stock, id_categoria) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = getConexion().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            llenarParametros(ps, l);
            int filas = ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) l.setId(keys.getInt(1));
            }
            return filas > 0;
        }
    }

    @Override
    public Libro buscarPorId(int id) throws SQLException {
        try (PreparedStatement ps = getConexion().prepareStatement(SELECT_BASE + "WHERE l.id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    @Override
    public List<Libro> listar() throws SQLException {
        List<Libro> lista = new ArrayList<>();
        try (PreparedStatement ps = getConexion().prepareStatement(SELECT_BASE + "ORDER BY l.titulo");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    /** Busca libros cuyo título o autor contenga el texto. */
    public List<Libro> buscar(String texto) throws SQLException {
        List<Libro> lista = new ArrayList<>();
        String sql = SELECT_BASE + "WHERE l.titulo LIKE ? OR l.autor LIKE ? ORDER BY l.titulo";
        try (PreparedStatement ps = getConexion().prepareStatement(sql)) {
            ps.setString(1, "%" + texto + "%");
            ps.setString(2, "%" + texto + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        }
        return lista;
    }

    @Override
    public boolean actualizar(Libro l) throws SQLException {
        String sql = "UPDATE libros SET titulo = ?, autor = ?, isbn = ?, editorial = ?, "
                   + "stock = ?, id_categoria = ? WHERE id = ?";
        try (PreparedStatement ps = getConexion().prepareStatement(sql)) {
            llenarParametros(ps, l);
            ps.setInt(7, l.getId());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean eliminar(int id) throws SQLException {
        String sql = "DELETE FROM libros WHERE id = ?";
        try (PreparedStatement ps = getConexion().prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Suma (o resta, si cambio es negativo) unidades al stock.
     * La condición "stock + ? >= 0" impide que el stock quede negativo.
     * Este método lo usará el servicio de préstamos (paso 6) dentro de un bloque synchronized.
     */
    public boolean cambiarStock(int idLibro, int cambio) throws SQLException {
        String sql = "UPDATE libros SET stock = stock + ? WHERE id = ? AND stock + ? >= 0";
        try (PreparedStatement ps = getConexion().prepareStatement(sql)) {
            ps.setInt(1, cambio);
            ps.setInt(2, idLibro);
            ps.setInt(3, cambio);
            return ps.executeUpdate() > 0;
        }
    }

    private void llenarParametros(PreparedStatement ps, Libro l) throws SQLException {
        ps.setString(1, l.getTitulo());
        ps.setString(2, l.getAutor());
        ps.setString(3, l.getIsbn());
        ps.setString(4, l.getEditorial());
        ps.setInt(5, l.getStock());
        if (l.getCategoria() != null) {
            ps.setInt(6, l.getCategoria().getId());
        } else {
            ps.setNull(6, Types.INTEGER);
        }
    }

    private Libro mapear(ResultSet rs) throws SQLException {
        Categoria cat = new Categoria(rs.getInt("cat_id"), rs.getString("cat_nombre"));
        return new Libro(rs.getInt("id"), rs.getString("titulo"), rs.getString("autor"),
                rs.getString("isbn"), rs.getString("editorial"), rs.getInt("stock"), cat);
    }
}
