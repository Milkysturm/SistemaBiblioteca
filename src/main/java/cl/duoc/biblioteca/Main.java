package cl.duoc.biblioteca;

import cl.duoc.biblioteca.conexion.DatabaseConnection;

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

        } catch (SQLException e) {
            System.out.println("Error de conexión: " + e.getMessage());
        }
    }
}
