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
            System.out.println("   💸 ZAR WALLET APPLICATION      ");
            System.out.println("==================================");
            System.out.println("1. Login Customer");
            System.out.println("2. Login Admin");
            System.out.println("3. Register Akun Baru");
            System.out.println("4. Keluar Aplikasi");
            System.out.print(">> Pilih Menu: ");

            try {
                choice = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                choice = 0;
            }

            switch (choice) {
                case 1:
                    loginCustomerFlow();
                    break;
                case 2:
                    loginAdminFlow();
                    break;
                case 3:
                    handleRegisterFlow();
                    break;
                case 4:
                    System.out.println("Terima kasih sudah menggunakan ZAR WALLET!");
                    break;
                default:
                    System.out.println("❌ Pilihan tidak valid, coba lagi!");
            }
        } while (choice != 4);
    }

    // ==========================================
    // FLOW 1: LOGIN CUSTOMER
    // ==========================================
    private static void loginCustomerFlow() {
        System.out.println("\n--- 🔐 LOGIN CUSTOMER ---");
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();

        // Panggil Service Authentication
        Customer customer = authService.loginCustomer(username, password);

        if (customer != null) {
            currentUser = customer;
            System.out.println("✅ Login Sukses! Selamat Datang, " + customer.getFullName());
            showCustomerMenu((Customer) currentUser);
        } else {
            System.out.println("❌ Login Gagal! Cek username atau password.");
        }
    }

    // ==========================================
    // FLOW 2: DASHBOARD CUSTOMER
    // ==========================================
    private static void showCustomerMenu(Customer cust) {
        boolean isRunning = true;
        while (isRunning) {
            // Tampilan Header Saldo Realtime
            System.out.println("\n--- 👤 DASHBOARD NASABAH ---");
            System.out.println("Nama   : " + cust.getFullName());
            System.out.println("Saldo  : Rp " + String.format("%,.0f", cust.getWallet().checkBalance()));
            System.out.println("----------------------------");
            System.out.println("1. Transfer Saldo");
            System.out.println("2. Beli Produk (Pulsa/Token)");
            System.out.println("3. Top Up Saldo");
            System.out.println("4. Cek Riwayat Transaksi");
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
                    handleCheckHistory(cust);
                    break;
                case "5":
                    isRunning = false;
                    currentUser = null;
                    System.out.println("👋 Dadah, sampai jumpa lagi!");
                    break;
                default:
                    System.out.println("❌ Pilih angka 1-5 aja.");
            }
        }
    }

    // ==========================================
    // FEATURE HANDLERS (Logika Input User)
    // ==========================================

    // 4. Cek History
    private static void handleCheckHistory(Customer cust) {
        System.out.println("\n--- 📜 RIWAYAT TRANSAKSI ---");
        List<Transaction> history = walletService.getTransactionHistory(cust.getId());

        if (history.isEmpty()) {
            System.out.println("Belum ada transaksi.");
        } else {
            for (Transaction trx : history) {
                // Determine icon based on type
                String icon = "📄";
                if (trx.getType() == Transaction.TransactionType.TOPUP)
                    icon = "➕";
                else if (trx.getType() == Transaction.TransactionType.TRANSFER)
                    icon = "💸";
                else if (trx.getType() == Transaction.TransactionType.PAYMENT)
                    icon = "🛒";

                System.out.printf("%s [%s] - Rp %,.0f\n",
                        icon, trx.getType(), trx.getAmount());
            }
        }
        System.out.println("----------------------------");
    }

    // 1. Transfer
    private static void handleTransfer(Customer cust) {
        System.out.println("\n--- 💸 TRANSFER SALDO ---");
        System.out.print("Masukkan ID Tujuan (Cth: 2): ");
        try {
            int targetId = Integer.parseInt(scanner.nextLine());
            System.out.print("Nominal Transfer: Rp ");
            double amount = Double.parseDouble(scanner.nextLine());

            if (amount <= 0) {
                System.out.println("❌ Nominal tidak valid (harus positif)!");
                return;
            }

            // Validasi PIN dulu
            System.out.print("Masukkan PIN Kamu: ");
            String pin = scanner.nextLine();

            // Cek PIN (Harusnya cek ke DB, tapi sementara simple check di model wallet)
            if (!cust.getWallet().validatePin(pin)) {
                System.out.println("❌ PIN Salah!");
                return;
            }

            // Panggil Service Wallet untuk proses transaksi
            boolean success = walletService.transfer(cust, targetId, amount);
            if (success) {
                System.out.println("✅ Transfer BERHASIL dikirim!");
            }
        } catch (NumberFormatException e) {
            System.out.println("❌ Input harus angka!");
        }
    }

    // 2. Beli Produk
    private static void handleBuyProduct(Customer cust) {
        System.out.println("\n=== 🛒 DAFTAR PRODUK ===");
        List<Product> products = productService.getAllProducts();

        // Tampilkan semua produk
        for (Product p : products) {
            System.out.printf("[%d] %s - Harga: Rp %,.0f (Stok: %d)\n",
                    p.getProductId(), p.getProductName(), p.getPrice(), p.getStock());
        }

        // Kalau null berarti mode Admin (cuma lihat doang)
        if (cust == null) {
            System.out.println("---------------------------");
            return;
        }

        System.out.print("\nPilih ID Produk yang mau dibeli: ");
        try {
            int pId = Integer.parseInt(scanner.nextLine());

            // Cari produk yang dipilih
            Product selectedProduct = null;
            for (Product p : products) {
                if (p.getProductId() == pId) {
                    selectedProduct = p;
                    break;
                }
            }

            if (selectedProduct != null) {
                // SECURITY CHECK: Minta PIN
                System.out.print("Masukkan PIN untuk Konfirmasi: ");
                String pin = scanner.nextLine();

                if (cust.getWallet().validatePin(pin)) {
                    // Proses pembelian via Service
                    walletService.buyProduct(cust, selectedProduct);
                } else {
                    System.out.println("❌ PIN Salah! Transaksi Dibatalkan.");
                }
            } else {
                System.out.println("❌ Produk tidak ditemukan.");
            }
        } catch (NumberFormatException e) {
            System.out.println("❌ Input error, masukkan angka ID.");
        }
    }

    // 3. Top Up
    private static void handleTopUp(Customer cust) {
        System.out.println("\n--- ➕ TOP UP SALDO ---");
        System.out.print("Mau Top Up berapa: Rp ");
        try {
            double amount = Double.parseDouble(scanner.nextLine());
            if (amount <= 0) {
                System.out.println("❌ Nominal harus positif!");
                return;
            }
            boolean success = walletService.topUp(cust, amount);
            if (success) {
                System.out.println("✅ Top Up Berhasil! Saldo bertambah.");
            } else {
                System.out.println("❌ Top Up Gagal.");
            }
        } catch (Exception e) {
            System.out.println("❌ Input angka aja.");
        }
    }

    // ==========================================
    // FLOW 3: LOGIN ADMIN
    // ==========================================
    private static void loginAdminFlow() {
        System.out.println("\n--- 🔐 LOGIN ADMIN ---");
        System.out.print("Masukkan Admin Code: ");
        String code = scanner.nextLine();

        if (code.equals("ADM001")) {
            boolean isAdmin = true;
            while (isAdmin) {
                System.out.println("\n--- 🛠 MENU ADMIN ---");
                System.out.println("1. Lihat Semua Produk");
                System.out.println("2. Restock Produk (Tambah Stok)");
                System.out.println("3. Lihat Semua User (Nasabah)");
                System.out.println("4. Lihat Laporan Transaksi");
                System.out.println("5. Kembali ke Menu Utama");
                System.out.print(">> Pilih: ");
                String admMenu = scanner.nextLine();

                if (admMenu.equals("1")) {
                    handleBuyProduct(null); // Reuse method view produk
                } else if (admMenu.equals("2")) {
                    System.out.print("Masukkan ID Produk: ");
                    try {
                        int pid = Integer.parseInt(scanner.nextLine());
                        System.out.print("Tambah Stok Berapa: ");
                        int stock = Integer.parseInt(scanner.nextLine());

                        // Panggil service untuk tambah stok
                        productService.addStock(pid, stock);
                        System.out.println("✅ Stok Berhasil Ditambahkan!");
                    } catch (NumberFormatException e) {
                        System.out.println("❌ Input angka aja bro!");
                    }
                } else if (admMenu.equals("3")) {
                    handleViewAllUsers();
                } else if (admMenu.equals("4")) {
                    handleViewAllTransactions();
                } else if (admMenu.equals("5")) {
                    isAdmin = false;
                }
            }
        } else {
            System.out.println("❌ Kode Admin Salah!");
        }
    }

    // Admin: Lihat Semua User
    private static void handleViewAllUsers() {
        System.out.println("\n--- 👥 DAFTAR SELURUH NASABAH ---");
        List<Customer> customers = authService.getAllCustomers();

        if (customers.isEmpty()) {
            System.out.println("Belum ada nasabah terdaftar.");
        } else {
            for (Customer c : customers) {
                // Tampilkan ID, Nama, Username, dan Saldo
                double balance = (c.getWallet() != null) ? c.getWallet().checkBalance() : 0.0;
                System.out.printf("🆔 [%d] %s (@%s) - Saldo: Rp %,.0f\n",
                        c.getId(), c.getFullName(), c.getUsername(), balance);
            }
        }
        System.out.println("---------------------------------");
    }

    // Admin: Lihat Semua Transaksi
    private static void handleViewAllTransactions() {
        System.out.println("\n--- 📊 LAPORAN SEMUA TRANSAKSI ---");
        List<Transaction> transactions = walletService.getAllTransactions();

        if (transactions.isEmpty()) {
            System.out.println("Belum ada data transaksi.");
        } else {
            for (Transaction trx : transactions) {
                String icon = "📄";
                if (trx.getType() == Transaction.TransactionType.TOPUP)
                    icon = "➕";
                else if (trx.getType() == Transaction.TransactionType.TRANSFER)
                    icon = "💸";
                else if (trx.getType() == Transaction.TransactionType.PAYMENT)
                    icon = "🛒";

                System.out.printf("%s [trx_id:%d] Cust:%d - %s : Rp %,.0f\n",
                        icon, trx.getTransactionId(), trx.getCustomerId(), trx.getType(), trx.getAmount());
            }
        }
        System.out.println("----------------------------------");
    }

    // ==========================================
    // FLOW 4: REGISTRASI
    // ==========================================
    private static void handleRegisterFlow() {
        System.out.println("\n--- 📝 FORM REGISTRASI ---");
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

        if (pin.length() != 6) {
            System.out.println("❌ Gagal: PIN harus 6 digit angka!");
            return;
        }

        System.out.println("Sedang memproses...");
        boolean success = authService.registerCustomer(uname, pass, fname, phone, pin);

        if (success) {
            System.out.println("✅ Akun berhasil dibuat! Silakan Login.");
        } else {
            System.out.println("❌ Registrasi Gagal! Username atau No HP sudah terdaftar.");
        }
    }
}