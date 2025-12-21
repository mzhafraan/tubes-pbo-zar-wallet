package services;

import infrastructure.DatabaseHelper;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import models.*;

public class AuthService {

    // Login logic buat Customer
    public Customer loginCustomer(String username, String password) {
        String sql = "SELECT * FROM customer WHERE username = ? AND password = ?";

        try (Connection conn = DatabaseHelper.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                // 1. Bikin Object Customer dari data DB
                Customer cust = new Customer(
                        rs.getInt("customer_id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("full_name"),
                        rs.getString("phone_number"),
                        rs.getString("pin"));

                // 2. AMBIL WALLET MILIK DIA (PENTING!)
                // Kita harus 'attach' wallet ke customer ini biar saldonya kebaca
                Wallet userWallet = getWalletByCustomerId(cust.getId());
                cust.setWallet(userWallet);

                return cust;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // === TAMBAHAN FITUR REGISTER ===
    public boolean registerCustomer(String username, String password, String fullName, String phone, String pin) {
        Connection conn = null;
        String sqlCust = "INSERT INTO customer (username, password, full_name, phone_number, pin) VALUES (?, ?, ?, ?, ?)";
        String sqlWallet = "INSERT INTO wallet (customer_id, balance) VALUES (?, 0)"; // Saldo awal 0

        try {
            conn = DatabaseHelper.getConnection();
            conn.setAutoCommit(false); // START TRANSACTION

            // 1. Insert ke Tabel Customer
            int newCustomerId = -1;
            try (PreparedStatement stmt = conn.prepareStatement(sqlCust, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, username);
                stmt.setString(2, password);
                stmt.setString(3, fullName);
                stmt.setString(4, phone);
                stmt.setString(5, pin);
                int affectedRows = stmt.executeUpdate();

                if (affectedRows == 0)
                    throw new SQLException("Gagal membuat user.");

                // Ambil ID Customer yang baru aja dibuat (Auto Increment)
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        newCustomerId = generatedKeys.getInt(1);
                    } else {
                        throw new SQLException("Gagal mengambil ID Customer.");
                    }
                }
            }

            // 2. Otomatis Buatin Wallet
            try (PreparedStatement stmt = conn.prepareStatement(sqlWallet)) {
                stmt.setInt(1, newCustomerId);
                stmt.executeUpdate();
            }

            conn.commit(); // SAVE SEMUA
            System.out.println("Register Berhasil! ID Kamu: " + newCustomerId);
            return true;

        } catch (Exception e) {
            try {
                if (conn != null)
                    conn.rollback();
            } catch (SQLException ex) {
            } // BATALIN KALAU ERROR
            System.err.println("Register Gagal: " + e.getMessage()); // Biasanya error karena username/no hp udah ada
            return false;
        } finally {
            try {
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
            }
        }
    }

    // === FITUR ADMIN: LIHAT SEMUA USER ===
    public List<Customer> getAllCustomers() {
        List<Customer> list = new ArrayList<>();
        String sql = "SELECT * FROM customer";

        try (Connection conn = DatabaseHelper.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Customer cust = new Customer(
                        rs.getInt("customer_id"),
                        rs.getString("username"),
                        rs.getString("password"), // Password sebenernya jangan ditampilin, tapi buat object gpp
                        rs.getString("full_name"),
                        rs.getString("phone_number"),
                        rs.getString("pin"));

                // Attach Wallet biar admin bisa liat saldo user juga
                Wallet w = getWalletByCustomerId(cust.getId());
                cust.setWallet(w);

                list.add(cust);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // Helper: Ambil data wallet berdasarkan ID Customer
    private Wallet getWalletByCustomerId(int customerId) {
        String sql = "SELECT * FROM wallet WHERE customer_id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new Wallet(
                        rs.getInt("wallet_id"),
                        rs.getInt("customer_id"),
                        rs.getDouble("balance"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}