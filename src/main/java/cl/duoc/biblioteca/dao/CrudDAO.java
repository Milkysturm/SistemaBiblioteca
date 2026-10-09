package cl.duoc.biblioteca.dao;

import java.sql.SQLException;
import java.util.List;

/**
 * INTERFAZ genérica con las operaciones CRUD.
 * Cada DAO la implementa para su entidad (T = Libro, Estudiante, etc.).
 * Así todos los DAO tienen el mismo "contrato" y se pueden usar de la misma forma.
 */
public interface CrudDAO<T> {

    /** Create: inserta un registro nuevo. Devuelve true si se insertó. */
    boolean insertar(T objeto) throws SQLException;

    /** Read: busca un registro por su id. Devuelve null si no existe. */
    T buscarPorId(int id) throws SQLException;

    /** Read: devuelve todos los registros. */
    List<T> listar() throws SQLException;

    /** Update: modifica un registro existente. Devuelve true si se actualizó. */
    boolean actualizar(T objeto) throws SQLException;

    /** Delete: elimina un registro por su id. Devuelve true si se eliminó. */
    boolean eliminar(int id) throws SQLException;
}
