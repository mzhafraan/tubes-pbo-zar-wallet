package models;

import java.util.ArrayList;
import java.util.List;

public class Admin extends User {
    private String adminCode;
    private List<User> userList;       // Admin bisa liat semua user
    private List<Product> productList; // Admin manage produk

    public Admin(int id, String username, String password, String fullName, String adminCode) {
        super(id, username, password, fullName);
        this.adminCode = adminCode;
        this.userList = new ArrayList<>();
        this.productList = new ArrayList<>();
    }

    @Override
    public boolean login(String username, String password) {
        return this.username.equals(username) && this.password.equals(password);
    }

    public void addProduct(Product p) {
        this.productList.add(p);
    }
}