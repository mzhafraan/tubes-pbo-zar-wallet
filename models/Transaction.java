package models;

import java.util.Date;
import java.util.List;

public class Transaction {
    public enum TransactionType {
        TOPUP, TRANSFER, PAYMENT
    }

    private int transactionId;
    private int customerId;
    private TransactionType type;
    private double amount;
    private Date timestamp;
    private int targetCustomerId; // Untuk transfer

    // Relasi References: Transaction nyimpen object Product (bisa null kalau bukan
    // beli barang)
    private Product productItem;

    public Transaction(int transactionId, int customerId, TransactionType type, double amount) {
        this.transactionId = transactionId;
        this.customerId = customerId;
        this.type = type;
        this.amount = amount;
        this.timestamp = new Date(); // Set waktu sekarang
    }

    public void setTimestamp(Date date) {
        this.timestamp = date;
    }

    // Setter khusus untuk melengkapi data
    public void setTargetCustomerId(int id) {
        this.targetCustomerId = id;
    }

    public void setProductItem(Product p) {
        this.productItem = p;
    }

    // Method sesuai diagram
    public void saveHistory() {
        // Logic simpan ke DB (nanti dikerjakan di Service/DAO)
        System.out.println("Menyimpan transaksi ke database...");
    }

    // Di diagram return List, ini biasanya static method atau manggil dari DB
    public List<Transaction> getHistory(int customerId) {
        return null; // Nanti diisi logic fetch dari database
    }

    // Getter
    public Product getProductItem() {
        return productItem;
    }

    public double getAmount() {
        return amount;
    }

    public TransactionType getType() {
        return type;
    }

    public int getTransactionId() {
        return transactionId;
    }

    public int getCustomerId() {
        return customerId;
    }

    public Date getTimestamp() {
        return timestamp;
    }
}