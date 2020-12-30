-- --------------------------------------------------------
-- Host:                         127.0.0.1
-- Server version:               5.5.60 - MySQL Community Server (GPL)
-- Server OS:                    Win64
-- HeidiSQL version:             7.0.0.4053
-- Date/time:                    2019-08-16 16:37:35
-- --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!40014 SET FOREIGN_KEY_CHECKS=0 */;

-- Dumping structure for table cyberconnector.abstract_model
DROP TABLE IF EXISTS `abstract_model`;
CREATE TABLE IF NOT EXISTS `abstract_model` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `identifier` varchar(50) CHARACTER SET latin1 NOT NULL,
  `name` varchar(50) CHARACTER SET latin1 NOT NULL,
  `namespace` varchar(150) CHARACTER SET latin1 NOT NULL,
  `process_connection` text CHARACTER SET latin1 NOT NULL,
  `param_connection` text CHARACTER SET latin1,
  `keywords` tinytext CHARACTER SET latin1,
  `description` tinytext CHARACTER SET latin1,
  `begin_date` date DEFAULT '2013-09-11',
  `end_date` date DEFAULT '2013-09-11',
  `ecsdisciplinekeyword` varchar(50) CHARACTER SET latin1 DEFAULT 'N/A',
  `ecstopickeyword` varchar(50) CHARACTER SET latin1 DEFAULT 'N/A',
  `ecsparameterkeyword` varchar(50) CHARACTER SET latin1 DEFAULT 'N/A',
  `ecsvariablekeyword` varchar(50) CHARACTER SET latin1 DEFAULT 'N/A',
  `ecstermkeyword` varchar(50) CHARACTER SET latin1 DEFAULT 'N/A',
  `suported_format` varchar(50) CHARACTER SET latin1 DEFAULT 'image/png',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=76 DEFAULT CHARSET=utf8 COMMENT='This table stores the processing models of VDPs.';


-- Dumping structure for table cyberconnector.association
DROP TABLE IF EXISTS `association`;
CREATE TABLE IF NOT EXISTS `association` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `process_type_id` int(10) NOT NULL,
  `service_id` int(10) NOT NULL,
  `operation_name` varchar(50) CHARACTER SET latin1 NOT NULL,
  `inputs_pathes` text CHARACTER SET latin1,
  `output_path` text CHARACTER SET latin1,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=777 DEFAULT CHARSET=utf8 COMMENT='This table stores the links between process and services.';




-- Dumping structure for table cyberconnector.datasets
DROP TABLE IF EXISTS `datasets`;
CREATE TABLE IF NOT EXISTS `datasets` (
  `tid` int(11) NOT NULL AUTO_INCREMENT,
  `identifier` varchar(48) CHARACTER SET latin1 NOT NULL DEFAULT '',
  `name` text CHARACTER SET latin1,
  `description` text CHARACTER SET latin1,
  `history` text CHARACTER SET latin1,
  `processingLevel` varchar(64) CHARACTER SET latin1 DEFAULT NULL,
  `rights` text CHARACTER SET latin1,
  `creationDate` date DEFAULT NULL,
  `publicationDate` date DEFAULT NULL,
  `keyword` varchar(255) CHARACTER SET latin1 DEFAULT NULL,
  `beginTemporal` datetime DEFAULT NULL,
  `endTemporal` datetime DEFAULT NULL,
  `referenceSystemCode` varchar(48) CHARACTER SET latin1 DEFAULT 'EPSG:4326',
  `northBoundLatitude` double(20,6) DEFAULT '90.000000',
  `westBoundLongitude` double(20,6) DEFAULT '-180.000000',
  `eastBoundLongitude` double(20,6) DEFAULT '180.000000',
  `southBoundLatitude` double(20,6) DEFAULT '-90.000000',
  `Format` varchar(64) CHARACTER SET latin1 DEFAULT NULL,
  `sizeMB` varchar(64) CHARACTER SET latin1 DEFAULT NULL,
  `dataType` varchar(64) CHARACTER SET latin1 DEFAULT NULL,
  `featureType` varchar(64) CHARACTER SET latin1 DEFAULT NULL,
  `dataURL` text CHARACTER SET latin1,
  `serviceURL` text CHARACTER SET latin1,
  `sourceImage` int(11) DEFAULT NULL,
  `anytext` text CHARACTER SET latin1,
  PRIMARY KEY (`tid`),
  KEY `identifier` (`identifier`),
  KEY `name` (`name`(255)),
  KEY `description` (`description`(255)),
  KEY `keyword` (`keyword`),
  KEY `dataType` (`dataType`),
  KEY `featureType` (`featureType`)
) ENGINE=InnoDB AUTO_INCREMENT=42 DEFAULT CHARSET=utf8 COMMENT='This tables store the metadata of individual dataset.';



