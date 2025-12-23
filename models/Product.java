package models;

public class Product {
    private int productId;
    private String productName;
    private int stock;
    private double price;
    private String category;

    public Product(int productId, String productName, int stock, double price, String category) {
        this.productId = productId;
        this.productName = productName;
        this.stock = stock;
        this.price = price;
        this.category = category;
    }

    // Method sesuai diagram: +setStock(int quantity)
    public void setStock(int quantity) {
        this.stock = quantity;
    }
    
    // Getter biar data bisa diambil
    public int getProductId() { return productId; }
    public String getProductName() { return productName; }
    public double getPrice() { return price; }
    public int getStock() { return stock; }
}