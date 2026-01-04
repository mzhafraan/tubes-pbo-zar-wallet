-- --------------------------------------------------------
-- Host:                         127.0.0.1
-- Server version:               8.0.30 - MySQL Community Server - GPL
-- Server OS:                    Win64
-- HeidiSQL Version:             12.1.0.6537
-- --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;


-- Dumping database structure for ewallet_db
CREATE DATABASE IF NOT EXISTS `ewallet_db` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `ewallet_db`;

-- Dumping structure for table ewallet_db.admin
CREATE TABLE IF NOT EXISTS `admin` (
  `admin_id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `password` varchar(255) NOT NULL,
  `full_name` varchar(100) NOT NULL,
  `admin_code` varchar(20) NOT NULL,
  PRIMARY KEY (`admin_id`),
  UNIQUE KEY `username` (`username`),
  UNIQUE KEY `admin_code` (`admin_code`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Dumping data for table ewallet_db.admin: ~1 rows (approximately)
INSERT INTO `admin` (`admin_id`, `username`, `password`, `full_name`, `admin_code`) VALUES
	(1, 'admin1', 'admin123', 'Super Admin', 'ADM001');

-- Dumping structure for table ewallet_db.customer
CREATE TABLE IF NOT EXISTS `customer` (
  `customer_id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `password` varchar(255) NOT NULL,
  `full_name` varchar(100) NOT NULL,
  `phone_number` varchar(15) DEFAULT NULL,
  `pin` varchar(6) NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`customer_id`),
  UNIQUE KEY `username` (`username`),
  UNIQUE KEY `phone_number` (`phone_number`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Dumping data for table ewallet_db.customer: ~2 rows (approximately)
INSERT INTO `customer` (`customer_id`, `username`, `password`, `full_name`, `phone_number`, `pin`, `created_at`) VALUES
	(1, 'johndoe', 'pass123', 'John Doe', '081234567890', '123456', '2025-12-18 08:07:39'),
	(2, 'janedoe', 'pass123', 'Jane Doe', '089876543210', '654321', '2025-12-18 08:07:39'),
	(3, 'vionak', '1234', 'Viona', '09123980', '123456', '2025-12-18 09:13:29'),
	(5, 'yajid', '1234', 'Yazid Istiqlal', '0832764803', '123456', '2025-12-21 14:00:42'),
	(6, 'zhafran', '1234', 'Muhammad Zhafran', '0839498983294', '123456', '2025-12-23 19:12:14'),
	(7, 'faiz', '1234', 'Faiz Bastian', '083246327843', '123456', '2025-12-25 13:48:47'),
	(9, 'adamzakky', '123456', 'Adam Zakky Maulana', '081232432438', '123456', '2026-01-04 20:16:11'),
	(10, 'dzikralang', '123456', 'Dzikra Langit', '234993242934', '123456', '2026-01-04 20:35:44');

-- Dumping structure for table ewallet_db.product
CREATE TABLE IF NOT EXISTS `product` (
  `product_id` int NOT NULL AUTO_INCREMENT,
  `product_name` varchar(100) NOT NULL,
  `price` decimal(15,2) NOT NULL,
  `stock` int DEFAULT '0',
  `category` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`product_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Dumping data for table ewallet_db.product: ~3 rows (approximately)
INSERT INTO `product` (`product_id`, `product_name`, `price`, `stock`, `category`) VALUES
	(1, 'Pulsa 50k', 52000.00, 100, 'Pulsa'),
	(2, 'Token Listrik 100k', 102000.00, 50, 'Listrik'),
	(3, 'Voucher Game', 25000.00, 200, 'Game');

-- Dumping structure for table ewallet_db.transaction
CREATE TABLE IF NOT EXISTS `transaction` (
  `transaction_id` int NOT NULL AUTO_INCREMENT,
  `customer_id` int NOT NULL,
  `target_customer_id` int DEFAULT NULL,
  `product_id` int DEFAULT NULL,
  `transaction_type` enum('TOPUP','TRANSFER','PAYMENT') NOT NULL,
  `amount` decimal(15,2) NOT NULL,
  `timestamp` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`transaction_id`),
  KEY `customer_id` (`customer_id`),
  KEY `target_customer_id` (`target_customer_id`),
  KEY `product_id` (`product_id`),
  CONSTRAINT `transaction_ibfk_1` FOREIGN KEY (`customer_id`) REFERENCES `customer` (`customer_id`),
  CONSTRAINT `transaction_ibfk_2` FOREIGN KEY (`target_customer_id`) REFERENCES `customer` (`customer_id`),
  CONSTRAINT `transaction_ibfk_3` FOREIGN KEY (`product_id`) REFERENCES `product` (`product_id`)
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Dumping data for table ewallet_db.transaction: ~17 rows (approximately)
INSERT INTO `transaction` (`transaction_id`, `customer_id`, `target_customer_id`, `product_id`, `transaction_type`, `amount`, `timestamp`) VALUES
	(1, 1, NULL, NULL, 'TOPUP', 500000.00, '2025-12-18 08:07:39'),
	(2, 1, 2, NULL, 'TRANSFER', 50000.00, '2025-12-18 08:07:39'),
	(3, 2, NULL, 1, 'PAYMENT', 52000.00, '2025-12-18 08:07:39'),
	(4, 3, NULL, NULL, 'TOPUP', 100000.00, '2025-12-18 09:45:59'),
	(5, 3, 1, NULL, 'TRANSFER', 10000.00, '2025-12-18 09:46:27'),
	(6, 3, NULL, 3, 'PAYMENT', 25000.00, '2025-12-18 09:46:44'),
	(7, 3, NULL, NULL, 'TOPUP', 35000.00, '2025-12-21 13:59:14'),
	(8, 5, NULL, NULL, 'TOPUP', 50000.00, '2025-12-21 14:01:15'),
	(9, 5, 3, NULL, 'TRANSFER', 25000.00, '2025-12-21 14:01:50'),
	(10, 3, 5, NULL, 'TRANSFER', 25000.00, '2025-12-23 17:44:14'),
	(11, 3, 5, NULL, 'TRANSFER', 10000.00, '2025-12-23 17:44:43'),
	(12, 3, NULL, NULL, 'TOPUP', 10000.00, '2025-12-23 17:57:10'),
	(13, 3, NULL, 3, 'PAYMENT', 25000.00, '2025-12-23 17:57:21'),
	(14, 3, 5, NULL, 'TRANSFER', 25000.00, '2025-12-23 17:57:49'),
	(15, 5, 1, NULL, 'TRANSFER', 15000.00, '2025-12-23 19:10:07'),
	(16, 5, NULL, NULL, 'TOPUP', 30000.00, '2025-12-23 19:10:38'),
	(17, 5, NULL, 3, 'PAYMENT', 25000.00, '2025-12-23 19:11:04'),
	(18, 10, NULL, NULL, 'TOPUP', 10000.00, '2026-01-04 20:36:02'),
	(19, 10, 1, NULL, 'TRANSFER', 5000.00, '2026-01-04 20:42:31'),
	(20, 10, NULL, NULL, 'TOPUP', 100000.00, '2026-01-04 20:43:34'),
	(21, 10, NULL, 3, 'PAYMENT', 25000.00, '2026-01-04 20:43:52');

-- Dumping structure for table ewallet_db.wallet
CREATE TABLE IF NOT EXISTS `wallet` (
  `wallet_id` int NOT NULL AUTO_INCREMENT,
  `customer_id` int NOT NULL,
  `balance` decimal(15,2) DEFAULT '0.00',
  `last_update` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`wallet_id`),
  UNIQUE KEY `customer_id` (`customer_id`),
  CONSTRAINT `wallet_ibfk_1` FOREIGN KEY (`customer_id`) REFERENCES `customer` (`customer_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Dumping data for table ewallet_db.wallet: ~2 rows (approximately)
INSERT INTO `wallet` (`wallet_id`, `customer_id`, `balance`, `last_update`) VALUES
	(1, 1, 530000.00, '2026-01-04 20:42:31'),
	(2, 2, 100000.00, '2025-12-18 08:07:39'),
	(3, 3, 50000.00, '2025-12-23 17:57:49'),
	(4, 5, 75000.00, '2025-12-23 19:11:04'),
	(5, 6, 0.00, '2025-12-23 19:12:14'),
	(6, 7, 0.00, '2025-12-25 13:48:47'),
	(7, 9, 0.00, '2026-01-04 20:16:11'),
	(8, 10, 80000.00, '2026-01-04 20:43:52');

/*!40103 SET TIME_ZONE=IFNULL(@OLD_TIME_ZONE, 'system') */;
/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IFNULL(@OLD_FOREIGN_KEY_CHECKS, 1) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40111 SET SQL_NOTES=IFNULL(@OLD_SQL_NOTES, 1) */;