-- Dumping structure for table cyberconnector.environment
DROP TABLE IF EXISTS `environment`;
CREATE TABLE IF NOT EXISTS `environment` (
  `id` varchar(50) CHARACTER SET latin1 NOT NULL,
  `name` varchar(50) CHARACTER SET latin1 DEFAULT NULL,
  `type` varchar(50) CHARACTER SET latin1 NOT NULL,
  `bin` tinytext CHARACTER SET latin1 NOT NULL,
  `pyenv` varchar(50) CHARACTER SET latin1 DEFAULT NULL,
  `host` varchar(50) CHARACTER SET latin1 NOT NULL,
  `basedir` tinytext CHARACTER SET latin1,
  `settings` tinytext CHARACTER SET latin1,
  KEY `id` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='The running environment on a host, for example, python environment. ';

-- Dumping structure for table cyberconnector.history
DROP TABLE IF EXISTS `history`;
CREATE TABLE IF NOT EXISTS `history` (
  `id` varchar(20) CHARACTER SET latin1 NOT NULL,
  `process` varchar(50) CHARACTER SET latin1 NOT NULL,
  `begin_time` datetime NOT NULL,
  `end_time` datetime DEFAULT NULL,
  `input` longtext CHARACTER SET latin1,
  `output` longtext CHARACTER SET latin1,
  `host` text CHARACTER SET latin1,
  `indicator` varchar(50) CHARACTER SET latin1 DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


-- Dumping structure for table cyberconnector.hosts
DROP TABLE IF EXISTS `hosts`;
CREATE TABLE IF NOT EXISTS `hosts` (
  `id` varchar(50) CHARACTER SET latin1 NOT NULL,
  `name` varchar(50) CHARACTER SET latin1 NOT NULL,
  `ip` varchar(50) CHARACTER SET latin1 NOT NULL,
  `port` smallint(6) NOT NULL,
  `user` varchar(50) CHARACTER SET latin1 NOT NULL,
  `owner` varchar(50) CHARACTER SET latin1 DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='Host managed by CyberConnector';


-- Dumping structure for table cyberconnector.orders
DROP TABLE IF EXISTS `orders`;
CREATE TABLE IF NOT EXISTS `orders` (
  `orderid` varchar(50) CHARACTER SET latin1 NOT NULL,
  `product` varchar(50) CHARACTER SET latin1 NOT NULL,
  `ordertime` datetime NOT NULL,
  `updatetime` datetime NOT NULL,
  `project` varchar(50) CHARACTER SET latin1 DEFAULT NULL,
  `userid` int(10) DEFAULT NULL,
  `east` double(20,6) DEFAULT NULL,
  `south` double(20,6) DEFAULT NULL,
  `west` double(20,6) DEFAULT NULL,
  `north` double(20,6) DEFAULT NULL,
  `email` tinytext CHARACTER SET latin1 NOT NULL,
  `begintime` datetime DEFAULT NULL,
  `endtime` datetime DEFAULT NULL,
  `status` enum('Running','Ready','Done','Failed') CHARACTER SET latin1 NOT NULL,
  `message` text CHARACTER SET latin1 NOT NULL,
  `parametermap` text CHARACTER SET latin1,
  UNIQUE KEY `orderid` (`orderid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='This table stores the user orders.';


-- Dumping structure for table cyberconnector.process_type
DROP TABLE IF EXISTS `process_type`;
CREATE TABLE IF NOT EXISTS `process_type` (
  `id` varchar(50) CHARACTER SET latin1 NOT NULL,
  `name` varchar(50) CHARACTER SET latin1 NOT NULL,
  `code` longtext CHARACTER SET latin1 NOT NULL,
  `description` text CHARACTER SET latin1,
  `inputs` text CHARACTER SET latin1,
  `inputs_datatypes` varchar(200) CHARACTER SET latin1 DEFAULT NULL,
  `output` text CHARACTER SET latin1,
  `output_datatype` varchar(100) CHARACTER SET latin1 DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='This table stores all the usable logic process for modeling.';


-- Dumping structure for table cyberconnector.products
DROP TABLE IF EXISTS `products`;
CREATE TABLE IF NOT EXISTS `products` (
  `identifier` varchar(50) CHARACTER SET latin1 NOT NULL DEFAULT '',
  `abbreviation` varchar(50) CHARACTER SET latin1 DEFAULT NULL,
  `description` tinytext CHARACTER SET latin1,
  `keywords` tinytext CHARACTER SET latin1,
  `name` varchar(100) CHARACTER SET latin1 NOT NULL,
  `east` double(20,6) DEFAULT NULL,
  `south` double(20,6) DEFAULT NULL,
  `west` double(20,6) DEFAULT NULL,
  `north` double(20,6) DEFAULT NULL,
  `srs` varchar(50) CHARACTER SET latin1 DEFAULT 'EPSG:4326',
  `begintime` date DEFAULT '1900-01-01',
  `endtime` date DEFAULT NULL,
  `ifvirtual` char(1) CHARACTER SET latin1 NOT NULL DEFAULT '0',
  `likes` tinyint(4) DEFAULT '0',
  `parent_abstract_model` varchar(50) CHARACTER SET latin1 DEFAULT NULL,
  `dataFormat` varchar(50) CHARACTER SET latin1 DEFAULT NULL,
  `accessURL` tinytext CHARACTER SET latin1,
  `ontology_reference` tinytext CHARACTER SET latin1,
  `lastUpdateDate` date DEFAULT NULL,
  `userid` int(10) DEFAULT NULL,
  `isspatial` char(1) CHARACTER SET latin1 DEFAULT NULL,
  PRIMARY KEY (`identifier`),
  UNIQUE KEY `identifier` (`identifier`),
  UNIQUE KEY `name` (`name`),
  KEY `ifvirtual` (`ifvirtual`),
  KEY `parent_abstract_model` (`parent_abstract_model`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='This table archives the metadata of VDPs.';



-- Dumping structure for table cyberconnector.requirements
DROP TABLE IF EXISTS `requirements`;
CREATE TABLE IF NOT EXISTS `requirements` (
  `productid` varchar(50) CHARACTER SET latin1 NOT NULL,
  `format` varchar(50) CHARACTER SET latin1 NOT NULL,
  `modelInput` varchar(50) CHARACTER SET latin1 NOT NULL,
  `type` enum('BoundingBox','TimeRange','TimeStamp','Projection','InitialDataURL','OutputFormat','SpatialPoint','SpatialPolygon','Unknown') CHARACTER SET latin1 NOT NULL,
  `constraints` varchar(50) CHARACTER SET latin1 NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='This table is unused.';


-- Dumping structure for table cyberconnector.service
DROP TABLE IF EXISTS `service`;
CREATE TABLE IF NOT EXISTS `service` (
  `tid` int(11) NOT NULL AUTO_INCREMENT,
  `id` varchar(48) CHARACTER SET latin1 NOT NULL,
  `home` varchar(255) CHARACTER SET latin1 DEFAULT NULL,
  `name` text CHARACTER SET latin1,
  `description` text CHARACTER SET latin1,
  `status` varchar(16) CHARACTER SET latin1 DEFAULT NULL,
  `registerdate` datetime DEFAULT NULL,
  `expiration` datetime DEFAULT NULL,
  `majorVersion` int(16) NOT NULL DEFAULT '1',
  `minorVersion` int(16) NOT NULL DEFAULT '0',
  `userVersion` varchar(64) CHARACTER SET latin1 DEFAULT NULL,
  `keywords` varchar(256) CHARACTER SET latin1 DEFAULT NULL,
  `serviceType` varchar(64) CHARACTER SET latin1 DEFAULT NULL,
  `accessURL` varchar(256) CHARACTER SET latin1 DEFAULT NULL,
  `wsdlURL` varchar(256) CHARACTER SET latin1 DEFAULT NULL,
  `userid` int(10) DEFAULT NULL,
  PRIMARY KEY (`tid`),
  KEY `id` (`id`),
  KEY `name` (`name`(64)),
  KEY `description` (`description`(255)),
  KEY `keywords` (`keywords`),
  KEY `serviceType` (`serviceType`),
  KEY `accessURL` (`accessURL`)
) ENGINE=InnoDB AUTO_INCREMENT=178 DEFAULT CHARSET=utf8 COMMENT='This table stores the metadata of physical web services.';


-- Dumping structure for table cyberconnector.users
DROP TABLE IF EXISTS `users`;
CREATE TABLE IF NOT EXISTS `users` (
  `uid` int(10) NOT NULL AUTO_INCREMENT,
  `name` varchar(20) CHARACTER SET latin1 NOT NULL,
  `pswd` text CHARACTER SET latin1 NOT NULL,
  `type` varchar(15) CHARACTER SET latin1 DEFAULT NULL,
  `address` tinytext CHARACTER SET latin1 NOT NULL,
  `fullname` varchar(50) CHARACTER SET latin1 DEFAULT NULL,
  `sex` varchar(6) CHARACTER SET latin1 DEFAULT NULL,
  `last_login_time` timestamp NULL DEFAULT NULL,
  `reg_time` timestamp NULL DEFAULT NULL,
  `last_operate_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `status` varchar(8) CHARACTER SET latin1 DEFAULT NULL,
  `token` varchar(50) CHARACTER SET latin1 DEFAULT NULL,
  `email` varchar(50) CHARACTER SET latin1 DEFAULT NULL,
  `phone` varchar(50) CHARACTER SET latin1 DEFAULT NULL,
  `department` varchar(50) CHARACTER SET latin1 DEFAULT NULL,
  `institute` varchar(50) CHARACTER SET latin1 DEFAULT NULL,
  `last_ip` varchar(50) CHARACTER SET latin1 DEFAULT NULL,
  PRIMARY KEY (`uid`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8;
