
-- Database initialization script.

CREATE DATABASE mysystem;

CREATE USER 'mysystem'@'localhost' IDENTIFIED BY 'mysystem';

GRANT ALL ON mysystem.* TO 'mysystem'@'localhost';

