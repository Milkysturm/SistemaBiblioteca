package cl.duoc.biblioteca.vista;

import cl.duoc.biblioteca.controlador.ControladorBiblioteca;
import cl.duoc.biblioteca.modelo.Estudiante;
import cl.duoc.biblioteca.modelo.LibroMasPrestado;
import cl.duoc.biblioteca.modelo.Prestamo;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.sql.SQLException;
import java.util.List;

/**
 * Reportes para la gestion de la biblioteca: libros mas prestados, historial
 * por estudiante, libros actualmente en prestamo y devoluciones con atraso.
 *
 * @author Olga Rivas
 * @version 1.0
 */
public final class PanelReportes extends PanelCrud {

    /** Version de la clase, requerida por ser serializable. */
    private static final long serialVersionUID = 1L;

    /** Nombre del reporte de libros mas prestados. */
    private static final String MAS_PRESTADOS = "Libros mas prestados";

    /** Nombre del reporte de historial por estudiante. */
    private static final String HISTORIAL = "Historial por estudiante";

    /** Nombre del reporte de libros en prestamo. */
    private static final String EN_PRESTAMO = "Libros actualmente en prestamo";

    /** Nombre del reporte de devoluciones con atraso. */
    private static final String CON_ATRASO = "Devoluciones con atraso";

    /** Cantidad de libros que muestra el ranking de mas prestados. */
    private static final int TOP_LIBROS = 10;

    /** Controlador que habla con la base de datos. */
    private final transient ControladorBiblioteca controlador;

    /** Selector del reporte. */
    private final JComboBox<String> comboReporte =
            new JComboBox<>(new String[]{MAS_PRESTADOS, HISTORIAL, EN_PRESTAMO, CON_ATRASO});

    /** Selector del estudiante, solo para el historial. */
    private final JComboBox<Estudiante> comboEstudiante = new JComboBox<>();

    /** Etiqueta del selector de estudiante. */
    private final JLabel etiquetaEstudiante = new JLabel("Estudiante:");

    /** Resumen del reporte que se esta mostrando. */
    private final JLabel resumen = new JLabel(" ");

    /** true mientras se recarga el combo de estudiantes, para no generar el reporte varias veces. */
    private boolean recargando;

    /** Muestra una tabla distinta para cada reporte. */
    private final CardLayout tarjetas = new CardLayout();

    /** Contenedor de las tres tablas. */
    private final JPanel contenedorTablas = new JPanel(tarjetas);

    /** Tabla del ranking de libros mas prestados. */
    private final JTable tablaMasPrestados = crearTabla(
            new String[]{"Lugar", "Titulo", "Autor", "Veces prestado"},
            new Class<?>[]{Integer.class, String.class, String.class, Integer.class});

    /** Tabla del historial de un estudiante. */
    private final JTable tablaHistorial = crearTabla(
            new String[]{"ID", "Libro", "Prestado", "Vence", "Entregado", "Estado"},
            new Class<?>[]{Integer.class, String.class, String.class, String.class, String.class, String.class});

    /** Tabla de libros actualmente en prestamo. */
    private final JTable tablaEnPrestamo = crearTabla(
            new String[]{"ID", "Estudiante", "Libro", "Prestado", "Vence", "Estado"},
            new Class<?>[]{Integer.class, String.class, String.class, String.class, String.class, String.class});

    /** Tabla de devoluciones hechas despues del vencimiento. */
    private final JTable tablaConAtraso = crearTabla(
            new String[]{"ID", "Estudiante", "Libro", "Vence", "Entregado", "Dias de atraso"},
            new Class<?>[]{Integer.class, String.class, String.class, String.class, String.class, Long.class});

    /**
     * Construye el panel.
     *
     * @param controlador controlador del sistema
     */
    public PanelReportes(ControladorBiblioteca controlador) {
        this.controlador = controlador;
        setBorder(MARGEN);

        definirAnchos(tablaMasPrestados, 50, 300, 200, 100);
        definirAnchos(tablaHistorial, 40, 260, 90, 90, 90, 200);
        definirAnchos(tablaEnPrestamo, 40, 160, 240, 90, 90, 200);
        definirAnchos(tablaConAtraso, 40, 160, 260, 90, 90, 110);
        tablaHistorial.getColumnModel().getColumn(5).setCellRenderer(new RenderEstadoPrestamo());
        tablaEnPrestamo.getColumnModel().getColumn(5).setCellRenderer(new RenderEstadoPrestamo());

        contenedorTablas.add(new JScrollPane(tablaMasPrestados), MAS_PRESTADOS);
        contenedorTablas.add(new JScrollPane(tablaHistorial), HISTORIAL);
        contenedorTablas.add(new JScrollPane(tablaEnPrestamo), EN_PRESTAMO);
        contenedorTablas.add(new JScrollPane(tablaConAtraso), CON_ATRASO);

        JButton botonGenerar = new JButton("Generar reporte");
        botonGenerar.addActionListener(e -> generar());
        comboReporte.addActionListener(e -> generar());
        comboEstudiante.addActionListener(e -> {
            if (!recargando && HISTORIAL.equals(comboReporte.getSelectedItem())) {
                generar();
            }
        });

        JPanel selector = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        selector.add(new JLabel("Reporte:"));
        selector.add(comboReporte);
        selector.add(etiquetaEstudiante);
        selector.add(comboEstudiante);
        selector.add(botonGenerar);

        resumen.setFont(resumen.getFont().deriveFont(Font.BOLD));

        add(selector, BorderLayout.NORTH);
        add(contenedorTablas, BorderLayout.CENTER);
        add(resumen, BorderLayout.SOUTH);
        recargar();
    }

