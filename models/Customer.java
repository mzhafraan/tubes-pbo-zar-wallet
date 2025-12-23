package models;

import java.util.Date;

public class Customer extends User {

    private String phoneNumber;
    private String pin;
    private Date dateCreatedAt;

    private Wallet wallet;

    public Customer(int id, String username, String password, String fullName, String phoneNumber, String pin) {
        super(id, username, password, fullName);
        this.phoneNumber = phoneNumber;
        this.pin = pin;
        this.dateCreatedAt = new Date();
    }

    public void setWallet(Wallet wallet) {
        this.wallet = wallet;
    }

    public Wallet getWallet() {
        return this.wallet;
    }

    public String getPin() {
        return this.pin;
    }

    @Override
    public boolean login(String username, String password) {

        return this.username.equals(username) && this.password.equals(password);
    }

    public void register() {

    }

    public void viewProfile() {
        System.out.println("Name: " + this.fullName);
        System.out.println("Balance: " + this.wallet.checkBalance());
    }

    public void buyProduct(Product p) {
        if (this.wallet.processPayment(p.getPrice())) {
            System.out.println("Berhasil membeli " + p.getProductName());
        } else {
            System.out.println("Saldo tidak cukup!");
        }
    }
}
