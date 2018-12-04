-- drop database if exists example_docker_db;
-- create database example_docker_db;
use example_db;

--
-- Table structure for table `example_table`
--

DROP TABLE IF EXISTS `example_table`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `example_table` (
  `id`        bigint(20)   NOT NULL,
  `INS_DATE`  datetime     NOT NULL,
  `NAME`      varchar(255) NOT NULL,
  `VALUE`     varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `EXAMPLE_TABLE`
--

LOCK TABLES `example_table` WRITE;
/*!40000 ALTER TABLE `example_table` DISABLE KEYS */;
INSERT INTO `example_table` (id, INS_DATE, NAME, VALUE)
VALUES
( 1, now(), 'example-1', 'value-1'), 
( 2, now(), 'example-2', 'value-2'), 
( 3, now(), 'example-3', 'value-3'), 
( 4, now(), 'example-4', 'value-4'), 
( 5, now(), 'example-5', 'value-5'), 
( 6, now(), 'example-6', 'value-6'), 
( 7, now(), 'example-7', 'value-7'), 
( 8, now(), 'example-8', 'value-8'), 
( 9, now(), 'example-9', 'value-9');
/*!40000 ALTER TABLE `example_table` ENABLE KEYS */;
UNLOCK TABLES;
