package services;

import infrastructure.DatabaseHelper;
import models.Customer;
import models.Product;
import java.sql.*;

public class WalletService {

    // FITUR 1: TOP UP
    public boolean topUp(Customer cust, double amount) {
        String sqlUpdate = "UPDATE wallet SET balance = balance + ? WHERE customer_id = ?";
        String sqlLog = "INSERT INTO transaction (customer_id, transaction_type, amount) VALUES (?, 'TOPUP', ?)";

        try (Connection conn = DatabaseHelper.getConnection()) {
            conn.setAutoCommit(false); // START TRANSACTION

            // 1. Update Saldo
            try (PreparedStatement stmt = conn.prepareStatement(sqlUpdate)) {
                stmt.setDouble(1, amount);
                stmt.setInt(2, cust.getId());
                stmt.executeUpdate();
            }

            // 2. Catat History
            try (PreparedStatement stmt = conn.prepareStatement(sqlLog)) {
                stmt.setInt(1, cust.getId());
                stmt.setDouble(2, amount);
                stmt.executeUpdate();
            }

            conn.commit(); // SAVE
            // Update saldo di object Java biar realtime
            cust.getWallet().topUp(amount);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // FITUR 2: TRANSFER (Logic Paling Rumit)
    public boolean transfer(Customer sender, int targetCustId, double amount) {
        Connection conn = null;
        try {
            conn = DatabaseHelper.getConnection();
            conn.setAutoCommit(false); // MATIKAN AUTO SAVE

            // A. Validasi Saldo
            if (sender.getWallet().checkBalance() < amount) {
                System.out.println("Saldo tidak cukup!");
                return false;
            }

            // B. Kurangi Saldo Pengirim
            String sqlDebit = "UPDATE wallet SET balance = balance - ? WHERE customer_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sqlDebit)) {
                stmt.setDouble(1, amount);
                stmt.setInt(2, sender.getId());
                stmt.executeUpdate();
            }

            // C. Tambah Saldo Penerima
            String sqlCredit = "UPDATE wallet SET balance = balance + ? WHERE customer_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sqlCredit)) {
                stmt.setDouble(1, amount);
                stmt.setInt(2, targetCustId);
                int row = stmt.executeUpdate();
                if (row == 0)
                    throw new SQLException("ID Penerima Salah!"); // Batalin kalau user gak ada
            }

            // D. Catat History Transfer
            String sqlLog = "INSERT INTO transaction (customer_id, target_customer_id, transaction_type, amount) VALUES (?, ?, 'TRANSFER', ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sqlLog)) {
                stmt.setInt(1, sender.getId());
                stmt.setInt(2, targetCustId);
                stmt.setDouble(3, amount);
                stmt.executeUpdate();
            }

            conn.commit(); // SEMUA SUKSES -> SAVE PERMANEN

            // Update saldo di object Java sender biar sinkron
            sender.getWallet().processPayment(amount);
            return true;

        } catch (Exception e) {
            try {
                if (conn != null)
                    conn.rollback();
            } catch (Exception ex) {
            } // BALIKIN SALDO KALAU ERROR
            System.err.println("Transfer Gagal: " + e.getMessage());
            return false;
        }
    }

    // FITUR 3: BELI PRODUK (Logic Pembayaran)
    public boolean buyProduct(Customer cust, Product product) {
        Connection conn = null;
        try {
            conn = DatabaseHelper.getConnection();
            conn.setAutoCommit(false); // START TRANSACTION

            // 1. Validasi Saldo
            if (cust.getWallet().checkBalance() < product.getPrice()) {
                System.out.println("Saldo tidak cukup!");
                return false;
            }

            // 2. Validasi Stok (Ambil real-time dari DB biar aman)
            String checkStockSql = "SELECT stock FROM product WHERE product_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(checkStockSql)) {
                stmt.setInt(1, product.getProductId());
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    int currentStock = rs.getInt("stock");
                    if (currentStock <= 0) {
                        System.out.println("Stok habis!");
                        return false;
                    }
                } else {
                    return false; // Produk ga ada
                }
            }

            // 3. Kurangi Saldo User
            String sqlDebit = "UPDATE wallet SET balance = balance - ? WHERE customer_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sqlDebit)) {
                stmt.setDouble(1, product.getPrice());
                stmt.setInt(2, cust.getId());
                stmt.executeUpdate();
            }

            // 4. Kurangi Stok Produk
            String sqlStock = "UPDATE product SET stock = stock - 1 WHERE product_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sqlStock)) {
                stmt.setInt(1, product.getProductId());
                stmt.executeUpdate();
            }

            // 5. Catat Transaksi
            String sqlLog = "INSERT INTO transaction (customer_id, product_id, transaction_type, amount) VALUES (?, ?, 'PAYMENT', ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sqlLog)) {
                stmt.setInt(1, cust.getId());
                stmt.setInt(2, product.getProductId());
                stmt.setDouble(3, product.getPrice());
                stmt.executeUpdate();
            }

            conn.commit(); // SAVE PERMANEN

            // Update Object Java Biar Sinkron
            cust.getWallet().processPayment(product.getPrice());
            product.setStock(product.getStock() - 1);

            System.out.println("Pembelian Berhasil!");
            return true;

        } catch (Exception e) {
            try {
                if (conn != null)
                    conn.rollback();
            } catch (SQLException ex) {
            }
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
            }
        }
    }
}