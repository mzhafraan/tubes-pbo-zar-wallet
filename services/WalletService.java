package services;

import infrastructure.DatabaseHelper;
import java.sql.*;
import models.Customer;
import models.Product;

public class WalletService {

    public boolean topUp(Customer cust, double amount) {
        if (amount <= 0) {
            System.out.println("❌ Amount harus positif!");
            return false;
        }
        String sqlUpdate = "UPDATE wallet SET balance = balance + ? WHERE customer_id = ?";
        String sqlLog = "INSERT INTO transaction (customer_id, transaction_type, amount) VALUES (?, 'TOPUP', ?)";

        try (Connection conn = DatabaseHelper.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement stmt = conn.prepareStatement(sqlUpdate)) {
                stmt.setDouble(1, amount);
                stmt.setInt(2, cust.getId());
                stmt.executeUpdate();
            }

            try (PreparedStatement stmt = conn.prepareStatement(sqlLog)) {
                stmt.setInt(1, cust.getId());
                stmt.setDouble(2, amount);
                stmt.executeUpdate();
            }

            conn.commit();

            cust.getWallet().topUp(amount);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean topUp(Customer cust, double amount, String provider) {
        System.out.println("\n[Sistem] Memproses Top Up via " + provider + "...");
        return topUp(cust, amount);
    }

    public boolean transfer(Customer sender, int targetCustId, double amount) {
        Connection conn = null;
        try {
            conn = DatabaseHelper.getConnection();
            conn.setAutoCommit(false);

            if (amount <= 0) {
                System.out.println("❌ Amount harus positif!");
                return false;
            }
            if (sender.getWallet().checkBalance() < amount) {
                System.out.println("❌ Saldo tidak cukup!");
                return false;
            }

            String sqlDebit = "UPDATE wallet SET balance = balance - ? WHERE customer_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sqlDebit)) {
                stmt.setDouble(1, amount);
                stmt.setInt(2, sender.getId());
                stmt.executeUpdate();
            }

            String sqlCredit = "UPDATE wallet SET balance = balance + ? WHERE customer_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sqlCredit)) {
                stmt.setDouble(1, amount);
                stmt.setInt(2, targetCustId);
                int row = stmt.executeUpdate();

                if (row == 0) {
                    System.out.println("ID Penerima tidak ditemukan!");
                    throw new SQLException("User not found");
                }
            }

            String sqlLog = "INSERT INTO transaction (customer_id, target_customer_id, transaction_type, amount) VALUES (?, ?, 'TRANSFER', ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sqlLog)) {
                stmt.setInt(1, sender.getId());
                stmt.setInt(2, targetCustId);
                stmt.setDouble(3, amount);
                stmt.executeUpdate();
            }

            conn.commit();

