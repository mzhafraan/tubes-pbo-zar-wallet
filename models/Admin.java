package models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Admin extends User {

    private String adminCode;

    // Admin manages Lists (sesuai diagram)
    private List<User> userList;
    private List<Product> productList;

    public Admin(int id, String username, String password, String fullName, String adminCode) {
        super(id, username, password, fullName);
        this.adminCode = adminCode;
        this.userList = new ArrayList<>();
        this.productList = new ArrayList<>();
    }

    // === IMPLEMENTASI ABSTRACT USER ===
    @Override
    public boolean login(String username, String password) {
        // Login admin mungkin ngecek adminCode juga
        return this.username.equals(username) && this.password.equals(password);
    }

    // === METHOD SESUAI DIAGRAM ===
    public void manageProducts() {
        System.out.println("Masuk ke menu management produk...");
    }

    public void addProduct(Product p) {
        this.productList.add(p);
        // Logic insert ke DB
    }

    public void deleteProduct(int productId) {
        // Logic delete dari list/DB
    }

    public void updateStock(Product p, int quantity) {
        p.setStock(quantity);
    }

    public void viewReports(Date startDate, Date endDate) {
        System.out.println("Menampilkan laporan dari " + startDate + " sampai " + endDate);
    }
}
