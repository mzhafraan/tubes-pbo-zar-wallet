package models;

public interface IPaymentService {
    boolean processPayment(double amount);
    boolean validatePin(String pin);
}