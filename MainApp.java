import java.util.List;
import java.util.Scanner;
import models.*;
import services.*;

public class MainApp {
    // 1. Inisialisasi Service & Scanner
    private static Scanner scanner = new Scanner(System.in);
    private static AuthService authService = new AuthService();
    private static WalletService walletService = new WalletService();
    private static ProductService productService = new ProductService();

    // Simpan data user yang lagi login
    private static User currentUser = null;

    public static void main(String[] args) {
        int choice;
        do {
            System.out.println("\n==================================");
            System.out.println("   💸 ZAR WALLET   ");
            System.out.println("==================================");
            System.out.println("1. Login Customer");
            System.out.println("2. Login Admin");
            System.out.println("3. Register");
            System.out.println("4. Exit");
            System.out.print(">> Pilih Menu: ");

            try {
                choice = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                choice = 0; // Handle kalau user input huruf
            }

            switch (choice) {
                case 1:
                    loginCustomerFlow();
                    break;
                case 2:
                    loginAdminFlow(); // Simpel aja buat admin
                    break;
                case 3:
                    handleRegisterFlow();
                    break;
                case 4:
                    System.out.println("Terima kasih sudah menggunakan ZAR WALLET!");
                    break;
                default:
                    System.out.println("Input salah, Bro!");
            }
        } while (choice != 4);
    }

    // ==========================================
    // FLOW LOGIN CUSTOMER
    // ==========================================
    private static void loginCustomerFlow() {
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();

        // Panggil Service Adam
        Customer customer = authService.loginCustomer(username, password);

        if (customer != null) {
            currentUser = customer;
            System.out.println("Login Sukses! Welcome, " + customer.getFullName());
            showCustomerMenu((Customer) currentUser);
        } else {
            System.out.println("❌ Login Gagal! Cek username/password.");
        }
    }

    // ==========================================
    // MENU UTAMA CUSTOMER
    // ==========================================
    private static void showCustomerMenu(Customer cust) {
        boolean isRunning = true;
        while (isRunning) {
            // Tampilan Header Saldo Realtime
            System.out.println("\n--- 👤 DASHBOARD NASABAH ---");
            System.out.println("Nama   : " + cust.getFullName());
            System.out.println("Saldo  : Rp " + String.format("%,.0f", cust.getWallet().checkBalance())); // Format
                                                                                                          // Rupiah
            System.out.println("----------------------------");
            System.out.println("1. Transfer Saldo");
            System.out.println("2. Beli Produk (Pulsa/Token)");
            System.out.println("3. Top Up Saldo");
            System.out.println("4. Cek Profile & PIN");
            System.out.println("5. Logout");
            System.out.print(">> Mau ngapain: ");

            String menu = scanner.nextLine();
            switch (menu) {
                case "1":
                    handleTransfer(cust);
                    break;
                case "2":
                    handleBuyProduct(cust);
                    break;
                case "3":
                    handleTopUp(cust);
                    break;
                case "4":
                    cust.viewProfile(); // Method dari Class Customer
                    break;
                case "5":
                    isRunning = false;
                    currentUser = null;
                    System.out.println("Logging out...");
                    break;
                default:
                    System.out.println("Pilih angka 1-5 aja.");
            }
        }
    }

    // ==========================================
    // LOGIC FITUR (Handling Input)
    // ==========================================

    // Fitur Transfer (Panggil Logic Zhafran)
    private static void handleTransfer(Customer cust) {
        System.out.print("\nMasukkan ID Tujuan (Cth: 2): ");
        try {
            int targetId = Integer.parseInt(scanner.nextLine());
            System.out.print("Nominal Transfer: Rp ");
            double amount = Double.parseDouble(scanner.nextLine());

            // Validasi PIN dulu (Sesuai Diagram)
            System.out.print("Masukkan PIN Kamu: ");
            String pin = scanner.nextLine();

            if (!cust.getWallet().validatePin(pin)) { // Sementara return true
                System.out.println("PIN Salah!");
                return;
            }

            // Eksekusi Transfer
            boolean success = walletService.transfer(cust, targetId, amount);
            if (success) {
                System.out.println("Transfer BERHASIL!");
            }
        } catch (NumberFormatException e) {
            System.out.println("Input harus angka!");
        }
    }

