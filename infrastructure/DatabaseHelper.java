package infrastructure;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseHelper {
    // Config Database
    private static final String URL = "jdbc:mysql://localhost:3306/ewallet_db";
    private static final String USER = "root";
    private static final String PASS = ""; // Kosongin kalau XAMPP default, atau isi password lo

    // Method buat ambil koneksi
    public static Connection getConnection() {
        Connection conn = null;
        try {
            // Load Driver (Opsional di Java baru, tapi bagus buat legacy)
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Buka Jembatan
            conn = DriverManager.getConnection(URL, USER, PASS);
            // System.out.println("Koneksi ke Database Berhasil! 🔥"); // Un-comment buat
            // testing

        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("Gagal Konek Database: " + e.getMessage());
            throw new RuntimeException("Gagal menghubungkan ke database: " + e.getMessage(), e);
        }
        return conn;
    }
}