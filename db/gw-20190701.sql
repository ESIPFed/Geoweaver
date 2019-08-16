-- --------------------------------------------------------
-- Host:                         127.0.0.1
-- Server version:               5.5.60 - MySQL Community Server (GPL)
-- Server OS:                    Win64
-- HeidiSQL version:             7.0.0.4053
-- Date/time:                    2019-07-01 15:46:25
-- --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!40014 SET FOREIGN_KEY_CHECKS=0 */;

-- Dumping database structure for cyberconnector
DROP DATABASE IF EXISTS `cyberconnector`;
CREATE DATABASE IF NOT EXISTS `cyberconnector` /*!40100 DEFAULT CHARACTER SET latin1 */;
USE `cyberconnector`;


-- Dumping structure for table cyberconnector.abstract_model
DROP TABLE IF EXISTS `abstract_model`;
CREATE TABLE IF NOT EXISTS `abstract_model` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `identifier` varchar(50) NOT NULL,
  `name` varchar(50) NOT NULL,
  `namespace` varchar(150) NOT NULL,
  `process_connection` text NOT NULL,
  `param_connection` text,
  `keywords` tinytext,
  `description` tinytext,
  `begin_date` date DEFAULT '2013-09-11',
  `end_date` date DEFAULT '2013-09-11',
  `ecsdisciplinekeyword` varchar(50) DEFAULT 'N/A',
  `ecstopickeyword` varchar(50) DEFAULT 'N/A',
  `ecsparameterkeyword` varchar(50) DEFAULT 'N/A',
  `ecsvariablekeyword` varchar(50) DEFAULT 'N/A',
  `ecstermkeyword` varchar(50) DEFAULT 'N/A',
  `suported_format` varchar(50) DEFAULT 'image/png',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='This table stores the processing models of VDPs.';

-- Data exporting was unselected.


-- Dumping structure for table cyberconnector.association
DROP TABLE IF EXISTS `association`;
CREATE TABLE IF NOT EXISTS `association` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `process_type_id` int(10) NOT NULL,
  `service_id` int(10) NOT NULL,
  `operation_name` varchar(50) NOT NULL,
  `inputs_pathes` text,
  `output_path` text,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='This table stores the links between process and services.';

-- Data exporting was unselected.


