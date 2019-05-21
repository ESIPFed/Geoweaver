-- --------------------------------------------------------
-- Host:                         127.0.0.1
-- Server version:               5.5.60 - MySQL Community Server (GPL)
-- Server OS:                    Win64
-- HeidiSQL version:             7.0.0.4053
-- Date/time:                    2019-05-20 15:12:14
-- --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!40014 SET FOREIGN_KEY_CHECKS=0 */;

-- Dumping database structure for cyberconnector
CREATE DATABASE IF NOT EXISTS `cyberconnector` /*!40100 DEFAULT CHARACTER SET latin1 */;
USE `cyberconnector`;


-- Dumping structure for table cyberconnector.environment
CREATE TABLE IF NOT EXISTS `environment` (
  `id` varchar(50) NOT NULL,
  `name` varchar(50) DEFAULT NULL,
  `type` varchar(50) NOT NULL,
  `bin` tinytext NOT NULL,
  `pyenv` varchar(50) DEFAULT NULL,
  `host` varchar(50) NOT NULL,
  `basedir` tinytext,
  `settings` tinytext,
  KEY `id` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='The running environment on a host, for example, python environment. ';

-- Data exporting was unselected.
/*!40014 SET FOREIGN_KEY_CHECKS=1 */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
