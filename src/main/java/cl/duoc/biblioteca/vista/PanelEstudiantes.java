package cl.duoc.biblioteca.vista;

import cl.duoc.biblioteca.controlador.ControladorBiblioteca;
import cl.duoc.biblioteca.modelo.Estudiante;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

/**
 * Gestion de estudiantes: registrar, modificar, eliminar y visualizar.
 *
 * Al registrar un estudiante se le puede dar una contrasena para crear su cuenta
 * de usuario, y asi pueda iniciar sesion con su correo.
 *
 * @author Olga Rivas
 * @version 1.0
 */
public final class PanelEstudiantes extends PanelCrud {

    /** Version de la clase, requerida por ser serializable. */
    private static final long serialVersionUID = 1L;

    /** Largo maximo del nombre y el correo, el mismo de las columnas en la base. */
    private static final int LARGO_TEXTO = 100;

    /** Largo maximo del RUT, el mismo de la columna en la base. */
    private static final int LARGO_RUT = 12;

    /** Largo maximo del curso, el mismo de la columna en la base. */
    private static final int LARGO_CURSO = 20;

    /** Formato de RUT aceptado: 7 u 8 digitos, guion y digito verificador. */
    private static final String FORMATO_RUT = "\\d{7,8}-[\\dkK]";

    /** Columna de la tabla con el id del estudiante. */
    private static final int COL_ID = 0;

    /** Columna de la tabla con el RUT. */
    private static final int COL_RUT = 2;

    /** Controlador que habla con la base de datos. */
    private final transient ControladorBiblioteca controlador;

    /** Campo del nombre. */
    private final JTextField campoNombre = new JTextField(18);

    /** Campo del RUT. */
    private final JTextField campoRut = new JTextField(10);

    /** Campo del curso. */
    private final JTextField campoCurso = new JTextField(10);

    /** Campo del correo. */
    private final JTextField campoCorreo = new JTextField(16);

    /** Contrasena de la cuenta de usuario (opcional). */
    private final JPasswordField campoClave = new JPasswordField(10);

    /** Tabla con los estudiantes. */
    private final JTable tabla = crearTabla(
            new String[]{"ID", "Nombre", "RUT", "Curso", "Correo", "Cuenta"},
            new Class<?>[]{Integer.class, String.class, String.class, String.class,
                    String.class, String.class});

    /** Etiqueta con el total de estudiantes. */
    private final JLabel contador = new JLabel();

