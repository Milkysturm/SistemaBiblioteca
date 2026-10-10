package cl.duoc.biblioteca.vista;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.Color;
import java.awt.Component;

/**
 * Pinta la columna Estado de los prestamos: en rojo los atrasados y en naranjo
 * los devueltos con atraso.
 *
 * @author Olga Rivas
 * @version 1.0
 */
final class RenderEstadoPrestamo extends DefaultTableCellRenderer {

    /** Version de la clase, requerida por ser serializable. */
    private static final long serialVersionUID = 1L;

    /** Color de los prestamos atrasados. */
    private static final Color ROJO = new Color(190, 30, 30);

    /** Color de los devueltos con atraso. */
    private static final Color NARANJO = new Color(200, 110, 0);

    @Override
    public Component getTableCellRendererComponent(JTable tabla, Object valor, boolean seleccionada,
                                                   boolean foco, int fila, int columna) {
        Component celda = super.getTableCellRendererComponent(tabla, valor, seleccionada, foco, fila, columna);
        if (seleccionada) {
            return celda;
        }
        String texto = String.valueOf(valor);
        if (texto.startsWith("Atrasado")) {
            celda.setForeground(ROJO);
        } else if (texto.contains("atraso")) {
            celda.setForeground(NARANJO);
        } else {
            celda.setForeground(tabla.getForeground());
        }
        return celda;
    }
}
