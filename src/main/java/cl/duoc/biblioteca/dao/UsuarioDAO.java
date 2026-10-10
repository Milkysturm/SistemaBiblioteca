package cl.duoc.biblioteca.dao;

import cl.duoc.biblioteca.modelo.Rol;
import cl.duoc.biblioteca.modelo.Usuario;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a la tabla usuarios.
 *
 * @author Olga Rivas
 * @version 1.0
 */
public class UsuarioDAO extends BaseDAO implements CrudDAO<Usuario> {

    /**
     * Nombre de la columna de la contrasena. En la base lleva enie, por eso se
     * escribe con el codigo unicode de la enie (Java lo convierte) y va entre comillas invertidas.
     */
    private static final String COLUMNA_CLAVE = "`contrase\u00f1a`";

    /** Columnas que se leen en todas las consultas. */
    private static final String SELECT_BASE =
            "SELECT id, nombre, rut, correo, " + COLUMNA_CLAVE + " AS clave, rol FROM usuarios ";

    /**
     * Valida el inicio de sesion.
     *
     * @param correo     correo del usuario
     * @param contrasena contrasena escrita
     * @return el usuario si los datos coinciden, o null si no
     * @throws SQLException si falla la consulta
     */
    public Usuario autenticar(String correo, String contrasena) throws SQLException {
        String sql = SELECT_BASE + "WHERE correo = ? AND " + COLUMNA_CLAVE + " = ?";
        try (PreparedStatement ps = getConexion().prepareStatement(sql)) {
            ps.setString(1, correo);
            ps.setString(2, contrasena);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    /**
     * @param rut RUT con guion
     * @return el usuario con ese RUT, o null si no existe
     * @throws SQLException si falla la consulta
     */
    public Usuario buscarPorRut(String rut) throws SQLException {
        try (PreparedStatement ps = getConexion().prepareStatement(SELECT_BASE + "WHERE rut = ?")) {
            ps.setString(1, rut);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    /**
     * @param correo correo electronico
     * @return el usuario con ese correo, o null si no existe
     * @throws SQLException si falla la consulta
     */
    public Usuario buscarPorCorreo(String correo) throws SQLException {
        try (PreparedStatement ps = getConexion().prepareStatement(SELECT_BASE + "WHERE correo = ?")) {
            ps.setString(1, correo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    @Override
    public boolean insertar(Usuario usuario) throws SQLException {
        String sql = "INSERT INTO usuarios (nombre, rut, correo, " + COLUMNA_CLAVE + ", rol) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = getConexion().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            llenarParametros(ps, usuario);
            int filas = ps.executeUpdate();
            usuario.setId(idGenerado(ps));
            return filas > 0;
        }
    }

    @Override
    public Usuario buscarPorId(int id) throws SQLException {
        try (PreparedStatement ps = getConexion().prepareStatement(SELECT_BASE + "WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    @Override
    public List<Usuario> listar() throws SQLException {
        List<Usuario> lista = new ArrayList<>();
        try (PreparedStatement ps = getConexion().prepareStatement(SELECT_BASE + "ORDER BY nombre");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    @Override
    public boolean actualizar(Usuario usuario) throws SQLException {
        String sql = "UPDATE usuarios SET nombre = ?, rut = ?, correo = ?, " + COLUMNA_CLAVE + " = ?, rol = ? "
                + "WHERE id = ?";
        try (PreparedStatement ps = getConexion().prepareStatement(sql)) {
            llenarParametros(ps, usuario);
            ps.setInt(6, usuario.getId());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean eliminar(int id) throws SQLException {
        String sql = "DELETE FROM usuarios WHERE id = ?";
        try (PreparedStatement ps = getConexion().prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Copia los datos del usuario en los cinco primeros parametros de la sentencia.
     *
     * @param ps      sentencia de INSERT o UPDATE
     * @param usuario usuario con los datos
     * @throws SQLException si falla la asignacion
     */
    private void llenarParametros(PreparedStatement ps, Usuario usuario) throws SQLException {
        ps.setString(1, usuario.getNombre());
        ps.setString(2, usuario.getRut());
        ps.setString(3, usuario.getCorreo());
        ps.setString(4, usuario.getContrasena());
        ps.setString(5, usuario.getRol().getValorBD());
    }

    /**
     * Convierte la fila actual en un Usuario.
     *
     * @param rs resultado posicionado en una fila
     * @return el usuario leido
     * @throws SQLException si falla la lectura
     */
    private Usuario mapear(ResultSet rs) throws SQLException {
        return new Usuario(rs.getInt("id"), rs.getString("nombre"), rs.getString("rut"),
                rs.getString("correo"), rs.getString("clave"), Rol.desdeBD(rs.getString("rol")));
    }
}
