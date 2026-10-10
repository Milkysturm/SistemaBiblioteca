package cl.duoc.biblioteca.controlador;

import cl.duoc.biblioteca.dao.CategoriaDAO;
import cl.duoc.biblioteca.dao.EstudianteDAO;
import cl.duoc.biblioteca.dao.LibroDAO;
import cl.duoc.biblioteca.dao.PrestamoDAO;
import cl.duoc.biblioteca.dao.UsuarioDAO;
import cl.duoc.biblioteca.modelo.Categoria;
import cl.duoc.biblioteca.modelo.Estudiante;
import cl.duoc.biblioteca.modelo.Libro;
import cl.duoc.biblioteca.modelo.Prestamo;
import cl.duoc.biblioteca.modelo.Rol;
import cl.duoc.biblioteca.modelo.Usuario;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Intermediario entre las ventanas y los DAO (la C del patron MVC).
 *
 * Las ventanas le piden acciones y el controlador aplica las reglas del negocio
 * antes de llamar al DAO que corresponde. No contiene codigo de Swing ni SQL.
 *
 * @author Olga Rivas
 * @version 1.0
 */
public class ControladorBiblioteca {

    /** Acceso a la tabla libros. */
    private final LibroDAO libroDAO = new LibroDAO();

    /** Acceso a la tabla categorias. */
    private final CategoriaDAO categoriaDAO = new CategoriaDAO();

    /** Acceso a la tabla estudiantes. */
    private final EstudianteDAO estudianteDAO = new EstudianteDAO();

    /** Acceso a la tabla usuarios. */
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    /** Acceso a la tabla prestamos. */
    private final PrestamoDAO prestamoDAO = new PrestamoDAO();

    /**
     * Prestamos y devoluciones. Es una sola instancia para toda la aplicacion, asi
     * sus metodos synchronized coordinan a todos los hilos que modifican el stock.
     */
    private final ServicioPrestamos servicioPrestamos = new ServicioPrestamos();

    // ------------------------------------------------------------------
    // Autenticacion
    // ------------------------------------------------------------------

    /**
     * Valida el inicio de sesion contra la base de datos.
     *
     * @param correo     correo del usuario
     * @param contrasena contrasena escrita
     * @return el usuario, o null si el correo o la contrasena no coinciden
     * @throws SQLException si falla la consulta
     */
    public Usuario iniciarSesion(String correo, String contrasena) throws SQLException {
        return usuarioDAO.autenticar(correo, contrasena);
    }

    /**
     * Busca la ficha de estudiante de un usuario, relacionandolos por el RUT.
     *
     * @param usuario usuario con sesion iniciada
     * @return su ficha de estudiante, o null si no tiene
     * @throws SQLException si falla la consulta
     */
    public Estudiante fichaDe(Usuario usuario) throws SQLException {
        return estudianteDAO.buscarPorRut(usuario.getRut());
    }

    // ------------------------------------------------------------------
    // Libros y categorias
    // ------------------------------------------------------------------

    /**
     * @return todas las categorias, ordenadas por nombre
     * @throws SQLException si falla la consulta
     */
    public List<Categoria> listarCategorias() throws SQLException {
        return categoriaDAO.listar();
    }

    /**
     * @return todos los libros, ordenados por titulo
     * @throws SQLException si falla la consulta
     */
    public List<Libro> listarLibros() throws SQLException {
        return libroDAO.listar();
    }

    /**
     * Busca libros por titulo o autor. Con el texto vacio devuelve todos.
     *
     * @param texto texto a buscar
     * @return libros que coinciden
     * @throws SQLException si falla la consulta
     */
    public List<Libro> buscarLibros(String texto) throws SQLException {
        return texto.isBlank() ? libroDAO.listar() : libroDAO.buscar(texto.trim());
    }

    /**
     * Registra un libro nuevo.
     *
     * @param libro libro a guardar
     * @throws SQLException          si falla la insercion
     * @throws IllegalStateException si ya existe un libro con el mismo ISBN
     */
    public void crearLibro(Libro libro) throws SQLException {
        validarIsbnLibre(libro);
        libroDAO.insertar(libro);
    }

    /**
     * Guarda los cambios de un libro.
     *
     * @param libro libro con los datos nuevos
     * @return true si el libro existia y se actualizo
     * @throws SQLException          si falla la actualizacion
     * @throws IllegalStateException si otro libro ya tiene el mismo ISBN
     */
    public boolean actualizarLibro(Libro libro) throws SQLException {
        validarIsbnLibre(libro);
        return libroDAO.actualizar(libro);
    }

    /**
     * Elimina un libro, siempre que no tenga prestamos registrados.
     *
     * @param idLibro libro a eliminar
     * @return true si se elimino
     * @throws SQLException          si falla la eliminacion
     * @throws IllegalStateException si el libro tiene prestamos en su historial
     */
    public boolean eliminarLibro(int idLibro) throws SQLException {
        int prestamos = prestamoDAO.contarPorLibro(idLibro);
        if (prestamos > 0) {
            throw new IllegalStateException("Este libro tiene " + prestamos
                    + " prestamo(s) en su historial.\nNo se puede eliminar sin perder ese registro.");
        }
        return libroDAO.eliminar(idLibro);
    }

