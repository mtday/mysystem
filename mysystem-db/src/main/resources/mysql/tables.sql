
DROP TABLE IF EXISTS companies;


CREATE TABLE IF NOT EXISTS companies (
    `id`             INTEGER      NOT NULL PRIMARY KEY AUTO_INCREMENT,
    `name`           VARCHAR(250) NOT NULL,
    `active`         BOOLEAN      NOT NULL DEFAULT TRUE,

    CONSTRAINT unique_company_name UNIQUE (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

