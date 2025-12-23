package models;

import java.util.Date;

public class Customer extends User {
    private String phoneNumber;
    private String pin;
    private Date dateCreatedAt;

    // Relasi Composition (Wajik Hitam): Customer HAS-A Wallet
    private Wallet wallet;

    public Customer(int id, String username, String password, String fullName, String phoneNumber, String pin) {
        super(id, username, password, fullName);
        this.phoneNumber = phoneNumber;
        this.pin = pin;
        this.dateCreatedAt = new Date();
    }

    // Method untuk set Wallet (wajib dipanggil pas login)
    public void setWallet(Wallet wallet) {
        this.wallet = wallet;
    }

    public Wallet getWallet() {
        return this.wallet;
    }

    public String getPin() {
        return this.pin;
    }

    // === IMPLEMENTASI ABSTRACT USER ===
    @Override
    public boolean login(String username, String password) {
        // Logic cek password sederhana
        return this.username.equals(username) && this.password.equals(password);
    }

    // === METHOD SESUAI DIAGRAM ===
    public void register() {
        // Logic register ke DB
    }

    public void viewProfile() {
        System.out.println("Name: " + this.fullName);
        System.out.println("Balance: " + this.wallet.checkBalance());
    }

    // Relasi "Selects" ke Product
    public void buyProduct(Product p) {
        if (this.wallet.processPayment(p.getPrice())) {
            System.out.println("Berhasil membeli " + p.getProductName());
        } else {
            System.out.println("Saldo tidak cukup!");
        }
    }
}