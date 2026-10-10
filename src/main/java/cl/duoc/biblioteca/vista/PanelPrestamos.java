package cl.duoc.biblioteca.vista;

import cl.duoc.biblioteca.controlador.ControladorBiblioteca;
import cl.duoc.biblioteca.modelo.Estudiante;
import cl.duoc.biblioteca.modelo.Libro;
import cl.duoc.biblioteca.modelo.Prestamo;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Prestamos y devoluciones.
 *
 * El registro se hace en un hilo en segundo plano (SwingWorker), para que la
 * ventana no se congele mientras se guarda el prestamo, se actualiza el stock y
 * se actualiza el historial del estudiante.
 *
 * El bibliotecario elige al estudiante y ve todos los prestamos. Un estudiante
 * usa el mismo panel con su ficha fija: solo pide y devuelve sus propios libros.
 *
 * @author Olga Rivas
 * @version 1.0
 */
public final class PanelPrestamos extends PanelCrud {

    /** Version de la clase, requerida por ser serializable. */
    private static final long serialVersionUID = 1L;

    /**
     * Pausa breve entre los pasos del hilo, solo para que en la demostracion se
     * vea el avance y que la ventana sigue respondiendo mientras el hilo trabaja.
     */
    private static final long PAUSA_DEMOSTRACION_MS = 700;

    /** Columna de la tabla con el id del prestamo. */
    private static final int COL_ID = 0;

    /** Columna de la tabla con el estado del prestamo. */
    private static final int COL_ESTADO = 6;

    /** Opcion del filtro que muestra solo los prestamos sin devolver. */
    private static final String SOLO_ACTIVOS = "Libros en prestamo";

    /** Opcion del filtro que muestra todos los prestamos. */
    private static final String TODOS = "Todos los prestamos";

    /** Controlador que habla con la base de datos. */
    private final transient ControladorBiblioteca controlador;

    /** Estudiante con sesion iniciada, o null si quien usa el panel es el bibliotecario. */
    private final transient Estudiante estudianteFijo;

    /** Selector del estudiante (solo bibliotecario). */
    private final JComboBox<Estudiante> comboEstudiante = new JComboBox<>();

    /** Selector del libro, solo con libros que tienen stock. */
    private final JComboBox<Libro> comboLibro = new JComboBox<>();

    /** Filtro de la tabla (solo bibliotecario). */
    private final JComboBox<String> comboFiltro = new JComboBox<>(new String[]{SOLO_ACTIVOS, TODOS});

    /** Boton para registrar el prestamo. */
    private final JButton botonPrestar = new JButton();

    /** Boton para registrar la devolucion del prestamo seleccionado. */
    private final JButton botonDevolver = new JButton();

    /** Barra que se mueve mientras el hilo trabaja. */
    private final JProgressBar barraProgreso = new JProgressBar();

    /** Texto con el paso que esta haciendo el hilo. */
    private final JLabel estadoHilo = new JLabel(" ");

    /** Tabla con los prestamos. */
    private final JTable tabla = crearTabla(
            new String[]{"ID", "Estudiante", "Libro", "Prestado", "Vence", "Entregado", "Estado"},
            new Class<?>[]{Integer.class, String.class, String.class, String.class,
                    String.class, String.class, String.class});

    /** Etiqueta con el total de prestamos mostrados. */
    private final JLabel contador = new JLabel();

    /**
     * Construye el panel.
     *
     * @param controlador    controlador del sistema
     * @param estudianteFijo ficha del estudiante con sesion iniciada, o null para el bibliotecario
     */
    public PanelPrestamos(ControladorBiblioteca controlador, Estudiante estudianteFijo) {
        this.controlador = controlador;
        this.estudianteFijo = estudianteFijo;
        setBorder(MARGEN);
        definirAnchos(tabla, 40, 150, 230, 85, 85, 85, 190);
        tabla.getColumnModel().getColumn(COL_ESTADO).setCellRenderer(new RenderEstadoPrestamo());

        botonPrestar.setText(estudianteFijo == null ? "Registrar prestamo" : "Pedir prestado");
        botonPrestar.addActionListener(e -> prestar());
        botonDevolver.setText(estudianteFijo == null ? "Registrar devolucion" : "Devolver libro");
        botonDevolver.addActionListener(e -> devolver());
        comboFiltro.addActionListener(e -> mostrarPrestamos());

        JPanel abajo = new JPanel(new BorderLayout(8, 0));
        abajo.add(contador, BorderLayout.WEST);
        JPanel progreso = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        progreso.add(estadoHilo);
        progreso.add(barraProgreso);
        barraProgreso.setVisible(false);
        abajo.add(progreso, BorderLayout.EAST);

        add(crearFormulario(), BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);
        add(abajo, BorderLayout.SOUTH);
        recargar();
    }

