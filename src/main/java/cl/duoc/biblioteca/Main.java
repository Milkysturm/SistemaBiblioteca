package cl.duoc.biblioteca;

import cl.duoc.biblioteca.conexion.DatabaseConnection;
import cl.duoc.biblioteca.modelo.Estudiante;
import cl.duoc.biblioteca.modelo.Persona;
import cl.duoc.biblioteca.modelo.Rol;
import cl.duoc.biblioteca.modelo.Usuario;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Main {
    public static void main(String[] args) {
        try {
            Connection con = DatabaseConnection.getInstance().getConnection();

            // Statement y ResultSet se cierran solos; la Connection NO (es compartida)
            try (Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery("SELECT titulo, stock FROM libros")) {
                while (rs.next()) {
                    System.out.println(rs.getString("titulo") + " | stock: " + rs.getInt("stock"));
                }
            }

            // Prueba del Singleton: ¿las dos llamadas devuelven el MISMO objeto?
            System.out.println("¿Misma instancia? "
                    + (DatabaseConnection.getInstance() == DatabaseConnection.getInstance()));

            // Prueba de polimorfismo: misma variable tipo Persona, distinto comportamiento
            Persona[] personas = {
                    new Usuario(1, "Antonia Pérez", "12345678-9", "antonia@correo.cl", "clave123", Rol.BIBLIOTECARIO),
                    new Estudiante(1, "Carlos Ruiz", "98765432-1", "3ro Medio A", "carlos@correo.cl")
            };
            for (Persona p : personas) {
                System.out.println(p.getResumen());
            }

        } catch (SQLException e) {
            System.out.println("Error de conexión: " + e.getMessage());
        }
    }
}