            sender.getWallet().processPayment(amount);
            return true;

        } catch (Exception e) {

            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (Exception ex) {
            }
            return false;
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception ex) {
            }
        }
    }

    public boolean buyProduct(Customer cust, Product product) {
        Connection conn = null;
        try {
            conn = DatabaseHelper.getConnection();
            conn.setAutoCommit(false);

            if (cust.getWallet().checkBalance() < product.getPrice()) {
                System.out.println("Saldo tidak cukup!");
                return false;
            }

            String checkStockSql = "SELECT stock FROM product WHERE product_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(checkStockSql)) {
                stmt.setInt(1, product.getProductId());
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    if (rs.getInt("stock") <= 0) {
                        System.out.println("Stok Habis!");
                        return false;
                    }
                } else {
                    return false;
                }
            }

            String sqlDebit = "UPDATE wallet SET balance = balance - ? WHERE customer_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sqlDebit)) {
                stmt.setDouble(1, product.getPrice());
                stmt.setInt(2, cust.getId());
                stmt.executeUpdate();
            }

            String sqlStock = "UPDATE product SET stock = stock - 1 WHERE product_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sqlStock)) {
                stmt.setInt(1, product.getProductId());
                stmt.executeUpdate();
            }

            String sqlLog = "INSERT INTO transaction (customer_id, product_id, transaction_type, amount) VALUES (?, ?, 'PAYMENT', ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sqlLog)) {
                stmt.setInt(1, cust.getId());
                stmt.setInt(2, product.getProductId());
                stmt.setDouble(3, product.getPrice());
                stmt.executeUpdate();
            }

            conn.commit();

            cust.getWallet().processPayment(product.getPrice());
            product.setStock(product.getStock() - 1);

            System.out.println("Pembelian Berhasil!");
            return true;

        } catch (Exception e) {
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException ex) {
            }
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ex) {
            }
        }
    }

    public java.util.List<models.Transaction> getTransactionHistory(int customerId) {
        java.util.List<models.Transaction> history = new java.util.ArrayList<>();

        // JOIN untuk ambil nama produk, nama tujuan, nama pengirim
        String sql = "SELECT t.*, " +
                "p.product_name, " +
                "c_target.full_name as target_name, " +
                "c_source.full_name as source_name " +
                "FROM `transaction` t " +
                "LEFT JOIN product p ON t.product_id = p.product_id " +
                "LEFT JOIN customer c_target ON t.target_customer_id = c_target.customer_id " +
                "LEFT JOIN customer c_source ON t.customer_id = c_source.customer_id " +
                "WHERE t.customer_id = ? OR t.target_customer_id = ? " +
                "ORDER BY t.timestamp DESC";

        try (Connection conn = DatabaseHelper.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, customerId);
            stmt.setInt(2, customerId);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("transaction_id");
                int custId = rs.getInt("customer_id"); // Pengirim
                String typeStr = rs.getString("transaction_type");
                double amount = rs.getDouble("amount");
                Timestamp time = rs.getTimestamp("timestamp");

                models.Transaction.TransactionType type = models.Transaction.TransactionType.valueOf(typeStr);

                models.Transaction trx = new models.Transaction(id, custId, type, amount);
                trx.setTimestamp(time);

                // Set detail info
                trx.setTargetCustomerId(rs.getInt("target_customer_id"));
                trx.setProductName(rs.getString("product_name"));
                trx.setTargetUserName(rs.getString("target_name"));
                trx.setSourceUserName(rs.getString("source_name"));

                history.add(trx);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return history;
    }

    public java.util.List<models.Transaction> getAllTransactions() {
        java.util.List<models.Transaction> history = new java.util.ArrayList<>();
        // JOIN juga untuk admin view
        String sql = "SELECT t.*, " +
                "p.product_name, " +
                "c_target.full_name as target_name, " +
                "c_source.full_name as source_name " +
                "FROM transaction t " +
                "LEFT JOIN product p ON t.product_id = p.product_id " +
                "LEFT JOIN customer c_target ON t.target_customer_id = c_target.customer_id " +
                "LEFT JOIN customer c_source ON t.customer_id = c_source.customer_id " +
                "ORDER BY t.timestamp DESC";

        try (Connection conn = DatabaseHelper.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int id = rs.getInt("transaction_id");
                int custId = rs.getInt("customer_id");
                String typeStr = rs.getString("transaction_type");
                double amount = rs.getDouble("amount");

                int targetId = rs.getInt("target_customer_id");

                models.Transaction.TransactionType type = models.Transaction.TransactionType.valueOf(typeStr);
                models.Transaction trx = new models.Transaction(id, custId, type, amount);
                try {
                    trx.setTimestamp(rs.getTimestamp("timestamp"));
                } catch (Exception e) {
                }

                // Set detail info
                trx.setTargetCustomerId(targetId);
                trx.setProductName(rs.getString("product_name"));
                trx.setTargetUserName(rs.getString("target_name"));
                trx.setSourceUserName(rs.getString("source_name"));

                history.add(trx);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return history;
    }
}
