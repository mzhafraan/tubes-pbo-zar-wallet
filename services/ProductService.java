package services;

import infrastructure.DatabaseHelper;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import models.Product;

public class ProductService {

    public List<Product> getAllProducts() {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT * FROM product";

        try (Connection conn = DatabaseHelper.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(new Product(
                        rs.getInt("product_id"),
                        rs.getString("product_name"),
                        rs.getInt("stock"),
                        rs.getDouble("price"),
                        rs.getString("category")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public void addStock(int productId, int quantity) {
        String sql = "UPDATE product SET stock = stock + ? WHERE product_id = ?";
        try (Connection conn = DatabaseHelper.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, quantity);
            stmt.setInt(2, productId);
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean addProduct(String name, int stock, double price, String category) {
        String sql = "INSERT INTO product (product_name, stock, price, category) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseHelper.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setInt(2, stock);
            stmt.setDouble(3, price);
            stmt.setString(4, category);
            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
