-- Importer ce fichier dans phpMyAdmin (onglet Importer).
-- Pas de DROP : une base deja existante est conservee et CREATE DATABASE echoue.
CREATE DATABASE demo_java_mysql_db_explorer CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE demo_java_mysql_db_explorer;

CREATE TABLE T_Articles (
  IdArticle INT PRIMARY KEY AUTO_INCREMENT,
  Description VARCHAR(150) NOT NULL,
  Brand VARCHAR(80) NOT NULL,
  UnitaryPrice DECIMAL(10,2) NOT NULL
);
CREATE TABLE T_Users (
  IdUser INT PRIMARY KEY AUTO_INCREMENT,
  Login VARCHAR(80) NOT NULL UNIQUE,
  DisplayName VARCHAR(100) NOT NULL,
  ConnectionCount INT NOT NULL DEFAULT 0
);
CREATE TABLE T_Admins (
  IdAdmin INT PRIMARY KEY AUTO_INCREMENT,
  IdUser INT NOT NULL UNIQUE,
  Role VARCHAR(50) NOT NULL DEFAULT 'administrateur',
  FOREIGN KEY (IdUser) REFERENCES T_Users(IdUser)
);
CREATE TABLE T_Commands (
  IdCommand INT PRIMARY KEY AUTO_INCREMENT,
  IdUser INT NOT NULL,
  CommandDate DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  Status VARCHAR(30) NOT NULL DEFAULT 'en attente',
  FOREIGN KEY (IdUser) REFERENCES T_Users(IdUser)
);
CREATE TABLE T_CommandLines (
  IdCommandLine INT PRIMARY KEY AUTO_INCREMENT,
  IdCommand INT NOT NULL,
  IdArticle INT NOT NULL,
  Quantity INT NOT NULL,
  UnitaryPrice DECIMAL(10,2) NOT NULL,
  FOREIGN KEY (IdCommand) REFERENCES T_Commands(IdCommand),
  FOREIGN KEY (IdArticle) REFERENCES T_Articles(IdArticle)
);
INSERT INTO T_Articles (Description, Brand, UnitaryPrice) VALUES
('Souris', 'Logitech', 65.00), ('Clavier', 'Microsoft', 49.50),
('Système d’exploitation', 'Linux', 0.00), ('Tapis souris', 'Demo', 5.00),
('Clé USB 64 Go', 'Kingston', 8.00), ('Laptop', 'HP', 1199.00),
('CD x 500', 'CETME', 250.00), ('DVD-R x 100', 'CETME', 99.00),
('DVD+R x 100', 'CETME', 105.00), ('Batterie laptop', 'HP', 80.00),
('Casque audio', 'Sony', 105.00);
INSERT INTO T_Users (Login, DisplayName) VALUES
('Anderson', 'Neo'), ('Skywalker', 'Luke'), ('Plissken', 'Snake'), ('Ripley', 'Ellen'), ('Bond', 'James');
INSERT INTO T_Admins (IdUser) VALUES (1);
INSERT INTO T_Commands (IdUser, Status) VALUES (1, 'validée'), (2, 'en attente');
INSERT INTO T_CommandLines (IdCommand, IdArticle, Quantity, UnitaryPrice) VALUES
(1, 1, 2, 65.00), (1, 2, 1, 49.50), (2, 6, 1, 1199.00);
