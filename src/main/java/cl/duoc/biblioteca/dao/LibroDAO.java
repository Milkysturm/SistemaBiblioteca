package cl.duoc.biblioteca.dao;

import cl.duoc.biblioteca.modelo.Categoria;
import cl.duoc.biblioteca.modelo.Libro;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a la tabla libros, junto con el nombre de su categoria.
 *
 * @author Olga Rivas
 * @version 1.0
 */
public class LibroDAO extends BaseDAO implements CrudDAO<Libro> {

    /** Consulta base: el JOIN trae el nombre de la categoria de cada libro. */
    private static final String SELECT_BASE =
            "SELECT l.id, l.titulo, l.autor, l.isbn, l.editorial, l.stock, "
            + "c.id AS cat_id, c.nombre AS cat_nombre "
            + "FROM libros l LEFT JOIN categorias c ON l.id_categoria = c.id ";

    @Override
    public boolean insertar(Libro libro) throws SQLException {
        String sql = "INSERT INTO libros (titulo, autor, isbn, editorial, stock, id_categoria) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = getConexion().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            llenarParametros(ps, libro);
            int filas = ps.executeUpdate();
            libro.setId(idGenerado(ps));
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

    /**
     * @param isbn codigo ISBN
     * @return el libro con ese ISBN, o null si no existe
     * @throws SQLException si falla la consulta
     */
    public Libro buscarPorIsbn(String isbn) throws SQLException {
        try (PreparedStatement ps = getConexion().prepareStatement(SELECT_BASE + "WHERE l.isbn = ?")) {
            ps.setString(1, isbn);
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
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    /**
     * Busca libros cuyo titulo o autor contenga el texto.
     *
     * @param texto texto a buscar
     * @return libros que coinciden, ordenados por titulo
     * @throws SQLException si falla la consulta
     */
    public List<Libro> buscar(String texto) throws SQLException {
        String sql = SELECT_BASE + "WHERE l.titulo LIKE ? OR l.autor LIKE ? ORDER BY l.titulo";
        List<Libro> lista = new ArrayList<>();
        try (PreparedStatement ps = getConexion().prepareStatement(sql)) {
            ps.setString(1, "%" + texto + "%");
            ps.setString(2, "%" + texto + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    @Override
    public boolean actualizar(Libro libro) throws SQLException {
        String sql = "UPDATE libros SET titulo = ?, autor = ?, isbn = ?, editorial = ?, "
                + "stock = ?, id_categoria = ? WHERE id = ?";
        try (PreparedStatement ps = getConexion().prepareStatement(sql)) {
            llenarParametros(ps, libro);
            ps.setInt(7, libro.getId());
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
     * Suma unidades al stock, o las resta si el cambio es negativo.
     *
     * La condicion "stock + ? >= 0" impide que el stock quede negativo.
     *
     * @param idLibro libro a modificar
     * @param cambio  unidades a sumar (negativo para restar)
     * @return true si se aplico; false si no habia stock suficiente o el libro no existe
     * @throws SQLException si falla la actualizacion
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

    /**
     * Copia los datos del libro en los seis primeros parametros de la sentencia.
     *
     * @param ps    sentencia de INSERT o UPDATE
     * @param libro libro con los datos
     * @throws SQLException si falla la asignacion
     */
    private void llenarParametros(PreparedStatement ps, Libro libro) throws SQLException {
        ps.setString(1, libro.getTitulo());
        ps.setString(2, libro.getAutor());
        ps.setString(3, libro.getIsbn());
        ps.setString(4, libro.getEditorial());
        ps.setInt(5, libro.getStock());
        ps.setInt(6, libro.getCategoria().getId());
    }

    /**
     * Convierte la fila actual en un Libro con su Categoria.
     *
     * @param rs resultado posicionado en una fila
     * @return el libro leido
     * @throws SQLException si falla la lectura
     */
    private Libro mapear(ResultSet rs) throws SQLException {
        Categoria categoria = new Categoria(rs.getInt("cat_id"), rs.getString("cat_nombre"));
        return new Libro(rs.getInt("id"), rs.getString("titulo"), rs.getString("autor"),
                rs.getString("isbn"), rs.getString("editorial"), rs.getInt("stock"), categoria);
    }
}
