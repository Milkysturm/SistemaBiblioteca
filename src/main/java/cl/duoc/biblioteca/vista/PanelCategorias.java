package cl.duoc.biblioteca.vista;

import cl.duoc.biblioteca.controlador.ControladorBiblioteca;
import cl.duoc.biblioteca.modelo.Categoria;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.sql.SQLException;
import java.util.List;

/**
 * Gestion de categorias de libros: registrar, modificar, eliminar y visualizar.
 *
 * @author Olga Rivas
 * @version 1.0
 */
public final class PanelCategorias extends PanelCrud {

    /** Version de la clase, requerida por ser serializable. */
    private static final long serialVersionUID = 1L;

    /** Largo maximo del nombre, el mismo de la columna en la base. */
    private static final int LARGO_NOMBRE = 100;

    /** Columna de la tabla con el id de la categoria. */
    private static final int COL_ID = 0;

    /** Columna de la tabla con el nombre. */
    private static final int COL_NOMBRE = 1;

    /** Controlador que habla con la base de datos. */
    private final transient ControladorBiblioteca controlador;

    /** Campo del nombre de la categoria. */
    private final JTextField campoNombre = new JTextField(24);

    /** Tabla con las categorias. */
    private final JTable tabla = crearTabla(
            new String[]{"ID", "Nombre", "Libros"},
            new Class<?>[]{Integer.class, String.class, Integer.class});

    /** Etiqueta con el total de categorias. */
    private final JLabel contador = new JLabel();

    /**
     * Construye el panel.
     *
     * @param controlador controlador del sistema
     */
    public PanelCategorias(ControladorBiblioteca controlador) {
        this.controlador = controlador;
        setBorder(MARGEN);
        definirAnchos(tabla, 40, 400, 80);

        add(crearFormulario(), BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);
        add(contador, BorderLayout.SOUTH);

        // Al elegir una fila, su nombre pasa al formulario para poder editarlo.
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int fila = filaSeleccionada(tabla);
                if (fila >= 0) {
                    campoNombre.setText(textoDe(modeloDe(tabla).getValueAt(fila, COL_NOMBRE)));
                }
            }
        });
        recargar();
    }

    /**
     * Crea el formulario con el campo y los botones.
     *
     * @return panel del formulario
     */
    private JPanel crearFormulario() {
        JButton botonAgregar = new JButton("Agregar");
        botonAgregar.addActionListener(e -> agregar());
        JButton botonActualizar = new JButton("Actualizar");
        botonActualizar.addActionListener(e -> actualizar());
        JButton botonEliminar = new JButton("Eliminar");
        botonEliminar.addActionListener(e -> eliminar());
        JButton botonLimpiar = new JButton("Limpiar");
        botonLimpiar.addActionListener(e -> limpiar());

        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        panel.setBorder(BorderFactory.createTitledBorder("Datos de la categoria"));
        panel.add(new JLabel("Nombre:"));
        panel.add(campoNombre);
        panel.add(botonAgregar);
        panel.add(botonActualizar);
        panel.add(botonEliminar);
        panel.add(botonLimpiar);
        return panel;
    }

    /**
     * Vuelve a leer las categorias y cuantos libros tiene cada una.
     */
    @Override
    public void recargar() {
        soltarSeleccion(tabla);
        DefaultTableModel modelo = modeloDe(tabla);
        modelo.setRowCount(0);
        try {
            List<Categoria> categorias = controlador.listarCategorias();
            for (Categoria categoria : categorias) {
                modelo.addRow(new Object[]{categoria.getId(), categoria.getNombre(),
                        controlador.contarLibrosDeCategoria(categoria.getId())});
            }
            contador.setText(categorias.size() + " categoria(s)");
        } catch (SQLException e) {
            contador.setText("No se pudieron leer las categorias");
            informarError("No se pudieron leer las categorias.", e);
        }
    }

    /**
     * Registra una categoria nueva.
     */
    private void agregar() {
        String nombre = leerNombre();
        if (nombre == null) {
            return;
        }
        Categoria categoria = new Categoria(0, nombre);
        try {
            controlador.crearCategoria(categoria);
        } catch (IllegalStateException e) {
            advertir(e.getMessage());
            return;
        } catch (SQLException e) {
            informarError("No se pudo guardar la categoria.", e);
            return;
        }
        informar("Categoria guardada con el id " + categoria.getId() + ".");
        limpiar();
        recargar();
    }

    /**
     * Cambia el nombre de la categoria seleccionada.
     */
    private void actualizar() {
        int fila = filaSeleccionada(tabla);
        if (fila < 0) {
            advertir("Selecciona primero una categoria en la tabla.");
            return;
        }
        String nombre = leerNombre();
        if (nombre == null) {
            return;
        }
        int id = (int) modeloDe(tabla).getValueAt(fila, COL_ID);
        try {
            if (controlador.actualizarCategoria(new Categoria(id, nombre))) {
                informar("Categoria actualizada.");
            } else {
                advertir("La categoria ya no existe en la base de datos.");
            }
        } catch (IllegalStateException e) {
            advertir(e.getMessage());
            return;
        } catch (SQLException e) {
            informarError("No se pudo actualizar la categoria.", e);
            return;
        }
        limpiar();
        recargar();
    }

    /**
     * Elimina la categoria seleccionada, pidiendo confirmacion.
     */
    private void eliminar() {
        int fila = filaSeleccionada(tabla);
        if (fila < 0) {
            advertir("Selecciona primero una categoria en la tabla.");
            return;
        }
        int id = (int) modeloDe(tabla).getValueAt(fila, COL_ID);
        String nombre = textoDe(modeloDe(tabla).getValueAt(fila, COL_NOMBRE));
        if (!confirmar("Vas a eliminar la categoria \"" + nombre + "\".\n\nContinuar?")) {
            return;
        }
        try {
            if (controlador.eliminarCategoria(id)) {
                informar("Categoria eliminada.");
            } else {
                advertir("La categoria ya no existe en la base de datos.");
            }
        } catch (IllegalStateException e) {
            advertir(e.getMessage());
            return;
        } catch (SQLException e) {
            informarError("No se pudo eliminar la categoria.", e);
            return;
        }
        limpiar();
        recargar();
    }

    /**
     * @return el nombre escrito, o null si esta vacio o es muy largo y ya se aviso
     */
    private String leerNombre() {
        String nombre = campoNombre.getText().trim();
        if (nombre.isEmpty()) {
            advertir("Debes indicar el nombre de la categoria.");
        } else if (nombre.length() > LARGO_NOMBRE) {
            advertir("El nombre no puede tener mas de " + LARGO_NOMBRE + " caracteres.");
        } else {
            return nombre;
        }
        campoNombre.requestFocusInWindow();
        return null;
    }

    /**
     * Deja el formulario vacio y sin seleccion.
     */
    private void limpiar() {
        soltarSeleccion(tabla);
        campoNombre.setText("");
        campoNombre.requestFocusInWindow();
    }
}
