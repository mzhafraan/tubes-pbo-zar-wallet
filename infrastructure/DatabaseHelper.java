package infrastructure;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseHelper {

    private static final String URL = "jdbc:mysql://localhost:3306/ewallet_db";
    private static final String USER = "root";
    private static final String PASS = "";

    public static Connection getConnection() {
        Connection conn = null;
        try {

            Class.forName("com.mysql.cj.jdbc.Driver");

            conn = DriverManager.getConnection(URL, USER, PASS);

        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("Gagal Konek Database: " + e.getMessage());
            throw new RuntimeException("Gagal menghubungkan ke database: " + e.getMessage(), e);
        }
        return conn;
    }
}
