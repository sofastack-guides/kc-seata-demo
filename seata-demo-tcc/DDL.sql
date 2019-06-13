# ==========================
# balance db

DROP TABLE IF EXISTS `balance_tb`;
CREATE TABLE `balance_tb`
(
    `id`        int(11) NOT NULL AUTO_INCREMENT,
    `user_name` varchar(255)   DEFAULT NULL,
    `balance`   decimal(16, 2) DEFAULT 0.00,
    PRIMARY KEY (`id`),
    UNIQUE KEY (`user_name`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

# ==========================
# stock db

DROP TABLE IF EXISTS `stock_tb`;
CREATE TABLE `stock_tb`
(
    `id`           int(11) NOT NULL AUTO_INCREMENT,
    `product_code` varchar(255)   DEFAULT NULL,
    `name`         varchar(255)   DEFAULT NULL,
    `description`  text,
    `price`        decimal(10, 2) DEFAULT 0.00,
    `count`        int(11)        DEFAULT 0,
    `user_name`    varchar(255)   DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY (`id`, `user_name`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

DROP TABLE IF EXISTS `order_tb`;
CREATE TABLE `order_tb`
(
    `id`           int(11) NOT NULL AUTO_INCREMENT,
    `user_name`    varchar(255) DEFAULT NULL,
    `product_code` varchar(255) DEFAULT NULL,
    `count`        int(11)      DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;


# ==========================
# undo_log
CREATE TABLE `undo_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `branch_id` bigint(20) NOT NULL,
  `xid` varchar(100) NOT NULL,
  `rollback_info` longblob NOT NULL,
  `log_status` int(11) NOT NULL,
  `log_created` datetime NOT NULL,
  `log_modified` datetime NOT NULL,
  `ext` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `ux_undo_log` (`xid`,`branch_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

ALTER TABLE balance_tb add column freezed decimal(10,2) default 0.00;