-- Dumping structure for table cyberconnector.datasets
DROP TABLE IF EXISTS `datasets`;
CREATE TABLE IF NOT EXISTS `datasets` (
  `tid` int(11) NOT NULL AUTO_INCREMENT,
  `identifier` varchar(48) NOT NULL DEFAULT '',
  `name` text,
  `description` text,
  `history` text,
  `processingLevel` varchar(64) DEFAULT NULL,
  `rights` text,
  `creationDate` date DEFAULT NULL,
  `publicationDate` date DEFAULT NULL,
  `keyword` varchar(255) DEFAULT NULL,
  `beginTemporal` datetime DEFAULT NULL,
  `endTemporal` datetime DEFAULT NULL,
  `referenceSystemCode` varchar(48) DEFAULT 'EPSG:4326',
  `northBoundLatitude` double(20,6) DEFAULT '90.000000',
  `westBoundLongitude` double(20,6) DEFAULT '-180.000000',
  `eastBoundLongitude` double(20,6) DEFAULT '180.000000',
  `southBoundLatitude` double(20,6) DEFAULT '-90.000000',
  `Format` varchar(64) DEFAULT NULL,
  `sizeMB` varchar(64) DEFAULT NULL,
  `dataType` varchar(64) DEFAULT NULL,
  `featureType` varchar(64) DEFAULT NULL,
  `dataURL` text,
  `serviceURL` text,
  `sourceImage` int(11) DEFAULT NULL,
  `anytext` text,
  PRIMARY KEY (`tid`),
  KEY `identifier` (`identifier`),
  KEY `name` (`name`(255)),
  KEY `description` (`description`(255)),
  KEY `keyword` (`keyword`),
  KEY `dataType` (`dataType`),
  KEY `featureType` (`featureType`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='This tables store the metadata of individual dataset.';

-- Data exporting was unselected.


-- Dumping structure for table cyberconnector.environment
DROP TABLE IF EXISTS `environment`;
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


-- Dumping structure for table cyberconnector.history
DROP TABLE IF EXISTS `history`;
CREATE TABLE IF NOT EXISTS `history` (
  `id` varchar(20) NOT NULL,
  `process` varchar(50) NOT NULL,
  `begin_time` datetime NOT NULL,
  `end_time` datetime DEFAULT NULL,
  `input` longtext,
  `output` longtext,
  `host` text,
  `status` varchar(50) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Data exporting was unselected.


-- Dumping structure for table cyberconnector.hosts
DROP TABLE IF EXISTS `hosts`;
CREATE TABLE IF NOT EXISTS `hosts` (
  `id` varchar(50) NOT NULL,
  `name` varchar(50) NOT NULL,
  `ip` varchar(50) NOT NULL,
  `port` smallint(6) NOT NULL,
  `user` varchar(50) NOT NULL,
  `owner` varchar(50) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='Host managed by CyberConnector';

-- Data exporting was unselected.


-- Dumping structure for table cyberconnector.orders
DROP TABLE IF EXISTS `orders`;
CREATE TABLE IF NOT EXISTS `orders` (
  `orderid` varchar(50) NOT NULL,
  `product` varchar(50) NOT NULL,
  `ordertime` datetime NOT NULL,
  `updatetime` datetime NOT NULL,
  `project` varchar(50) DEFAULT NULL,
  `userid` int(10) DEFAULT NULL,
  `east` double(20,6) DEFAULT NULL,
  `south` double(20,6) DEFAULT NULL,
  `west` double(20,6) DEFAULT NULL,
  `north` double(20,6) DEFAULT NULL,
  `email` tinytext NOT NULL,
  `begintime` datetime DEFAULT NULL,
  `endtime` datetime DEFAULT NULL,
  `status` enum('Running','Ready','Done','Failed') NOT NULL,
  `message` text NOT NULL,
  `parametermap` text,
  UNIQUE KEY `orderid` (`orderid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='This table stores the user orders.';

-- Data exporting was unselected.


-- Dumping structure for table cyberconnector.process_type
DROP TABLE IF EXISTS `process_type`;
CREATE TABLE IF NOT EXISTS `process_type` (
  `id` varchar(50) NOT NULL,
  `name` varchar(50) NOT NULL,
  `code` longtext NOT NULL,
  `description` text,
  `inputs` text,
  `inputs_datatypes` varchar(200) DEFAULT NULL,
  `output` text,
  `output_datatype` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='This table stores all the usable logic process for modeling.';

-- Data exporting was unselected.


-- Dumping structure for table cyberconnector.products
DROP TABLE IF EXISTS `products`;
CREATE TABLE IF NOT EXISTS `products` (
  `identifier` varchar(50) NOT NULL DEFAULT '',
  `abbreviation` varchar(50) DEFAULT NULL,
  `description` tinytext,
  `keywords` tinytext,
  `name` varchar(100) NOT NULL,
  `east` double(20,6) DEFAULT NULL,
  `south` double(20,6) DEFAULT NULL,
  `west` double(20,6) DEFAULT NULL,
  `north` double(20,6) DEFAULT NULL,
  `srs` varchar(50) DEFAULT 'EPSG:4326',
  `begintime` date DEFAULT '1900-01-01',
  `endtime` date DEFAULT NULL,
  `ifvirtual` char(1) NOT NULL DEFAULT '0',
  `likes` tinyint(4) DEFAULT '0',
  `parent_abstract_model` varchar(50) DEFAULT NULL,
  `dataFormat` varchar(50) DEFAULT NULL,
  `accessURL` tinytext,
  `ontology_reference` tinytext,
  `lastUpdateDate` date DEFAULT NULL,
  `userid` int(10) DEFAULT NULL,
  `isspatial` char(1) DEFAULT NULL,
  PRIMARY KEY (`identifier`),
  UNIQUE KEY `identifier` (`identifier`),
  UNIQUE KEY `name` (`name`),
  KEY `ifvirtual` (`ifvirtual`),
  KEY `parent_abstract_model` (`parent_abstract_model`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='This table archives the metadata of VDPs.';

-- Data exporting was unselected.


-- Dumping structure for table cyberconnector.requirements
DROP TABLE IF EXISTS `requirements`;
CREATE TABLE IF NOT EXISTS `requirements` (
  `productid` varchar(50) NOT NULL,
  `format` varchar(50) NOT NULL,
  `modelInput` varchar(50) NOT NULL,
  `type` enum('BoundingBox','TimeRange','TimeStamp','Projection','InitialDataURL','OutputFormat','SpatialPoint','SpatialPolygon','Unknown') NOT NULL,
  `constraints` varchar(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='This table is unused.';

-- Data exporting was unselected.


-- Dumping structure for table cyberconnector.service
DROP TABLE IF EXISTS `service`;
CREATE TABLE IF NOT EXISTS `service` (
  `tid` int(11) NOT NULL AUTO_INCREMENT,
  `id` varchar(48) NOT NULL,
  `home` varchar(255) DEFAULT NULL,
  `name` text,
  `description` text,
  `status` varchar(16) DEFAULT NULL,
  `registerdate` datetime DEFAULT NULL,
  `expiration` datetime DEFAULT NULL,
  `majorVersion` int(16) NOT NULL DEFAULT '1',
  `minorVersion` int(16) NOT NULL DEFAULT '0',
  `userVersion` varchar(64) DEFAULT NULL,
  `keywords` varchar(256) DEFAULT NULL,
  `serviceType` varchar(64) DEFAULT NULL,
  `accessURL` varchar(256) DEFAULT NULL,
  `wsdlURL` varchar(256) DEFAULT NULL,
  `userid` int(10) DEFAULT NULL,
  PRIMARY KEY (`tid`),
  KEY `id` (`id`),
  KEY `name` (`name`(64)),
  KEY `description` (`description`(255)),
  KEY `keywords` (`keywords`),
  KEY `serviceType` (`serviceType`),
  KEY `accessURL` (`accessURL`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='This table stores the metadata of physical web services.';

-- Data exporting was unselected.


-- Dumping structure for table cyberconnector.users
DROP TABLE IF EXISTS `users`;
CREATE TABLE IF NOT EXISTS `users` (
  `uid` int(10) NOT NULL AUTO_INCREMENT,
  `name` varchar(20) NOT NULL,
  `pswd` text NOT NULL,
  `type` varchar(15) DEFAULT NULL,
  `address` tinytext NOT NULL,
  `fullname` varchar(50) DEFAULT NULL,
  `sex` varchar(6) DEFAULT NULL,
  `last_login_time` timestamp NULL DEFAULT NULL,
  `reg_time` timestamp NULL DEFAULT NULL,
  `last_operate_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `status` varchar(8) DEFAULT NULL,
  `token` varchar(50) DEFAULT NULL,
  `email` varchar(50) DEFAULT NULL,
  `phone` varchar(50) DEFAULT NULL,
  `department` varchar(50) DEFAULT NULL,
  `institute` varchar(50) DEFAULT NULL,
  `last_ip` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`uid`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Data exporting was unselected.
/*!40014 SET FOREIGN_KEY_CHECKS=1 */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
