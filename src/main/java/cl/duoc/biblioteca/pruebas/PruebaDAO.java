package cl.duoc.biblioteca.pruebas;

import cl.duoc.biblioteca.dao.*;
import cl.duoc.biblioteca.modelo.*;

import java.sql.SQLException;
import java.util.List;

/**
 * Pruebas funcionales de la capa DAO (Paso 4 de la pauta: "pruebas de cada módulo por separado").
 * Ejecutar con el botón ▶ junto a main.
 */
public class PruebaDAO {

    public static void main(String[] args) {
        try {
            probarLogin();
            probarLibros();
            probarCrudCategoria();
            probarEstudianteYPrestamos();
            probarPolimorfismoInterfaz();
            System.out.println("\n✅ Todas las pruebas terminaron sin errores.");
        } catch (SQLException e) {
            System.out.println("❌ Error de base de datos: " + e.getMessage());
        }
    }

    private static void titulo(String texto) {
        System.out.println("\n===== " + texto + " =====");
    }

    private static void probarLogin() throws SQLException {
        titulo("1. Login (UsuarioDAO.autenticar)");
        UsuarioDAO dao = new UsuarioDAO();

        Usuario ok = dao.autenticar("antonia@correo.cl", "clave123");
        System.out.println("Clave correcta   -> " + (ok != null ? ok.getResumen() : "RECHAZADO"));

        Usuario malo = dao.autenticar("antonia@correo.cl", "otraClave");
        System.out.println("Clave incorrecta -> " + (malo != null ? malo.getResumen() : "RECHAZADO"));
    }

    private static void probarLibros() throws SQLException {
        titulo("2. Listar libros con su categoría (LibroDAO.listar)");
        for (Libro l : new LibroDAO().listar()) {
            System.out.printf("%-45s %-18s stock: %d%n",
                    l.getTitulo(), l.getCategoria().getNombre(), l.getStock());
        }
    }

    private static void probarCrudCategoria() throws SQLException {
        titulo("3. CRUD completo con CategoriaDAO");
        CategoriaDAO dao = new CategoriaDAO();

        Categoria c = new Categoria(0, "Categoría de prueba");
        System.out.println("Insertar   -> " + dao.insertar(c) + " (id asignado: " + c.getId() + ")");
        System.out.println("Buscar     -> " + dao.buscarPorId(c.getId()));

        c.setNombre("Categoría modificada");
        System.out.println("Actualizar -> " + dao.actualizar(c) + " / ahora: " + dao.buscarPorId(c.getId()));

        System.out.println("Eliminar   -> " + dao.eliminar(c.getId()));
        System.out.println("Buscar de nuevo -> " + dao.buscarPorId(c.getId()) + " (null = ya no existe)");
    }

    private static void probarEstudianteYPrestamos() throws SQLException {
        titulo("4. Usuario estudiante -> su ficha y su historial");
        Usuario carlos = new UsuarioDAO().autenticar("carlos@correo.cl", "clave123");
        Estudiante ficha = new EstudianteDAO().buscarPorRut(carlos.getRut());   // unión por RUT
        System.out.println("Usuario: " + carlos.getNombre() + " -> ficha: " + ficha.getResumen());

        List<Prestamo> historial = new PrestamoDAO().listarPorEstudiante(ficha.getId());
        for (Prestamo p : historial) {
            System.out.println("  - " + p.getTituloLibro() + " | vence " + p.getFechaDevolucion()
                    + " | devuelto: " + p.isDevuelto()
                    + (p.estaAtrasado() ? " | ATRASADO " + p.getDiasAtraso() + " días" : ""));
        }

        titulo("5. Libros actualmente en préstamo (PrestamoDAO.listarActivos)");
        for (Prestamo p : new PrestamoDAO().listarActivos()) {
            System.out.println("  - " + p.getTituloLibro() + " -> " + p.getNombreEstudiante()
                    + (p.estaAtrasado() ? " (atrasado " + p.getDiasAtraso() + " días)" : ""));
        }
    }

    private static void probarPolimorfismoInterfaz() throws SQLException {
        titulo("6. Polimorfismo con la interfaz CrudDAO");
        // Distintos DAO guardados como el MISMO tipo: la interfaz CrudDAO
        CrudDAO<?>[] daos = {
                new LibroDAO(), new EstudianteDAO(), new CategoriaDAO(), new UsuarioDAO(), new PrestamoDAO()
        };
        for (CrudDAO<?> dao : daos) {
            System.out.println(dao.getClass().getSimpleName() + " -> " + dao.listar().size() + " registros");
        }
    }
}
