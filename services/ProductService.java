package services;

import infrastructure.DatabaseHelper;
import models.Product;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductService {

    // GET ALL: Ambil semua produk buat ditampilin di menu
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
                    rs.getString("category")
                ));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // UPDATE STOCK: Pas ada yang beli atau admin nambah stok
    public void updateProductStock(int productId, int newStock) {
        String sql = "UPDATE product SET stock = ? WHERE product_id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, newStock);
            stmt.setInt(2, productId);
            stmt.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }
}