    /**
     * Construye el panel.
     *
     * @param controlador controlador del sistema
     */
    public PanelEstudiantes(ControladorBiblioteca controlador) {
        this.controlador = controlador;
        setBorder(MARGEN);
        definirAnchos(tabla, 40, 200, 100, 110, 200, 60);

        add(crearFormulario(), BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);
        add(contador, BorderLayout.SOUTH);

        // Al elegir una fila, sus datos pasan al formulario para poder editarla.
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                cargarSeleccion();
            }
        });
        recargar();
    }

    /**
     * Crea el formulario con los campos y los botones.
     *
     * @return panel del formulario
     */
    private JPanel crearFormulario() {
        JPanel fila1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        fila1.add(new JLabel("Nombre:"));
        fila1.add(campoNombre);
        fila1.add(new JLabel("RUT:"));
        fila1.add(campoRut);
        fila1.add(new JLabel("Curso:"));
        fila1.add(campoCurso);

        campoClave.setToolTipText("Opcional. Al actualizar, dejala vacia para no cambiarla.");
        JPanel fila2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        fila2.add(new JLabel("Correo:"));
        fila2.add(campoCorreo);
        fila2.add(new JLabel("Contrasena de acceso:"));
        fila2.add(campoClave);
        fila2.add(new JLabel("(opcional, crea su cuenta de usuario)"));

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
        panel.setBorder(BorderFactory.createTitledBorder("Datos del estudiante"));
        panel.add(fila1);
        panel.add(fila2);
        panel.add(botones);
        return panel;
    }

    /**
     * Vuelve a leer los estudiantes desde la base de datos.
     */
    @Override
    public void recargar() {
        soltarSeleccion(tabla);
        DefaultTableModel modelo = modeloDe(tabla);
        modelo.setRowCount(0);

        try {
            List<Estudiante> estudiantes = controlador.listarEstudiantes();
            Set<String> conCuenta = controlador.rutsConCuenta();
            for (Estudiante estudiante : estudiantes) {
                modelo.addRow(new Object[]{estudiante.getId(), estudiante.getNombre(),
                        estudiante.getRut(), estudiante.getCurso(), estudiante.getCorreo(),
                        conCuenta.contains(estudiante.getRut()) ? "Si" : "No"});
            }
            contador.setText(estudiantes.size() + " estudiante(s) registrado(s)");
        } catch (SQLException e) {
            contador.setText("No se pudieron leer los estudiantes");
            informarError("No se pudieron leer los estudiantes.", e);
        }
    }

    /**
     * Registra un estudiante nuevo con los datos del formulario.
     */
    private void agregar() {
        Estudiante estudiante = leerFormulario(0);
        if (estudiante == null) {
            return;
        }
        String clave = new String(campoClave.getPassword());
        try {
            controlador.crearEstudiante(estudiante, clave);
        } catch (IllegalStateException e) {
            advertir(e.getMessage());
            return;
        } catch (SQLException e) {
            informarError("No se pudo guardar el estudiante.", e);
            return;
        }
        informar("Estudiante guardado con el id " + estudiante.getId()
                + (clave.isBlank() ? "." : ".\nYa puede iniciar sesion con su correo."));
        limpiar();
        recargar();
    }

    /**
     * Guarda los cambios del estudiante seleccionado.
     */
    private void actualizar() {
        int fila = filaSeleccionada(tabla);
        if (fila < 0) {
            advertir("Selecciona primero un estudiante en la tabla.");
            return;
        }
        int id = (int) modeloDe(tabla).getValueAt(fila, COL_ID);
        String rutAnterior = textoDe(modeloDe(tabla).getValueAt(fila, COL_RUT));
        Estudiante estudiante = leerFormulario(id);
        if (estudiante == null) {
            return;
        }
        try {
            if (controlador.actualizarEstudiante(estudiante, rutAnterior,
                    new String(campoClave.getPassword()))) {
                informar("Estudiante " + id + " actualizado.");
            } else {
                advertir("El estudiante ya no existe en la base de datos.");
            }
        } catch (IllegalStateException e) {
            advertir(e.getMessage());
            return;
        } catch (SQLException e) {
            informarError("No se pudo actualizar el estudiante.", e);
            return;
        }
        limpiar();
        recargar();
    }

    /**
     * Elimina el estudiante seleccionado y su cuenta, pidiendo confirmacion.
     */
    private void eliminar() {
        int fila = filaSeleccionada(tabla);
        if (fila < 0) {
            advertir("Selecciona primero un estudiante en la tabla.");
            return;
        }
        int id = (int) modeloDe(tabla).getValueAt(fila, COL_ID);
        String nombre = textoDe(modeloDe(tabla).getValueAt(fila, 1));
        if (!confirmar("Vas a eliminar a " + nombre + " y su cuenta de usuario.\n\nContinuar?")) {
            return;
        }
        try {
            if (controlador.eliminarEstudiante(id)) {
                informar("Estudiante eliminado.");
            } else {
                advertir("El estudiante ya no existe en la base de datos.");
            }
        } catch (IllegalStateException e) {
            advertir(e.getMessage());
            return;
        } catch (SQLException e) {
            informarError("No se pudo eliminar el estudiante.", e);
            return;
        }
        limpiar();
        recargar();
    }

    /**
     * Lee y valida el formulario antes de tocar la base.
     *
     * @param id identificador del estudiante, o 0 si es nuevo
     * @return el estudiante armado, o null si falta un dato y ya se aviso
     */
    private Estudiante leerFormulario(int id) {
        String nombre = campoNombre.getText().trim();
        String rut = campoRut.getText().trim();
        String curso = campoCurso.getText().trim();
        String correo = campoCorreo.getText().trim();

        if (!textoValido(nombre, "el nombre", LARGO_TEXTO, campoNombre)
                || !textoValido(rut, "el RUT", LARGO_RUT, campoRut)
                || !textoValido(curso, "el curso", LARGO_CURSO, campoCurso)
                || !textoValido(correo, "el correo", LARGO_TEXTO, campoCorreo)) {
            return null;
        }
        if (!rut.matches(FORMATO_RUT)) {
            advertir("El RUT debe tener el formato 12345678-9 (sin puntos, con guion).");
            campoRut.requestFocusInWindow();
            return null;
        }
        if (!correo.contains("@")) {
            advertir("El correo no es valido.");
            campoCorreo.requestFocusInWindow();
            return null;
        }
        return new Estudiante(id, nombre, rut, curso, correo);
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
     * Copia al formulario los datos de la fila seleccionada.
     */
    private void cargarSeleccion() {
        int fila = filaSeleccionada(tabla);
        if (fila < 0) {
            return;
        }
        DefaultTableModel modelo = modeloDe(tabla);
        campoNombre.setText(textoDe(modelo.getValueAt(fila, 1)));
        campoRut.setText(textoDe(modelo.getValueAt(fila, COL_RUT)));
        campoCurso.setText(textoDe(modelo.getValueAt(fila, 3)));
        campoCorreo.setText(textoDe(modelo.getValueAt(fila, 4)));
        campoClave.setText("");
    }

    /**
     * Deja el formulario vacio y sin seleccion.
     */
    private void limpiar() {
        soltarSeleccion(tabla);
        campoNombre.setText("");
        campoRut.setText("");
        campoCurso.setText("");
        campoCorreo.setText("");
        campoClave.setText("");
        campoNombre.requestFocusInWindow();
    }
}
