package models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

// Extends/Implements sesuai garis putus-putus segitiga di diagram
public class Wallet implements IPaymentService {

    private int walletId;
    private int customerId;
    private double balance;
    private Date lastUpdate;
    private String pin;

    // Relasi Aggregation (Wajik Kosong): Wallet punya List Transaction
    private List<Transaction> transactionHistory;

    public Wallet(int walletId, int customerId, double balance, String pin) {
        this.walletId = walletId;
        this.customerId = customerId;
        this.balance = balance;
        this.pin = pin;
        this.lastUpdate = new Date();
        this.transactionHistory = new ArrayList<>(); // Inisialisasi List
    }

    // === IMPLEMENTASI INTERFACE ===
    @Override
    public boolean processPayment(double amount) {
        if (this.balance >= amount) {
            this.balance -= amount;
            return true;
        }
        return false;
    }

    @Override
    public boolean validatePin(String inputPin) {
        if (this.pin != null && this.pin.equals(inputPin)) {
            return true; // PIN Cocok
        }
        return false;
    }

    // === METHOD SESUAI DIAGRAM ===
    public double checkBalance() {
        return this.balance;
    }

    public boolean topUp(double amount) {
        this.balance += amount;
        return true;
    }

    public boolean transfer(int targetCustomerId, double amount) {
        // Di diagram method ini ada di sini.
        // Realitanya, logic ini nanti manggil WalletService buat update DB.
        if (this.balance >= amount) {
            this.balance -= amount;
            return true;
        }
        return false;
    }

    public List<Transaction> getTransactions() {
        return this.transactionHistory;
    }

    // Getter ID
    public int getWalletId() {
        return walletId;
    }
}
