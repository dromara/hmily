/*
SQLyog Ultimate v12.2.6 (64 bit)
MySQL - 5.7.19-0ubuntu0.16.04.1 : Database - account
*********************************************************************
*/
CREATE DATABASE `tcc` ;

CREATE DATABASE /*!32312 IF NOT EXISTS*/`tcc_account` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_bin */;

USE `tcc_account`;

/*Table structure for table `account` */

DROP TABLE IF EXISTS `account`;

CREATE TABLE `account` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` varchar(128) NOT NULL,
  `balance` decimal(10,0) NOT NULL COMMENT '用户余额',
  `freeze_amount` decimal(10,0) NOT NULL COMMENT '冻结金额，扣款暂存余额',
  `create_time` datetime NOT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

/*Data for the table `account` */

insert  into `account`(`id`,`user_id`,`balance`,`freeze_amount`,`create_time`,`update_time`) values

(1,'10000',10000,0,'2017-09-18 14:54:22',NULL);



CREATE DATABASE /*!32312 IF NOT EXISTS*/`tcc_stock` /*!40100 DEFAULT CHARACTER SET utf8mb4 */;

USE `tcc_stock`;

/*Table structure for table `inventory` */

DROP TABLE IF EXISTS `inventory`;

CREATE TABLE `inventory` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `product_id` VARCHAR(128) NOT NULL,
  `total_inventory` int(10) NOT NULL COMMENT '总库存',
  `lock_inventory` int(10) NOT NULL COMMENT '锁定库存',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

/*Data for the table `inventory` */

insert  into `inventory`(`id`,`product_id`,`total_inventory`,`lock_inventory`) values

(1,'1',1000,0);


CREATE DATABASE /*!32312 IF NOT EXISTS*/`tcc_order` /*!40100 DEFAULT CHARACTER SET utf8mb4 */;

USE `tcc_order`;
DROP TABLE IF EXISTS `order`;

CREATE TABLE `order` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `create_time` datetime NOT NULL,
  `number` varchar(20) COLLATE utf8mb4_bin NOT NULL,
  `status` tinyint(4) NOT NULL,
  `product_id` varchar(128) NOT NULL,
  `total_amount` decimal(10,0) NOT NULL,
  `count` int(4) NOT NULL,
  `user_id` varchar(128) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;



