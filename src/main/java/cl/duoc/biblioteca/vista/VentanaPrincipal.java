package cl.duoc.biblioteca.vista;

import cl.duoc.biblioteca.controlador.ControladorBiblioteca;
import cl.duoc.biblioteca.modelo.Usuario;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Font;

/**
 * Ventana principal del sistema.
 *
 * Muestra una pestana por modulo, segun el rol: el bibliotecario ve la gestion
 * completa y el estudiante solo la consulta del catalogo.
 *
 * @author Olga Rivas
 * @version 1.0
 */
public final class VentanaPrincipal extends JFrame {

    /** Version de la clase, requerida por ser serializable. */
    private static final long serialVersionUID = 1L;

    /** Controlador compartido por todos los paneles. */
    private final transient ControladorBiblioteca controlador;

    /** Pestanas de navegacion entre modulos. */
    private final JTabbedPane pestanas = new JTabbedPane();

    /**
     * Construye y muestra la ventana para el usuario que inicio sesion.
     *
     * @param usuario     usuario con sesion iniciada
     * @param controlador controlador del sistema
     */
    public VentanaPrincipal(Usuario usuario, ControladorBiblioteca controlador) {
        this.controlador = controlador;

        if (usuario.esBibliotecario()) {
            pestanas.addTab("Libros", new PanelLibros(controlador, true));
            pestanas.addTab("Estudiantes", new PanelEstudiantes(controlador));
        } else {
            pestanas.addTab("Catalogo", new PanelLibros(controlador, false));
        }

        // Al cambiar de pestana se recarga la que queda a la vista, por si los
        // datos cambiaron mientras se trabajaba en otra.
        pestanas.addChangeListener(e -> {
            if (pestanas.getSelectedComponent() instanceof PanelCrud) {
                ((PanelCrud) pestanas.getSelectedComponent()).recargar();
            }
        });

        JLabel sesion = new JLabel("Sesion iniciada: " + usuario.getNombre() + "  -  " + usuario.getTipo());
        sesion.setFont(sesion.getFont().deriveFont(Font.BOLD));
        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.setBorder(BorderFactory.createEmptyBorder(8, 12, 4, 12));
        encabezado.add(sesion, BorderLayout.WEST);

        JButton botonCerrarSesion = new JButton("Cerrar sesion");
        botonCerrarSesion.addActionListener(e -> cerrarSesion());
        encabezado.add(botonCerrarSesion, BorderLayout.EAST);

        setJMenuBar(crearMenu());
        add(encabezado, BorderLayout.NORTH);
        add(pestanas, BorderLayout.CENTER);

        setTitle("Biblioteca Escolar - Sistema de Gestion");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(1000, 620);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    /**
     * Cierra esta ventana y vuelve al inicio de sesion, sin cerrar la aplicacion.
     */
    private void cerrarSesion() {
        dispose();
        new VentanaLogin(controlador);
    }

    /**
     * Crea el menu Sistema con las opciones de cerrar sesion y salir.
     *
     * @return barra de menu
     */
    private JMenuBar crearMenu() {
        JMenuItem cerrarSesion = new JMenuItem("Cerrar sesion");
        cerrarSesion.addActionListener(e -> cerrarSesion());

        JMenuItem salir = new JMenuItem("Salir");
        salir.addActionListener(e -> {
            dispose();
            System.exit(0);
        });

        JMenu menu = new JMenu("Sistema");
        menu.add(cerrarSesion);
        menu.addSeparator();
        menu.add(salir);

        JMenuBar barra = new JMenuBar();
        barra.add(menu);
        return barra;
    }
}
