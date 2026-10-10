package cl.duoc.biblioteca.vista;

import cl.duoc.biblioteca.controlador.ControladorBiblioteca;
import cl.duoc.biblioteca.modelo.Usuario;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.SQLException;

/**
 * Ventana de autenticacion: pide correo y contrasena y abre la ventana
 * principal con las funciones que corresponden al rol del usuario.
 *
 * @author Olga Rivas
 * @version 1.0
 */
public final class VentanaLogin extends JFrame {

    /** Version de la clase, requerida por ser serializable. */
    private static final long serialVersionUID = 1L;

    /** Controlador que valida el usuario en la base de datos. */
    private final transient ControladorBiblioteca controlador;

    /** Campo del correo. */
    private final JTextField campoCorreo = new JTextField(20);

    /** Campo de la contrasena. */
    private final JPasswordField campoClave = new JPasswordField(20);

    /**
     * Construye y muestra la ventana.
     *
     * @param controlador controlador del sistema
     */
    public VentanaLogin(ControladorBiblioteca controlador) {
        this.controlador = controlador;

        JLabel titulo = new JLabel("Biblioteca Escolar", SwingConstants.CENTER);
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 20f));
        JLabel subtitulo = new JLabel("Inicia sesion para continuar", SwingConstants.CENTER);

        JPanel encabezado = new JPanel(new BorderLayout(0, 4));
        encabezado.add(titulo, BorderLayout.NORTH);
        encabezado.add(subtitulo, BorderLayout.SOUTH);

        JButton botonIngresar = new JButton("Ingresar");
        botonIngresar.addActionListener(e -> ingresar());

        JPanel formulario = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.anchor = GridBagConstraints.WEST;

        c.gridx = 0;
        c.gridy = 0;
        formulario.add(new JLabel("Correo:"), c);
        c.gridx = 1;
        formulario.add(campoCorreo, c);

        c.gridx = 0;
        c.gridy = 1;
        formulario.add(new JLabel("Contrasena:"), c);
        c.gridx = 1;
        formulario.add(campoClave, c);

        c.gridx = 1;
        c.gridy = 2;
        c.anchor = GridBagConstraints.EAST;
        formulario.add(botonIngresar, c);

        JPanel contenido = new JPanel(new BorderLayout(0, 12));
        contenido.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        contenido.add(encabezado, BorderLayout.NORTH);
        contenido.add(formulario, BorderLayout.CENTER);

        setTitle("Biblioteca Escolar - Iniciar sesion");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setContentPane(contenido);
        getRootPane().setDefaultButton(botonIngresar);   // Enter = Ingresar
        pack();
        setResizable(false);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    /**
     * Valida los datos en la base y, si son correctos, abre la ventana principal.
     */
    private void ingresar() {
        String correo = campoCorreo.getText().trim();
        String clave = new String(campoClave.getPassword());
        if (correo.isEmpty() || clave.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Escribe tu correo y tu contrasena.",
                    "Faltan datos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Usuario usuario;
        try {
            usuario = controlador.iniciarSesion(correo, clave);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "No se pudo validar el usuario.\n\nDetalle: "
                    + e.getMessage(), "Error de base de datos", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (usuario == null) {
            JOptionPane.showMessageDialog(this, "Correo o contrasena incorrectos.",
                    "No se pudo iniciar sesion", JOptionPane.WARNING_MESSAGE);
            campoClave.setText("");
            campoClave.requestFocusInWindow();
            return;
        }

        dispose();
        new VentanaPrincipal(usuario, controlador);
    }
}
