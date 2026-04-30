CREATE DATABASE  IF NOT EXISTS `walletpet` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `walletpet`;
-- MySQL dump 10.13  Distrib 8.0.44, for Win64 (x86_64)
--
-- Host: localhost    Database: walletpet
-- ------------------------------------------------------
-- Server version	8.0.44

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `account_transactions`
--

DROP TABLE IF EXISTS `account_transactions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `account_transactions` (
  `account_trans_id` int NOT NULL AUTO_INCREMENT,
  `user_id` varchar(50) NOT NULL,
  `from_account_id` int NOT NULL,
  `to_account_id` int NOT NULL,
  `transaction_amount` decimal(12,2) NOT NULL,
  `note` varchar(255) DEFAULT NULL,
  `transaction_date` date NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`account_trans_id`),
  KEY `idx_account_trans_user_id` (`user_id`),
  KEY `idx_account_trans_from_account_id` (`from_account_id`),
  KEY `idx_account_trans_to_account_id` (`to_account_id`),
  CONSTRAINT `fk_account_trans_from` FOREIGN KEY (`from_account_id`) REFERENCES `accounts` (`account_id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_account_trans_to` FOREIGN KEY (`to_account_id`) REFERENCES `accounts` (`account_id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_account_trans_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `account_transactions`
--

