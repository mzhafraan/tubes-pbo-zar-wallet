package services;

import infrastructure.DatabaseHelper;
import java.sql.*;
import models.Customer;
import models.Product;

public class WalletService {

    // TOP UP SALDO
    public boolean topUp(Customer cust, double amount) {
        if (amount <= 0) {
            System.out.println("❌ Amount harus positif!");
            return false;
        }
        String sqlUpdate = "UPDATE wallet SET balance = balance + ? WHERE customer_id = ?";
        String sqlLog = "INSERT INTO transaction (customer_id, transaction_type, amount) VALUES (?, 'TOPUP', ?)";

        try (Connection conn = DatabaseHelper.getConnection()) {
            conn.setAutoCommit(false); // MULAI TRANSAKSI (Biar aman)

            // 1. Update Saldo di Database
            try (PreparedStatement stmt = conn.prepareStatement(sqlUpdate)) {
                stmt.setDouble(1, amount);
                stmt.setInt(2, cust.getId());
                stmt.executeUpdate();
            }

            // 2. Catat Riwayat Transaksi
            try (PreparedStatement stmt = conn.prepareStatement(sqlLog)) {
                stmt.setInt(1, cust.getId());
                stmt.setDouble(2, amount);
                stmt.executeUpdate();
            }

            conn.commit(); // SIMPAN PERMANEN

            // Update juga saldo di aplikasi biar langsung berubah
            cust.getWallet().topUp(amount);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // TRANSFER SALDO (Antar User)
    public boolean transfer(Customer sender, int targetCustId, double amount) {
        Connection conn = null;
        try {
            conn = DatabaseHelper.getConnection();
            conn.setAutoCommit(false); // cek manual commit

            // 1. Cek Saldo Pengirim Cukup Gak?
            if (amount <= 0) {
                System.out.println("❌ Amount harus positif!");
                return false;
            }
            if (sender.getWallet().checkBalance() < amount) {
                System.out.println("❌ Saldo tidak cukup!");
                return false;
            }

            // 2. Kurangi Saldo Pengirim
            String sqlDebit = "UPDATE wallet SET balance = balance - ? WHERE customer_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sqlDebit)) {
                stmt.setDouble(1, amount);
                stmt.setInt(2, sender.getId());
                stmt.executeUpdate();
            }

            // 3. Tambah Saldo Penerima
            String sqlCredit = "UPDATE wallet SET balance = balance + ? WHERE customer_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sqlCredit)) {
                stmt.setDouble(1, amount);
                stmt.setInt(2, targetCustId);
                int row = stmt.executeUpdate();

                // Kalau row 0 artinya ID Penerima gak ketemu
                if (row == 0) {
                    System.out.println("❌ ID Penerima tidak ditemukan!");
                    throw new SQLException("User not found");
                }
            }

            // 4. Catat Transaksi (TRANSFER)
            String sqlLog = "INSERT INTO transaction (customer_id, target_customer_id, transaction_type, amount) VALUES (?, ?, 'TRANSFER', ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sqlLog)) {
                stmt.setInt(1, sender.getId());
                stmt.setInt(2, targetCustId);
                stmt.setDouble(3, amount);
                stmt.executeUpdate();
            }

            conn.commit(); // SAVE SEMUA PROSES

            // Update saldo di memori Java
            sender.getWallet().processPayment(amount);
            return true;

        } catch (Exception e) {
            // Kalau ada error, batalkan semua perubahan (Rollback)
            try {
                if (conn != null)
                    conn.rollback();
            } catch (Exception ex) {
            }
            return false;
        } finally {
            try {
                if (conn != null)
                    conn.close();
            } catch (Exception ex) {
            }
        }
    }

    // BELI PRODUK (Pulsa/Token/Dll)
    public boolean buyProduct(Customer cust, Product product) {
        Connection conn = null;
        try {
            conn = DatabaseHelper.getConnection();
            conn.setAutoCommit(false); // cek manual commit

            // 1. Cek Saldo User
            if (cust.getWallet().checkBalance() < product.getPrice()) {
                System.out.println("❌ Saldo tidak cukup!");
                return false;
            }

            // 2. Cek Stok Produk
            String checkStockSql = "SELECT stock FROM product WHERE product_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(checkStockSql)) {
                stmt.setInt(1, product.getProductId());
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    if (rs.getInt("stock") <= 0) {
                        System.out.println("❌ Stok Habis!");
                        return false;
                    }
                } else {
                    return false; // Produk ga ada
                }
            }

            // 3. Potong Saldo User
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

            // 5. Catat Transaksi (PAYMENT)
            String sqlLog = "INSERT INTO transaction (customer_id, product_id, transaction_type, amount) VALUES (?, ?, 'PAYMENT', ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sqlLog)) {
                stmt.setInt(1, cust.getId());
                stmt.setInt(2, product.getProductId());
                stmt.setDouble(3, product.getPrice());
                stmt.executeUpdate();
            }

            conn.commit(); // Simpan permanen

            // Update data di aplikasi
            cust.getWallet().processPayment(product.getPrice());
            product.setStock(product.getStock() - 1);

            System.out.println("✅ Pembelian Berhasil!");
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

    // FITUR 4: CEK HISTORY TRANSAKSI
    public java.util.List<models.Transaction> getTransactionHistory(int customerId) {
        java.util.List<models.Transaction> history = new java.util.ArrayList<>();
        String sql = "SELECT * FROM transaction WHERE customer_id = ? OR target_customer_id = ? ORDER BY timestamp DESC";

        try (Connection conn = DatabaseHelper.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, customerId);
            stmt.setInt(2, customerId);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("transaction_id");
                int custId = rs.getInt("customer_id");
                String typeStr = rs.getString("transaction_type");
                double amount = rs.getDouble("amount");
                Timestamp time = rs.getTimestamp("timestamp");

                // Konversi String ke Enum (ERROR HANDLING if null)
                models.Transaction.TransactionType type = models.Transaction.TransactionType.valueOf(typeStr);

                models.Transaction trx = new models.Transaction(id, custId, type, amount);
                trx.setTimestamp(time); // Set waktu asli dari DB
                // Set data tambahan manual karena constructor terbatas
                // Note: Idealnya constructor Transaction diupdate, tapi ini cara cepat

                history.add(trx);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return history;
    }

    // FITUR ADMIN: LIHAT SEMUA TRANSAKSI
    public java.util.List<models.Transaction> getAllTransactions() {
        java.util.List<models.Transaction> history = new java.util.ArrayList<>();
        String sql = "SELECT * FROM transaction ORDER BY timestamp DESC";

        try (Connection conn = DatabaseHelper.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int id = rs.getInt("transaction_id");
                int custId = rs.getInt("customer_id");
                String typeStr = rs.getString("transaction_type");
                double amount = rs.getDouble("amount");

                // Optional: Ambil target customer buat info
                int targetId = rs.getInt("target_customer_id");

                models.Transaction.TransactionType type = models.Transaction.TransactionType.valueOf(typeStr);
                models.Transaction trx = new models.Transaction(id, custId, type, amount);
                try {
                    trx.setTimestamp(rs.getTimestamp("timestamp"));
                } catch (Exception e) {
                }

                // Set manual fields yang gak ada di constructor
                // Asumsi Transaction punya method setTargetCustomerId
                // Kalau gak ada, kita biarin aja standard logic

                history.add(trx);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return history;
    }
}