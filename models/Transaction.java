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
    private int targetCustomerId; 

    
    
    private Product productItem;

    public Transaction(int transactionId, int customerId, TransactionType type, double amount) {
        this.transactionId = transactionId;
        this.customerId = customerId;
        this.type = type;
        this.amount = amount;
        this.timestamp = new Date(); 
    }

    public void setTimestamp(Date date) {
        this.timestamp = date;
    }

    
    public void setTargetCustomerId(int id) {
        this.targetCustomerId = id;
    }

    public void setProductItem(Product p) {
        this.productItem = p;
    }

    
    public void saveHistory() {
        
        System.out.println("Menyimpan transaksi ke database...");
    }

    
    public List<Transaction> getHistory(int customerId) {
        return null; 
    }

    
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

    
    private String productName;
    private String targetUserName;
    private String sourceUserName; 

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getTargetUserName() {
        return targetUserName;
    }

    public void setTargetUserName(String targetUserName) {
        this.targetUserName = targetUserName;
    }

    public String getSourceUserName() {
        return sourceUserName;
    }

    public void setSourceUserName(String sourceUserName) {
        this.sourceUserName = sourceUserName;
    }

    public Date getTimestamp() {
        return timestamp;
    }
}