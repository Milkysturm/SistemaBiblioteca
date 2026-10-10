package cl.duoc.biblioteca.dao;

import cl.duoc.biblioteca.conexion.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Base comun de todos los DAO.
 *
 * Centraliza como se obtiene la conexion (siempre desde el Singleton) y como se
 * recupera el id generado al insertar, para no repetirlo en cada DAO.
 *
 * @author Olga Rivas
 * @version 1.0
 */
public abstract class BaseDAO {

    /**
     * @return la conexion unica del Singleton
     * @throws SQLException si no se puede conectar
     */
    protected Connection getConexion() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    /**
     * Lee el id que MySQL asigno (AUTO_INCREMENT) despues de un INSERT.
     *
     * @param sentencia sentencia preparada con RETURN_GENERATED_KEYS
     * @return el id generado, o 0 si no hay
     * @throws SQLException si falla la lectura
     */
    protected int idGenerado(PreparedStatement sentencia) throws SQLException {
        try (ResultSet claves = sentencia.getGeneratedKeys()) {
            return claves.next() ? claves.getInt(1) : 0;
        }
    }
}
