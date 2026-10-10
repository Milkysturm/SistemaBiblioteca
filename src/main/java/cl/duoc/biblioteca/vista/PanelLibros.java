package cl.duoc.biblioteca.vista;

import cl.duoc.biblioteca.controlador.ControladorBiblioteca;
import cl.duoc.biblioteca.modelo.Categoria;
import cl.duoc.biblioteca.modelo.Libro;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.sql.SQLException;
import java.util.List;

/**
 * Gestion de libros: registrar, modificar, eliminar, visualizar y buscar.
 *
 * El estudiante usa este mismo panel en modo consulta: solo ve el buscador y la
 * tabla, sin el formulario.
 *
 * @author Olga Rivas
 * @version 1.0
 */
public final class PanelLibros extends PanelCrud {

    /** Version de la clase, requerida por ser serializable. */
    private static final long serialVersionUID = 1L;

    /** Largo maximo del titulo, el mismo de la columna en la base. */
    private static final int LARGO_TITULO = 200;

    /** Largo maximo del autor y la editorial, el mismo de las columnas en la base. */
    private static final int LARGO_TEXTO = 100;

    /** Largo maximo del ISBN, el mismo de la columna en la base. */
    private static final int LARGO_ISBN = 20;

    /** Stock maximo que se puede registrar de una vez. */
    private static final int STOCK_MAXIMO = 999;

    /** Columna de la tabla con el id del libro. */
    private static final int COL_ID = 0;

    /** Controlador que habla con la base de datos. */
    private final transient ControladorBiblioteca controlador;

    /** true si el usuario puede modificar libros (bibliotecario). */
    private final boolean puedeEditar;

    /** Campo de busqueda por titulo o autor. */
    private final JTextField campoBuscar = new JTextField(22);

    /** Campo del titulo. */
    private final JTextField campoTitulo = new JTextField(20);

    /** Campo del autor. */
    private final JTextField campoAutor = new JTextField(14);

    /** Campo del ISBN. */
    private final JTextField campoIsbn = new JTextField(12);

    /** Campo de la editorial. */
    private final JTextField campoEditorial = new JTextField(12);

    /** Selector del stock disponible. */
    private final JSpinner campoStock = new JSpinner(new SpinnerNumberModel(1, 0, STOCK_MAXIMO, 1));

    /** Selector de la categoria. */
    private final JComboBox<Categoria> comboCategoria = new JComboBox<>();

    /** Tabla con los libros. */
    private final JTable tabla = crearTabla(
            new String[]{"ID", "Titulo", "Autor", "ISBN", "Editorial", "Categoria", "Stock"},
            new Class<?>[]{Integer.class, String.class, String.class, String.class,
                    String.class, String.class, Integer.class});

    /** Etiqueta con el total de libros mostrados. */
    private final JLabel contador = new JLabel();

    /** Libros que se muestran en la tabla, en el mismo orden que el modelo. */
    private transient List<Libro> librosMostrados = List.of();