    /**
     * Recarga la lista de estudiantes y vuelve a generar el reporte elegido.
     */
    @Override
    public void recargar() {
        Estudiante elegido = (Estudiante) comboEstudiante.getSelectedItem();
        recargando = true;
        try {
            comboEstudiante.removeAllItems();
            for (Estudiante estudiante : controlador.listarEstudiantes()) {
                comboEstudiante.addItem(estudiante);
                if (elegido != null && elegido.getId() == estudiante.getId()) {
                    comboEstudiante.setSelectedItem(estudiante);
                }
            }
        } catch (SQLException e) {
            informarError("No se pudieron leer los estudiantes.", e);
        } finally {
            recargando = false;
        }
        generar();
    }

    /**
     * Genera el reporte elegido y muestra su tabla.
     */
    private void generar() {
        String reporte = (String) comboReporte.getSelectedItem();
        boolean esHistorial = HISTORIAL.equals(reporte);
        etiquetaEstudiante.setVisible(esHistorial);
        comboEstudiante.setVisible(esHistorial);
        tarjetas.show(contenedorTablas, reporte);

        try {
            if (MAS_PRESTADOS.equals(reporte)) {
                generarMasPrestados();
            } else if (esHistorial) {
                generarHistorial();
            } else if (EN_PRESTAMO.equals(reporte)) {
                generarEnPrestamo();
            } else {
                generarConAtraso();
            }
        } catch (SQLException e) {
            resumen.setText("No se pudo generar el reporte");
            informarError("No se pudo generar el reporte.", e);
        }
    }

    /**
     * Ranking de los libros con mas prestamos registrados.
     *
     * @throws SQLException si falla la consulta
     */
    private void generarMasPrestados() throws SQLException {
        DefaultTableModel modelo = modeloDe(tablaMasPrestados);
        modelo.setRowCount(0);
        List<LibroMasPrestado> ranking = controlador.librosMasPrestados(TOP_LIBROS);
        int lugar = 1;
        for (LibroMasPrestado libro : ranking) {
            modelo.addRow(new Object[]{lugar++, libro.getTitulo(), libro.getAutor(), libro.getVecesPrestado()});
        }
        resumen.setText(ranking.isEmpty()
                ? "Todavia no hay prestamos registrados."
                : "Los " + ranking.size() + " libros con mas prestamos registrados.");
    }

    /**
     * Historial completo de prestamos del estudiante elegido.
     *
     * @throws SQLException si falla la consulta
     */
    private void generarHistorial() throws SQLException {
        DefaultTableModel modelo = modeloDe(tablaHistorial);
        modelo.setRowCount(0);
        Estudiante estudiante = (Estudiante) comboEstudiante.getSelectedItem();
        if (estudiante == null) {
            resumen.setText("No hay estudiantes registrados.");
            return;
        }

        List<Prestamo> historial = controlador.historialDe(estudiante.getId());
        int activos = 0;
        int conAtraso = 0;
        for (Prestamo prestamo : historial) {
            modelo.addRow(new Object[]{prestamo.getId(), prestamo.getTituloLibro(),
                    fecha(prestamo.getFechaPrestamo()), fecha(prestamo.getFechaDevolucion()),
                    fecha(prestamo.getFechaEntrega()), prestamo.getEstado()});
            if (!prestamo.isDevuelto()) {
                activos++;
            }
            if (prestamo.getDiasAtraso() > 0) {
                conAtraso++;
            }
        }
        resumen.setText(estudiante.getResumen() + "  -  " + historial.size() + " prestamo(s), "
                + activos + " sin devolver, " + conAtraso + " con atraso.");
    }

    /**
     * Libros que estan prestados ahora, del vencimiento mas antiguo al mas reciente.
     *
     * @throws SQLException si falla la consulta
     */
    private void generarEnPrestamo() throws SQLException {
        DefaultTableModel modelo = modeloDe(tablaEnPrestamo);
        modelo.setRowCount(0);
        List<Prestamo> activos = controlador.listarPrestamosActivos();
        int atrasados = 0;
        for (Prestamo prestamo : activos) {
            modelo.addRow(new Object[]{prestamo.getId(), prestamo.getNombreEstudiante(),
                    prestamo.getTituloLibro(), fecha(prestamo.getFechaPrestamo()),
                    fecha(prestamo.getFechaDevolucion()), prestamo.getEstado()});
            if (prestamo.estaAtrasado()) {
                atrasados++;
            }
        }
        resumen.setText(activos.size() + " libro(s) en prestamo, " + atrasados + " atrasado(s).");
    }

    /**
     * Prestamos que se devolvieron despues de su vencimiento: la constancia de los atrasos.
     *
     * @throws SQLException si falla la consulta
     */
    private void generarConAtraso() throws SQLException {
        DefaultTableModel modelo = modeloDe(tablaConAtraso);
        modelo.setRowCount(0);
        List<Prestamo> atrasados = controlador.devolucionesConAtraso();
        long totalDias = 0;
        for (Prestamo prestamo : atrasados) {
            modelo.addRow(new Object[]{prestamo.getId(), prestamo.getNombreEstudiante(),
                    prestamo.getTituloLibro(), fecha(prestamo.getFechaDevolucion()),
                    fecha(prestamo.getFechaEntrega()), prestamo.getDiasAtraso()});
            totalDias += prestamo.getDiasAtraso();
        }
        resumen.setText(atrasados.isEmpty()
                ? "No hay devoluciones con atraso registradas."
                : atrasados.size() + " devolucion(es) con atraso, " + totalDias + " dia(s) de atraso en total.");
    }
}
