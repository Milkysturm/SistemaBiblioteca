package cl.duoc.biblioteca.vista;

import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Base comun de los paneles de gestion (libros, estudiantes, etc.).
 *
 * Reune lo que todos hacen igual: la tabla de solo lectura, los mensajes al
 * usuario y la seleccion de filas. Asi cada panel solo tiene su propio codigo.
 *
 * @author Olga Rivas
 * @version 1.0
 */
public abstract class PanelCrud extends JPanel {

    /** Version de la clase, requerida por ser serializable. */
    private static final long serialVersionUID = 1L;

    /** Formato de fecha que se muestra al usuario. */
    protected static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    /** Margen que usan todos los paneles. */
    protected static final Border MARGEN = BorderFactory.createEmptyBorder(10, 12, 10, 12);

    /**
     * Crea el panel con la distribucion comun: formulario arriba, tabla al centro
     * y contador abajo.
     */
    protected PanelCrud() {
        super(new BorderLayout(0, 8));
    }

    /**
     * Vuelve a leer los datos desde la base y actualiza lo que muestra.
     * La ventana principal la llama al cambiar de pestana.
     */
    public abstract void recargar();

    /**
     * Crea una tabla que no se puede editar a mano: los datos se cambian con el formulario.
     *
     * @param columnas titulos de las columnas
     * @param clases   tipo de dato de cada columna, en el mismo orden
     * @return tabla configurada, vacia
     */
    protected final JTable crearTabla(String[] columnas, Class<?>[] clases) {
        DefaultTableModel modelo = new DefaultTableModel(columnas, 0) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }

            /**
             * Declara el tipo de cada columna, para que los numeros se ordenen como numeros.
             */
            @Override
            public Class<?> getColumnClass(int columna) {
                return (columna < clases.length) ? clases[columna] : String.class;
            }
        };

        JTable tabla = new JTable(modelo);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.getTableHeader().setReorderingAllowed(false);
        tabla.setAutoCreateRowSorter(true);
        tabla.setFillsViewportHeight(true);
        return tabla;
    }

    /**
     * Define el ancho preferido de cada columna, para que el ID no ocupe lo mismo que el titulo.
     *
     * @param tabla  tabla del panel
     * @param anchos ancho en pixeles de cada columna, en orden
     */
    protected final void definirAnchos(JTable tabla, int... anchos) {
        for (int i = 0; i < anchos.length && i < tabla.getColumnCount(); i++) {
            tabla.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        }
    }

    /**
     * @param tabla tabla del panel
     * @return el modelo de esa tabla
     */
    protected final DefaultTableModel modeloDe(JTable tabla) {
        return (DefaultTableModel) tabla.getModel();
    }

    /**
     * Devuelve la fila seleccionada en coordenadas del modelo.
     *
     * Hace falta convertirla porque con la tabla ordenada la fila que se ve no es
     * la misma que la del modelo.
     *
     * @param tabla tabla del panel
     * @return fila del modelo, o -1 si no hay nada seleccionado
     */
    protected final int filaSeleccionada(JTable tabla) {
        int fila = tabla.getSelectedRow();
        if (fila < 0 || fila >= tabla.getRowCount()) {
            return -1;
        }
        return tabla.convertRowIndexToModel(fila);
    }

    /**
     * Quita la seleccion de la tabla antes de vaciarla o limpiar el formulario.
     *
     * @param tabla tabla del panel
     */
    protected final void soltarSeleccion(JTable tabla) {
        tabla.clearSelection();
        tabla.getSelectionModel().setAnchorSelectionIndex(-1);
        tabla.getSelectionModel().setLeadSelectionIndex(-1);
    }

    /**
     * @param dia fecha a mostrar, puede ser null
     * @return la fecha como dd-MM-yyyy, o "" si es null
     */
    protected static String fecha(LocalDate dia) {
        return (dia == null) ? "" : dia.format(FORMATO_FECHA);
    }

    /**
     * @param valor contenido de una celda
     * @return el texto, o "" si la celda es nula
     */
    protected final String textoDe(Object valor) {
        return (valor == null) ? "" : valor.toString();
    }

    /**
     * Muestra un aviso de operacion realizada.
     *
     * @param texto mensaje a mostrar
     */
    protected final void informar(String texto) {
        JOptionPane.showMessageDialog(this, texto, "Listo", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Muestra un aviso de validacion: falta un dato, esta mal escrito o no se permite.
     *
     * @param texto mensaje a mostrar
     */
    protected final void advertir(String texto) {
        JOptionPane.showMessageDialog(this, texto, "Revisa los datos", JOptionPane.WARNING_MESSAGE);
    }

    /**
     * Muestra un error de base de datos con el detalle que devolvio MySQL.
     *
     * @param queSeIntentaba descripcion de la operacion que fallo
     * @param error          excepcion recibida
     */
    protected final void informarError(String queSeIntentaba, SQLException error) {
        JOptionPane.showMessageDialog(this,
                queSeIntentaba + "\n\nDetalle: " + error.getMessage(),
                "Error de base de datos", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Pide confirmacion antes de una accion que no se puede deshacer.
     *
     * @param texto pregunta a mostrar
     * @return true si el usuario confirmo
     */
    protected final boolean confirmar(String texto) {
        return JOptionPane.showConfirmDialog(this, texto, "Confirmar",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION;
    }
}