    /**
     * Comprueba que ningun otro libro use el ISBN.
     *
     * @param libro libro que se quiere guardar
     * @throws SQLException          si falla la consulta
     * @throws IllegalStateException si el ISBN ya esta en uso
     */
    private void validarIsbnLibre(Libro libro) throws SQLException {
        Libro existente = libroDAO.buscarPorIsbn(libro.getIsbn());
        if (existente != null && existente.getId() != libro.getId()) {
            throw new IllegalStateException("El ISBN " + libro.getIsbn()
                    + " ya pertenece al libro \"" + existente.getTitulo() + "\".");
        }
    }

    // ------------------------------------------------------------------
    // Estudiantes
    // ------------------------------------------------------------------

    /**
     * @return todos los estudiantes, ordenados por nombre
     * @throws SQLException si falla la consulta
     */
    public List<Estudiante> listarEstudiantes() throws SQLException {
        return estudianteDAO.listar();
    }

    /**
     * @return RUT de todos los usuarios con rol estudiante, para saber quien tiene cuenta
     * @throws SQLException si falla la consulta
     */
    public Set<String> rutsConCuenta() throws SQLException {
        Set<String> ruts = new HashSet<>();
        for (Usuario usuario : usuarioDAO.listar()) {
            if (usuario.getRol() == Rol.ESTUDIANTE) {
                ruts.add(usuario.getRut());
            }
        }
        return ruts;
    }

    /**
     * Registra un estudiante y, si se indica una contrasena, tambien su cuenta de
     * usuario para que pueda iniciar sesion.
     *
     * @param estudiante estudiante a guardar
     * @param contrasena contrasena de acceso, o vacia para no crear cuenta
     * @throws SQLException          si falla la insercion
     * @throws IllegalStateException si el RUT o el correo ya estan en uso
     */
    public void crearEstudiante(Estudiante estudiante, String contrasena) throws SQLException {
        validarRutLibre(estudiante);
        boolean crearCuenta = !contrasena.isBlank();
        if (crearCuenta) {
            validarCuentaLibre(estudiante.getRut(), estudiante.getCorreo(), null);
        }

        estudianteDAO.insertar(estudiante);
        if (crearCuenta) {
            usuarioDAO.insertar(cuentaPara(estudiante, contrasena));
        }
    }

    /**
     * Guarda los cambios de un estudiante y mantiene al dia su cuenta de usuario.
     *
     * @param estudiante  estudiante con los datos nuevos
     * @param rutAnterior RUT que tenia antes del cambio, para encontrar su cuenta
     * @param contrasena  contrasena nueva, o vacia para no cambiarla
     * @return true si el estudiante existia y se actualizo
     * @throws SQLException          si falla la actualizacion
     * @throws IllegalStateException si el RUT o el correo ya estan en uso
     */
    public boolean actualizarEstudiante(Estudiante estudiante, String rutAnterior, String contrasena)
            throws SQLException {
        validarRutLibre(estudiante);
        Usuario cuenta = cuentaDeEstudiante(rutAnterior);
        if (cuenta != null || !contrasena.isBlank()) {
            validarCuentaLibre(estudiante.getRut(), estudiante.getCorreo(), cuenta);
        }

        if (!estudianteDAO.actualizar(estudiante)) {
            return false;
        }

        if (cuenta != null) {
            cuenta.setNombre(estudiante.getNombre());
            cuenta.setRut(estudiante.getRut());
            cuenta.setCorreo(estudiante.getCorreo());
            if (!contrasena.isBlank()) {
                cuenta.setContrasena(contrasena);
            }
            usuarioDAO.actualizar(cuenta);
        } else if (!contrasena.isBlank()) {
            usuarioDAO.insertar(cuentaPara(estudiante, contrasena));
        }
        return true;
    }

    /**
     * Elimina un estudiante y su cuenta, siempre que no tenga prestamos registrados.
     *
     * @param idEstudiante estudiante a eliminar
     * @return true si se elimino
     * @throws SQLException          si falla la eliminacion
     * @throws IllegalStateException si el estudiante tiene prestamos en su historial
     */
    public boolean eliminarEstudiante(int idEstudiante) throws SQLException {
        int prestamos = prestamoDAO.contarPorEstudiante(idEstudiante);
        if (prestamos > 0) {
            throw new IllegalStateException("Este estudiante tiene " + prestamos
                    + " prestamo(s) en su historial.\nNo se puede eliminar sin perder ese registro.");
        }

        Estudiante estudiante = estudianteDAO.buscarPorId(idEstudiante);
        if (estudiante == null) {
            return false;
        }
        Usuario cuenta = cuentaDeEstudiante(estudiante.getRut());
        if (cuenta != null) {
            usuarioDAO.eliminar(cuenta.getId());
        }
        return estudianteDAO.eliminar(idEstudiante);
    }

