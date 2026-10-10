package cl.duoc.biblioteca.dao;

import java.sql.SQLException;
import java.util.List;

/**
 * Operaciones CRUD que todo DAO debe ofrecer.
 *
 * Es generica: T se reemplaza por la entidad de cada DAO (Libro, Estudiante, etc.).
 *
 * @param <T> entidad que maneja el DAO
 * @author Olga Rivas
 * @version 1.0
 */
public interface CrudDAO<T> {

    /**
     * Inserta un registro nuevo y le asigna el id generado por la base.
     *
     * @param objeto registro a insertar
     * @return true si se inserto
     * @throws SQLException si falla la insercion
     */
    boolean insertar(T objeto) throws SQLException;

    /**
     * @param id identificador del registro
     * @return el registro, o null si no existe
     * @throws SQLException si falla la consulta
     */
    T buscarPorId(int id) throws SQLException;

    /**
     * @return todos los registros
     * @throws SQLException si falla la consulta
     */
    List<T> listar() throws SQLException;

    /**
     * @param objeto registro con los datos nuevos
     * @return true si el registro existia y se actualizo
     * @throws SQLException si falla la actualizacion
     */
    boolean actualizar(T objeto) throws SQLException;

    /**
     * @param id identificador del registro
     * @return true si el registro existia y se elimino
     * @throws SQLException si falla la eliminacion
     */
    boolean eliminar(int id) throws SQLException;
}