    /**
     * Crea el formulario de prestamo y la barra de devolucion.
     *
     * @return panel superior
     */
    private JPanel crearFormulario() {
        JPanel filaPrestamo = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        if (estudianteFijo == null) {
            filaPrestamo.add(new JLabel("Estudiante:"));
            filaPrestamo.add(comboEstudiante);
        }
        filaPrestamo.add(new JLabel("Libro:"));
        filaPrestamo.add(comboLibro);
        filaPrestamo.add(botonPrestar);

        JLabel vencimiento = new JLabel("Un prestamo dura " + Prestamo.DIAS_PRESTAMO
                + " dias: si se presta hoy, vence el "
                + Prestamo.calcularVencimiento(LocalDate.now()).format(FORMATO_FECHA) + ".");

        JPanel filaInfo = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filaInfo.add(vencimiento);

        JPanel nuevo = new JPanel();
        nuevo.setLayout(new BoxLayout(nuevo, BoxLayout.Y_AXIS));
        nuevo.setBorder(BorderFactory.createTitledBorder(
                estudianteFijo == null ? "Nuevo prestamo" : "Pedir un libro"));
        nuevo.add(filaPrestamo);
        nuevo.add(filaInfo);

        JPanel filaTabla = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        if (estudianteFijo == null) {
            filaTabla.add(new JLabel("Mostrar:"));
            filaTabla.add(comboFiltro);
        } else {
            filaTabla.add(new JLabel("Tu historial de prestamos:"));
        }
        filaTabla.add(botonDevolver);
        filaTabla.add(new JLabel("(selecciona un prestamo en la tabla)"));

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(nuevo);
        panel.add(filaTabla);
        return panel;
    }

    /**
     * Recarga los selectores y la tabla.
     */
    @Override
    public void recargar() {
        try {
            if (estudianteFijo == null) {
                Estudiante elegido = (Estudiante) comboEstudiante.getSelectedItem();
                comboEstudiante.removeAllItems();
                for (Estudiante estudiante : controlador.listarEstudiantes()) {
                    comboEstudiante.addItem(estudiante);
                    if (elegido != null && elegido.getId() == estudiante.getId()) {
                        comboEstudiante.setSelectedItem(estudiante);
                    }
                }
            }
            comboLibro.removeAllItems();
            for (Libro libro : controlador.listarLibrosDisponibles()) {
                comboLibro.addItem(libro);
            }
        } catch (SQLException e) {
            informarError("No se pudieron leer los estudiantes o los libros.", e);
        }
        mostrarPrestamos();
    }

    /**
     * Muestra en la tabla los prestamos segun quien usa el panel y el filtro elegido.
     */
    private void mostrarPrestamos() {
        soltarSeleccion(tabla);
        DefaultTableModel modelo = modeloDe(tabla);
        modelo.setRowCount(0);

        List<Prestamo> prestamos;
        try {
            if (estudianteFijo != null) {
                prestamos = controlador.historialDe(estudianteFijo.getId());
            } else if (SOLO_ACTIVOS.equals(comboFiltro.getSelectedItem())) {
                prestamos = controlador.listarPrestamosActivos();
            } else {
                prestamos = controlador.listarPrestamos();
            }
        } catch (SQLException e) {
            contador.setText("No se pudieron leer los prestamos");
            informarError("No se pudieron leer los prestamos.", e);
            return;
        }

        for (Prestamo prestamo : prestamos) {
            modelo.addRow(new Object[]{prestamo.getId(), prestamo.getNombreEstudiante(),
                    prestamo.getTituloLibro(), fecha(prestamo.getFechaPrestamo()),
                    fecha(prestamo.getFechaDevolucion()), fecha(prestamo.getFechaEntrega()),
                    prestamo.getEstado()});
        }
        contador.setText(prestamos.size() + " prestamo(s)");
    }