    /**
     * Construye el panel.
     *
     * @param controlador controlador del sistema
     * @param puedeEditar true para mostrar el formulario de gestion (bibliotecario)
     */
    public PanelLibros(ControladorBiblioteca controlador, boolean puedeEditar) {
        this.controlador = controlador;
        this.puedeEditar = puedeEditar;
        setBorder(MARGEN);
        definirAnchos(tabla, 40, 250, 150, 120, 120, 120, 50);

        JPanel superior = new JPanel();
        superior.setLayout(new BoxLayout(superior, BoxLayout.Y_AXIS));
        if (puedeEditar) {
            superior.add(crearFormulario());
        }
        superior.add(crearBuscador());

        add(superior, BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);
        add(contador, BorderLayout.SOUTH);

        if (puedeEditar) {
            // Al elegir una fila, sus datos pasan al formulario para poder editarla.
            tabla.getSelectionModel().addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting()) {
                    cargarSeleccion();
                }
            });
        }
        recargar();
    }

    /**
     * Crea el formulario con los campos y los botones de gestion.
     *
     * @return panel del formulario
     */
    private JPanel crearFormulario() {
        JPanel fila1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        fila1.add(new JLabel("Titulo:"));
        fila1.add(campoTitulo);
        fila1.add(new JLabel("Autor:"));
        fila1.add(campoAutor);
        fila1.add(new JLabel("Editorial:"));
        fila1.add(campoEditorial);

        JPanel fila2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        fila2.add(new JLabel("ISBN:"));
        fila2.add(campoIsbn);
        fila2.add(new JLabel("Categoria:"));
        fila2.add(comboCategoria);
        fila2.add(new JLabel("Stock:"));
        fila2.add(campoStock);

        JButton botonAgregar = new JButton("Agregar");
        botonAgregar.addActionListener(e -> agregar());
        JButton botonActualizar = new JButton("Actualizar");
        botonActualizar.addActionListener(e -> actualizar());
        JButton botonEliminar = new JButton("Eliminar");
        botonEliminar.addActionListener(e -> eliminar());
        JButton botonLimpiar = new JButton("Limpiar");
        botonLimpiar.addActionListener(e -> limpiar());

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        botones.add(botonAgregar);
        botones.add(botonActualizar);
        botones.add(botonEliminar);
        botones.add(botonLimpiar);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("Datos del libro"));
        panel.add(fila1);
        panel.add(fila2);
        panel.add(botones);
        return panel;
    }

    /**
     * Crea la barra de busqueda por titulo o autor.
     *
     * @return panel del buscador
     */
    private JPanel crearBuscador() {
        JButton botonBuscar = new JButton("Buscar");
        botonBuscar.addActionListener(e -> mostrarLibros());
        campoBuscar.addActionListener(e -> mostrarLibros());

        JButton botonTodos = new JButton("Ver todos");
        botonTodos.addActionListener(e -> {
            campoBuscar.setText("");
            mostrarLibros();
        });

        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        panel.add(new JLabel("Buscar por titulo o autor:"));
        panel.add(campoBuscar);
        panel.add(botonBuscar);
        panel.add(botonTodos);
        return panel;
    }

    /**
     * Recarga las categorias y la tabla de libros.
     */
    @Override
    public void recargar() {
        if (puedeEditar) {
            cargarCategorias();
        }
        mostrarLibros();
    }

    /**
     * Llena el combo de categorias, conservando la que estaba elegida.
     */
    private void cargarCategorias() {
        Categoria elegida = (Categoria) comboCategoria.getSelectedItem();
        comboCategoria.removeAllItems();
        try {
            for (Categoria categoria : controlador.listarCategorias()) {
                comboCategoria.addItem(categoria);
                if (elegida != null && elegida.getId() == categoria.getId()) {
                    comboCategoria.setSelectedItem(categoria);
                }
            }
        } catch (SQLException e) {
            informarError("No se pudieron leer las categorias.", e);
        }
    }

    /**
     * Muestra en la tabla los libros que coinciden con el texto de busqueda.
     */
    private void mostrarLibros() {
        soltarSeleccion(tabla);
        DefaultTableModel modelo = modeloDe(tabla);
        modelo.setRowCount(0);

        try {
            librosMostrados = controlador.buscarLibros(campoBuscar.getText());
        } catch (SQLException e) {
            librosMostrados = List.of();
            contador.setText("No se pudieron leer los libros");
            informarError("No se pudieron leer los libros.", e);
            return;
        }

        for (Libro libro : librosMostrados) {
            modelo.addRow(new Object[]{libro.getId(), libro.getTitulo(), libro.getAutor(),
                    libro.getIsbn(), libro.getEditorial(), libro.getCategoria().getNombre(),
                    libro.getStock()});
        }
        contador.setText(librosMostrados.size() + " libro(s)");
    }

    /**
     * Registra un libro nuevo con los datos del formulario.
     */
    private void agregar() {
        Libro libro = leerFormulario(0);
        if (libro == null) {
            return;
        }
        try {
            controlador.crearLibro(libro);
        } catch (IllegalStateException e) {
            advertir(e.getMessage());
            return;
        } catch (SQLException e) {
            informarError("No se pudo guardar el libro.", e);
            return;
        }
        informar("Libro guardado con el id " + libro.getId() + ".");
        limpiar();
        mostrarLibros();
    }

    /**
     * Guarda los cambios del libro seleccionado.
     */
    private void actualizar() {
        Libro seleccionado = libroSeleccionado();
        if (seleccionado == null) {
            advertir("Selecciona primero un libro en la tabla.");
            return;
        }
        Libro libro = leerFormulario(seleccionado.getId());
        if (libro == null) {
            return;
        }
        try {
            if (controlador.actualizarLibro(libro)) {
                informar("Libro " + libro.getId() + " actualizado.");
            } else {
                advertir("El libro ya no existe en la base de datos.");
            }
        } catch (IllegalStateException e) {
            advertir(e.getMessage());
            return;
        } catch (SQLException e) {
            informarError("No se pudo actualizar el libro.", e);
            return;
        }
        limpiar();
        mostrarLibros();
    }

    /**
     * Elimina el libro seleccionado, pidiendo confirmacion.
     */
    private void eliminar() {
        Libro libro = libroSeleccionado();
        if (libro == null) {
            advertir("Selecciona primero un libro en la tabla.");
            return;
        }
        if (!confirmar("Vas a eliminar \"" + libro.getTitulo() + "\".\n\nContinuar?")) {
            return;
        }
        try {
            if (controlador.eliminarLibro(libro.getId())) {
                informar("Libro eliminado.");
            } else {
                advertir("El libro ya no existe en la base de datos.");
            }
        } catch (IllegalStateException e) {
            advertir(e.getMessage());
            return;
        } catch (SQLException e) {
            informarError("No se pudo eliminar el libro.", e);
            return;
        }
        limpiar();
        mostrarLibros();
    }

    /**
     * Lee y valida el formulario antes de tocar la base.
     *
     * @param id identificador del libro, o 0 si es nuevo
     * @return el libro armado, o null si falta un dato y ya se aviso
     */
    private Libro leerFormulario(int id) {
        String titulo = campoTitulo.getText().trim();
        String autor = campoAutor.getText().trim();
        String isbn = campoIsbn.getText().trim();
        String editorial = campoEditorial.getText().trim();
        Categoria categoria = (Categoria) comboCategoria.getSelectedItem();

        if (!textoValido(titulo, "el titulo", LARGO_TITULO, campoTitulo)
                || !textoValido(autor, "el autor", LARGO_TEXTO, campoAutor)
                || !textoValido(isbn, "el ISBN", LARGO_ISBN, campoIsbn)
                || !textoValido(editorial, "la editorial", LARGO_TEXTO, campoEditorial)) {
            return null;
        }
        if (categoria == null) {
            advertir("Debes elegir una categoria.");
            return null;
        }
        int stock = (Integer) campoStock.getValue();
        return new Libro(id, titulo, autor, isbn, editorial, stock, categoria);
    }

    /**
     * Comprueba que un campo de texto no este vacio ni sea demasiado largo.
     *
     * @param valor  texto escrito
     * @param nombre nombre del dato, para el mensaje
     * @param largo  largo maximo permitido
     * @param campo  campo al que se devuelve el foco si hay error
     * @return true si el texto es valido
     */
    private boolean textoValido(String valor, String nombre, int largo, JTextField campo) {
        if (valor.isEmpty()) {
            advertir("Debes indicar " + nombre + ".");
        } else if (valor.length() > largo) {
            advertir("El campo " + nombre + " no puede tener mas de " + largo + " caracteres.");
        } else {
            return true;
        }
        campo.requestFocusInWindow();
        return false;
    }

    /**
     * @return el libro de la fila seleccionada, o null si no hay seleccion
     */
    private Libro libroSeleccionado() {
        int fila = filaSeleccionada(tabla);
        if (fila < 0) {
            return null;
        }
        int id = (int) modeloDe(tabla).getValueAt(fila, COL_ID);
        for (Libro libro : librosMostrados) {
            if (libro.getId() == id) {
                return libro;
            }
        }
        return null;
    }

    /**
     * Copia al formulario los datos del libro seleccionado.
     */
    private void cargarSeleccion() {
        Libro libro = libroSeleccionado();
        if (libro == null) {
            return;
        }
        campoTitulo.setText(textoDe(libro.getTitulo()));
        campoAutor.setText(textoDe(libro.getAutor()));
        campoIsbn.setText(textoDe(libro.getIsbn()));
        campoEditorial.setText(textoDe(libro.getEditorial()));
        campoStock.setValue(libro.getStock());
        for (int i = 0; i < comboCategoria.getItemCount(); i++) {
            if (comboCategoria.getItemAt(i).getId() == libro.getCategoria().getId()) {
                comboCategoria.setSelectedIndex(i);
                break;
            }
        }
    }

    /**
     * Deja el formulario vacio y sin seleccion.
     */
    private void limpiar() {
        soltarSeleccion(tabla);
        campoTitulo.setText("");
        campoAutor.setText("");
        campoIsbn.setText("");
        campoEditorial.setText("");
        campoStock.setValue(1);
        campoTitulo.requestFocusInWindow();
    }
}