LOCK TABLES `account_transactions` WRITE;
/*!40000 ALTER TABLE `account_transactions` DISABLE KEYS */;
/*!40000 ALTER TABLE `account_transactions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `accounts`
--

DROP TABLE IF EXISTS `accounts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `accounts` (
  `account_id` int NOT NULL AUTO_INCREMENT,
  `user_id` varchar(50) NOT NULL,
  `account_name` varchar(50) NOT NULL,
  `balance` decimal(12,2) NOT NULL DEFAULT '0.00',
  `is_liability` tinyint(1) NOT NULL DEFAULT '0',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  `is_saving_account` tinyint(1) NOT NULL DEFAULT '0',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`account_id`),
  KEY `idx_accounts_user_id` (`user_id`),
  KEY `idx_accounts_is_saving_account` (`is_saving_account`),
  CONSTRAINT `fk_accounts_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `accounts`
--

LOCK TABLES `accounts` WRITE;
/*!40000 ALTER TABLE `accounts` DISABLE KEYS */;
INSERT INTO `accounts` VALUES (1,'default','測試現金帳戶',12700.00,0,0,0,'2026-04-25 15:35:53');
/*!40000 ALTER TABLE `accounts` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `budget`
--

DROP TABLE IF EXISTS `budget`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `budget` (
  `budget_id` varchar(50) NOT NULL,
  `user_id` varchar(50) NOT NULL,
  `budget_scope` varchar(20) NOT NULL,
  `category_id` varchar(50) DEFAULT NULL,
  `target_type` varchar(20) NOT NULL,
  `budget_amount` decimal(12,2) NOT NULL,
  `start_date` date NOT NULL,
  `end_date` date NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`budget_id`),
  KEY `idx_budget_user_id` (`user_id`),
  KEY `idx_budget_category_id` (`category_id`),
  KEY `idx_budget_period` (`start_date`,`end_date`),
  CONSTRAINT `fk_budget_category` FOREIGN KEY (`category_id`) REFERENCES `categories` (`category_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_budget_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `budget`
--

LOCK TABLES `budget` WRITE;
/*!40000 ALTER TABLE `budget` DISABLE KEYS */;
/*!40000 ALTER TABLE `budget` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `categories`
--

DROP TABLE IF EXISTS `categories`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `categories` (
  `category_id` varchar(50) NOT NULL,
  `user_id` varchar(50) NOT NULL,
  `category_name` varchar(50) NOT NULL,
  `category_type` varchar(20) NOT NULL,
  `icon` varchar(50) NOT NULL DEFAULT 'default',
  `color` varchar(30) DEFAULT NULL,
  `is_system` tinyint(1) NOT NULL DEFAULT '0',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `is_disable` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`category_id`),
  KEY `idx_categories_user_id` (`user_id`),
  KEY `idx_categories_type` (`category_type`),
  CONSTRAINT `fk_categories_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `categories`
--

LOCK TABLES `categories` WRITE;
/*!40000 ALTER TABLE `categories` DISABLE KEYS */;
<<<<<<< HEAD
INSERT INTO `categories` VALUES ('ES001','default','飲食','EXPENSE','wait','#FF9F43',1,'2026-04-16 11:53:24',0),('ES002','default','娛樂','EXPENSE','wait','#A29BFE',1,'2026-04-16 11:53:24',0),('IS001','default','薪水','INCOME','wait','#2ECC71',1,'2026-04-16 11:53:24',0),('IS002','default','投資','INCOME','wait','#3498DB',1,'2026-04-16 11:53:24',0);
=======
INSERT INTO `categories` VALUES ('CAT202604251443398cdfe68f','default','咖啡飲料','EXPENSE','coffee','#6D4C41',0,'2026-04-25 14:43:39',0),('ES001','default','飲食','EXPENSE','wait','#FF9F43',1,'2026-04-16 11:53:24',0),('ES002','default','娛樂','EXPENSE','wait','#A29BFE',1,'2026-04-16 11:53:24',0),('IS001','default','薪水','INCOME','wait','#2ECC71',1,'2026-04-16 11:53:24',0),('IS002','default','投資','INCOME','wait','#3498DB',1,'2026-04-16 11:53:24',0);
>>>>>>> tzuchen
/*!40000 ALTER TABLE `categories` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `daily_record_rewards`
--

DROP TABLE IF EXISTS `daily_record_rewards`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `daily_record_rewards` (
  `daily_reward_id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` varchar(50) NOT NULL,
  `reward_date` date NOT NULL,
  `qualified` tinyint(1) NOT NULL DEFAULT '0',
  `transaction_count` int NOT NULL DEFAULT '0',
  `streak_days` int NOT NULL DEFAULT '0',
  `reward_type` varchar(50) DEFAULT NULL,
  `reward_value` int DEFAULT NULL,
  `mood_delta` int NOT NULL DEFAULT '0',
<<<<<<< HEAD
  `cancan_delta` int NOT NULL DEFAULT '0',
  `claimed_at` datetime DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
=======
  `claimed_at` datetime DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
>>>>>>> tzuchen
  PRIMARY KEY (`daily_reward_id`),
  UNIQUE KEY `uk_daily_reward_user_date` (`user_id`,`reward_date`),
  KEY `idx_daily_reward_user_id` (`user_id`),
  KEY `idx_daily_reward_reward_date` (`reward_date`),
  CONSTRAINT `fk_daily_reward_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
<<<<<<< HEAD
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
=======
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
>>>>>>> tzuchen
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `daily_record_rewards`
--

LOCK TABLES `daily_record_rewards` WRITE;
/*!40000 ALTER TABLE `daily_record_rewards` DISABLE KEYS */;
<<<<<<< HEAD
=======
INSERT INTO `daily_record_rewards` VALUES (1,'default','2026-04-25',1,2,1,'MOOD_BONUS',5,5,'2026-04-25 16:13:03','2026-04-25 16:13:03','2026-04-25 16:13:03');
>>>>>>> tzuchen
/*!40000 ALTER TABLE `daily_record_rewards` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `pet_events`
--

DROP TABLE IF EXISTS `pet_events`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pet_events` (
  `pet_event_id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` varchar(50) NOT NULL,
  `pet_id` varchar(50) NOT NULL,
  `event_type` varchar(50) NOT NULL,
  `mood_delta` int DEFAULT NULL,
<<<<<<< HEAD
  `cancan_delta` int NOT NULL DEFAULT '0',
=======
>>>>>>> tzuchen
  `reward` varchar(50) DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`pet_event_id`),
  KEY `idx_pet_events_user_id` (`user_id`),
  KEY `idx_pet_events_pet_id` (`pet_id`),
  CONSTRAINT `fk_pet_events_pet` FOREIGN KEY (`pet_id`) REFERENCES `pets` (`pet_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_pet_events_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `pet_events`
--

LOCK TABLES `pet_events` WRITE;
/*!40000 ALTER TABLE `pet_events` DISABLE KEYS */;
/*!40000 ALTER TABLE `pet_events` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `pet_model`
--

DROP TABLE IF EXISTS `pet_model`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pet_model` (
  `petmodel_id` int NOT NULL AUTO_INCREMENT,
  `rive_name` varchar(45) NOT NULL,
  `description` varchar(225) NOT NULL,
  PRIMARY KEY (`petmodel_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `pet_model`
--

LOCK TABLES `pet_model` WRITE;
/*!40000 ALTER TABLE `pet_model` DISABLE KEYS */;
/*!40000 ALTER TABLE `pet_model` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `pets`
--

DROP TABLE IF EXISTS `pets`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pets` (
  `pet_id` varchar(50) NOT NULL,
  `user_id` varchar(50) NOT NULL,
  `pet_name` varchar(45) NOT NULL,
  `mood` int NOT NULL DEFAULT '60',
  `cancan` int NOT NULL DEFAULT '0',
  `last_update_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `is_displayed` tinyint(1) NOT NULL DEFAULT '1',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `model_id` int NOT NULL,
  PRIMARY KEY (`pet_id`),
  KEY `idx_pets_user_id` (`user_id`),
  KEY `fk_pets_model_idx` (`model_id`),
  CONSTRAINT `fk_pets_model` FOREIGN KEY (`model_id`) REFERENCES `pet_model` (`petmodel_id`),
  CONSTRAINT `fk_pets_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `pets`
--

LOCK TABLES `pets` WRITE;
/*!40000 ALTER TABLE `pets` DISABLE KEYS */;
/*!40000 ALTER TABLE `pets` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `saving_goals`
--

DROP TABLE IF EXISTS `saving_goals`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `saving_goals` (
  `saving_goal_id` varchar(50) NOT NULL,
  `goal_name` varchar(100) NOT NULL,
  `target_amount` decimal(12,2) NOT NULL,
  `final_account_name` varchar(100) DEFAULT NULL,
  `final_amount` decimal(12,2) DEFAULT NULL,
  `start_date` date NOT NULL,
  `end_date` date NOT NULL,
  `user_id` varchar(50) NOT NULL,
  `account_id` int NOT NULL,
  `status` varchar(50) NOT NULL DEFAULT 'ACTIVE',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`saving_goal_id`),
  UNIQUE KEY `uk_saving_goals_account_id` (`account_id`),
  KEY `idx_saving_goals_user_id` (`user_id`),
  CONSTRAINT `fk_saving_goals_account` FOREIGN KEY (`account_id`) REFERENCES `accounts` (`account_id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_saving_goals_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `saving_goals`
--

LOCK TABLES `saving_goals` WRITE;
/*!40000 ALTER TABLE `saving_goals` DISABLE KEYS */;
/*!40000 ALTER TABLE `saving_goals` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `transactions`
--

DROP TABLE IF EXISTS `transactions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `transactions` (
  `transaction_id` varchar(50) NOT NULL,
  `user_id` varchar(50) NOT NULL,
  `account_id` int NOT NULL,
  `category_id` varchar(50) NOT NULL,
  `transaction_amount` decimal(12,2) NOT NULL,
  `transaction_type` varchar(20) NOT NULL,
  `note` varchar(255) DEFAULT NULL,
  `transaction_date` date NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`transaction_id`),
  KEY `idx_transactions_user_id` (`user_id`),
  KEY `idx_transactions_account_id` (`account_id`),
  KEY `idx_transactions_category_id` (`category_id`),
  KEY `idx_transactions_date` (`transaction_date`),
  CONSTRAINT `fk_transactions_account` FOREIGN KEY (`account_id`) REFERENCES `accounts` (`account_id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_transactions_category` FOREIGN KEY (`category_id`) REFERENCES `categories` (`category_id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_transactions_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `transactions`
--

LOCK TABLES `transactions` WRITE;
/*!40000 ALTER TABLE `transactions` DISABLE KEYS */;
INSERT INTO `transactions` VALUES ('TXN202604251539062fd19503','default',1,'IS001',2000.00,'INCOME','薪資','2026-04-25','2026-04-25 15:39:06'),('TXN20260425161152db8f90b0','default',1,'ES002',300.00,'EXPENSE','午餐','2026-04-25','2026-04-25 16:11:52'),('TXN2026042516134427547d43','default',1,'IS001',1000.00,'INCOME','測試收入','2026-04-25','2026-04-25 16:13:44');
/*!40000 ALTER TABLE `transactions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_login_logs`
--

DROP TABLE IF EXISTS `user_login_logs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_login_logs` (
  `login_log_id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` varchar(50) NOT NULL,
  `login_date` date NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`login_log_id`),
  UNIQUE KEY `uk_user_login_logs_user_date` (`user_id`,`login_date`),
  KEY `idx_user_login_logs_user_id` (`user_id`),
  KEY `idx_user_login_logs_login_date` (`login_date`),
  CONSTRAINT `fk_user_login_logs_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_login_logs`
--

LOCK TABLES `user_login_logs` WRITE;
/*!40000 ALTER TABLE `user_login_logs` DISABLE KEYS */;
/*!40000 ALTER TABLE `user_login_logs` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `user_id` varchar(50) NOT NULL,
  `user_name` varchar(50) NOT NULL,
  `password` varchar(255) NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `role` varchar(50) NOT NULL DEFAULT 'USER',
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `uk_users_user_name` (`user_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES ('default','system','0000','2026-04-13 00:00:00','USER');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

<<<<<<< HEAD
-- Dump completed on 2026-04-24 14:56:32
=======
-- Dump completed on 2026-04-25 21:21:07
>>>>>>> tzuchen