    /**
     * Comprueba que ningun otro estudiante use el RUT.
     *
     * @param estudiante estudiante que se quiere guardar
     * @throws SQLException          si falla la consulta
     * @throws IllegalStateException si el RUT ya esta en uso
     */
    private void validarRutLibre(Estudiante estudiante) throws SQLException {
        Estudiante existente = estudianteDAO.buscarPorRut(estudiante.getRut());
        if (existente != null && existente.getId() != estudiante.getId()) {
            throw new IllegalStateException("El RUT " + estudiante.getRut()
                    + " ya pertenece a " + existente.getNombre() + ".");
        }
    }

    /**
     * Comprueba que el RUT y el correo no los use otra cuenta de usuario.
     *
     * @param rut    RUT de la cuenta
     * @param correo correo de la cuenta, que se usa para iniciar sesion
     * @param propia cuenta del mismo estudiante, que no cuenta como repetida (puede ser null)
     * @throws SQLException          si falla la consulta
     * @throws IllegalStateException si el RUT o el correo ya estan en uso
     */
    private void validarCuentaLibre(String rut, String correo, Usuario propia) throws SQLException {
        int idPropia = (propia == null) ? 0 : propia.getId();

        Usuario porRut = usuarioDAO.buscarPorRut(rut);
        if (porRut != null && porRut.getId() != idPropia) {
            throw new IllegalStateException("Ya existe una cuenta de usuario con el RUT " + rut + ".");
        }
        Usuario porCorreo = usuarioDAO.buscarPorCorreo(correo);
        if (porCorreo != null && porCorreo.getId() != idPropia) {
            throw new IllegalStateException("El correo " + correo + " ya lo usa otra cuenta.");
        }
    }

    /**
     * @param rut RUT del estudiante
     * @return su cuenta con rol estudiante, o null si no tiene
     * @throws SQLException si falla la consulta
     */
    private Usuario cuentaDeEstudiante(String rut) throws SQLException {
        Usuario cuenta = usuarioDAO.buscarPorRut(rut);
        return (cuenta != null && cuenta.getRol() == Rol.ESTUDIANTE) ? cuenta : null;
    }

    /**
     * @param estudiante estudiante dueno de la cuenta
     * @param contrasena contrasena de acceso
     * @return cuenta de usuario con rol estudiante y los datos del estudiante
     */
    private Usuario cuentaPara(Estudiante estudiante, String contrasena) {
        return new Usuario(0, estudiante.getNombre(), estudiante.getRut(),
                estudiante.getCorreo(), contrasena, Rol.ESTUDIANTE);
    }

    // ------------------------------------------------------------------
    // Prestamos y devoluciones
    // ------------------------------------------------------------------

    /**
     * @return libros con al menos un ejemplar disponible
     * @throws SQLException si falla la consulta
     */
    public List<Libro> listarLibrosDisponibles() throws SQLException {
        List<Libro> disponibles = libroDAO.listar();
        disponibles.removeIf(libro -> !libro.hayStock());
        return disponibles;
    }

    /**
     * @return prestamos aun no devueltos (libros actualmente en prestamo)
     * @throws SQLException si falla la consulta
     */
    public List<Prestamo> listarPrestamosActivos() throws SQLException {
        return prestamoDAO.listarActivos();
    }

    /**
     * @return todos los prestamos, del mas reciente al mas antiguo
     * @throws SQLException si falla la consulta
     */
    public List<Prestamo> listarPrestamos() throws SQLException {
        return prestamoDAO.listar();
    }

    /**
     * @param idEstudiante estudiante a consultar
     * @return historial de prestamos del estudiante
     * @throws SQLException si falla la consulta
     */
    public List<Prestamo> historialDe(int idEstudiante) throws SQLException {
        return prestamoDAO.listarPorEstudiante(idEstudiante);
    }

    /**
     * Registra un prestamo (metodo synchronized del servicio).
     *
     * @param idEstudiante estudiante que pide el libro
     * @param idLibro      libro solicitado
     * @return el prestamo registrado
     * @throws SQLException          si falla la base de datos
     * @throws IllegalStateException si no hay stock o el prestamo no esta permitido
     */
    public Prestamo registrarPrestamo(int idEstudiante, int idLibro) throws SQLException {
        return servicioPrestamos.registrarPrestamo(idEstudiante, idLibro);
    }

    /**
     * Registra una devolucion (metodo synchronized del servicio).
     *
     * @param idPrestamo prestamo a cerrar
     * @return el prestamo cerrado, con su atraso si lo hubo
     * @throws SQLException          si falla la base de datos
     * @throws IllegalStateException si el prestamo no existe o ya estaba devuelto
     */
    public Prestamo registrarDevolucion(int idPrestamo) throws SQLException {
        return servicioPrestamos.registrarDevolucion(idPrestamo);
    }
}