    /**
     * Registra el prestamo en un hilo en segundo plano.
     */
    private void prestar() {
        Estudiante estudiante = (estudianteFijo != null)
                ? estudianteFijo : (Estudiante) comboEstudiante.getSelectedItem();
        Libro libro = (Libro) comboLibro.getSelectedItem();
        if (estudiante == null || libro == null) {
            advertir("Elige un estudiante y un libro con stock disponible.");
            return;
        }

        trabajando(true);
        new SwingWorker<Prestamo, String>() {

            /** Corre en un hilo aparte: aqui va el trabajo con la base de datos. */
            @Override
            protected Prestamo doInBackground() throws Exception {
                publish("Verificando disponibilidad del libro...");
                pausa();
                // Registra el prestamo, descuenta el stock y deja el historial (metodo synchronized)
                Prestamo prestamo = controlador.registrarPrestamo(estudiante.getId(), libro.getId());
                publish("Stock e historial del estudiante actualizados.");
                pausa();
                return prestamo;
            }

            /** Corre en el hilo grafico: muestra cada paso que avisa el hilo. */
            @Override
            protected void process(List<String> pasos) {
                estadoHilo.setText(pasos.get(pasos.size() - 1));
            }

            /** Corre en el hilo grafico cuando el hilo termina. */
            @Override
            protected void done() {
                trabajando(false);
                Prestamo prestamo = resultado(this, "No se pudo registrar el prestamo.");
                if (prestamo != null) {
                    informar("Prestamo registrado: \"" + libro.getTitulo() + "\" para "
                            + estudiante.getNombre() + ".\nDebe devolverse a mas tardar el "
                            + fecha(prestamo.getFechaDevolucion()) + ".");
                }
                recargar();
            }
        }.execute();
    }

    /**
     * Registra la devolucion del prestamo seleccionado en un hilo en segundo plano.
     */
    private void devolver() {
        int fila = filaSeleccionada(tabla);
        if (fila < 0) {
            advertir("Selecciona primero un prestamo en la tabla.");
            return;
        }
        int idPrestamo = (int) modeloDe(tabla).getValueAt(fila, COL_ID);

        trabajando(true);
        new SwingWorker<Prestamo, String>() {

            @Override
            protected Prestamo doInBackground() throws Exception {
                publish("Registrando la devolucion y devolviendo el ejemplar al stock...");
                pausa();
                return controlador.registrarDevolucion(idPrestamo);
            }

            @Override
            protected void process(List<String> pasos) {
                estadoHilo.setText(pasos.get(pasos.size() - 1));
            }

            @Override
            protected void done() {
                trabajando(false);
                Prestamo prestamo = resultado(this, "No se pudo registrar la devolucion.");
                if (prestamo != null) {
                    long dias = prestamo.getDiasAtraso();
                    if (dias > 0) {
                        JOptionPane.showMessageDialog(PanelPrestamos.this,
                                "Libro devuelto con " + dias + " dia(s) de atraso.\n"
                                + "Vencia el " + fecha(prestamo.getFechaDevolucion())
                                + ". El atraso quedo registrado en el historial.",
                                "Devolucion con atraso", JOptionPane.WARNING_MESSAGE);
                    } else {
                        informar("Libro devuelto a tiempo. Gracias.");
                    }
                }
                recargar();
            }
        }.execute();
    }

    /**
     * Obtiene el resultado del hilo y muestra el problema si lo hubo.
     *
     * @param hilo           hilo que ya termino
     * @param queSeIntentaba descripcion de la operacion, para el mensaje de error
     * @return el prestamo, o null si hubo un problema y ya se aviso
     */
    private Prestamo resultado(SwingWorker<Prestamo, String> hilo, String queSeIntentaba) {
        try {
            return hilo.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } catch (ExecutionException e) {
            Throwable causa = e.getCause();
            if (causa instanceof IllegalStateException) {
                advertir(causa.getMessage());
            } else if (causa instanceof SQLException) {
                informarError(queSeIntentaba, (SQLException) causa);
            } else {
                advertir(queSeIntentaba + "\n" + causa);
            }
            return null;
        }
    }

    /**
     * Muestra u oculta el avance del hilo y bloquea los botones mientras trabaja,
     * para que no se registre dos veces la misma operacion.
     *
     * @param ocupado true mientras el hilo trabaja
     */
    private void trabajando(boolean ocupado) {
        botonPrestar.setEnabled(!ocupado);
        botonDevolver.setEnabled(!ocupado);
        barraProgreso.setIndeterminate(ocupado);
        barraProgreso.setVisible(ocupado);
        if (!ocupado) {
            estadoHilo.setText(" ");
        }
    }

    /**
     * Pausa del hilo en segundo plano (nunca del hilo grafico).
     *
     * @throws InterruptedException si el hilo se interrumpe
     */
    private static void pausa() throws InterruptedException {
        Thread.sleep(PAUSA_DEMOSTRACION_MS);
    }
}
