package cl.duoc.biblioteca.dao;

import cl.duoc.biblioteca.conexion.DatabaseConnection;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Clase ABSTRACTA base de todos los DAO.
 * Centraliza cómo se obtiene la conexión (siempre desde el Singleton),
 * para no repetir ese código en cada DAO.
 */
public abstract class BaseDAO {

    protected Connection getConexion() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }
}
