package services;

import infrastructure.DatabaseHelper;
import models.*;
import java.sql.*;

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
                    rs.getString("pin")
                );
                
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
                    rs.getDouble("balance")
                );
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }
}