    // Fitur Beli Produk (Panggil Logic Product Service)
    private static void handleBuyProduct(Customer cust) {
        System.out.println("\n=== DAFTAR PRODUK ===");
        List<Product> products = productService.getAllProducts();

        // Looping Nampilin Produk
        for (Product p : products) {
            System.out.printf("[%d] %s - Harga: Rp %,.0f (Stok: %d)\n",
                    p.getProductId(), p.getProductName(), p.getPrice(), p.getStock());
        }

        if (cust == null) {
            System.out.println("---------------------------");
            return;
        }

        System.out.print("Pilih ID Produk: ");
        try {
            int pId = Integer.parseInt(scanner.nextLine());

            // Cari produk di list (Simpel search)
            Product selectedProduct = null;
            for (Product p : products) {
                if (p.getProductId() == pId) {
                    selectedProduct = p;
                    break;
                }
            }

            if (selectedProduct != null) {
                // Logic Bayar (Kurangi saldo & Kurangi stok) via Service yang support
                // Transaction
                walletService.buyProduct(cust, selectedProduct);

                // Note: Logic update stok di DB harusnya dipanggil di dalam method buyProduct
                // Tapi untuk simulasi console, ini cukup.
            } else {
                System.out.println("Produk tidak ditemukan.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Input error.");
        }
    }

    // Fitur Top Up
    private static void handleTopUp(Customer cust) {
        System.out.print("\nMau Top Up berapa: Rp ");
        try {
            double amount = Double.parseDouble(scanner.nextLine());
            boolean success = walletService.topUp(cust, amount);
            if (success) {
                System.out.println("Top Up Berhasil! Saldo bertambah.");
            } else {
                System.out.println("Top Up Gagal.");
            }
        } catch (Exception e) {
            System.out.println("Input angka aja.");
        }
    }

    // ==========================================
    // FLOW LOGIN ADMIN (Simpel)
    // ==========================================
    private static void loginAdminFlow() {
        // Hardcode dulu atau bikin AuthService.loginAdmin()
        System.out.print("Admin Code: ");
        String code = scanner.nextLine();

        if (code.equals("ADM001")) { // Sesuai Dummy Data
            boolean isAdmin = true;
            while (isAdmin) {
                System.out.println("\n--- 🛠 MENU ADMIN ---");
                System.out.println("1. Lihat Semua Produk");
                System.out.println("2. Restock Produk");
                System.out.println("3. Back");
                System.out.print(">> Pilih: ");
                String admMenu = scanner.nextLine();

                if (admMenu.equals("1")) {
                    handleBuyProduct(null); // Reuse method nampilin produk (hack dikit)
                } else if (admMenu.equals("2")) {
                    System.out.print("ID Produk: ");
                    try {
                        int pid = Integer.parseInt(scanner.nextLine());
                        System.out.print("Tambah Stok: ");
                        int stock = Integer.parseInt(scanner.nextLine());
                        productService.addStock(pid, stock);
                        System.out.println("Stok Updated!");
                    } catch (NumberFormatException e) {
                        System.out.println("Input angka aja bro!");
                    }
                } else if (admMenu.equals("3")) {
                    isAdmin = false;
                }
            }
        } else {
            System.out.println("Kode Admin Salah!");
        }
    }

    // ==========================================
    // FLOW REGISTRASI (Form Input)
    // ==========================================
    private static void handleRegisterFlow() {
        System.out.println("\n--- FORM REGISTRASI ---");

        // Input Data
        System.out.print("Username (Unik): ");
        String uname = scanner.nextLine();

        System.out.print("Password: ");
        String pass = scanner.nextLine();

        System.out.print("Nama Lengkap: ");
        String fname = scanner.nextLine();

        System.out.print("No HP (08xx): ");
        String phone = scanner.nextLine();

        System.out.print("Buat PIN (6 Angka): ");
        String pin = scanner.nextLine();

        // Validasi simpel sebelum kirim ke database
        if (pin.length() != 6) {
            System.out.println("Gagal: PIN harus 6 digit angka!");
            return;
        }

        // Panggil Logic AuthService
        System.out.println("Sedang memproses...");
        boolean success = authService.registerCustomer(uname, pass, fname, phone, pin);

        if (success) {
            System.out.println("Akun berhasil dibuat! Silakan Login.");
        } else {
            System.out.println("Registrasi Gagal! Username atau No HP mungkin sudah dipakai.");
        }
    }
}