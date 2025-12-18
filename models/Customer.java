package models;

import java.util.Date;

public class Customer extends User {
    private String phoneNumber;
    private String pin;
    private Date createdAt;
    private Wallet wallet; // Composition: Customer HAS-A Wallet

    public Customer(int id, String username, String password, String fullName, String phoneNumber, String pin) {
        super(id, username, password, fullName);
        this.phoneNumber = phoneNumber;
        this.pin = pin;
        this.createdAt = new Date();
    }
    
    // Saat customer diload, wallet-nya harus diset
    public void setWallet(Wallet wallet) {
        this.wallet = wallet;
    }

    @Override
    public boolean login(String username, String password) {
        // Logic validasi login nanti
        return this.username.equals(username) && this.password.equals(password);
    }
}