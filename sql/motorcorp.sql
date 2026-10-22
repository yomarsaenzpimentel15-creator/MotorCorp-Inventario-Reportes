CREATE DATABASE IF NOT EXISTS motorcorp CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE motorcorp;
CREATE TABLE IF NOT EXISTS repuestos (
 id BIGINT PRIMARY KEY AUTO_INCREMENT,
 codigo VARCHAR(30) NOT NULL UNIQUE,
 nombre VARCHAR(120) NOT NULL,
 categoria VARCHAR(70) NOT NULL,
 precio DECIMAL(12,2) NOT NULL,
 stock INT NOT NULL DEFAULT 0,
 stock_minimo INT NOT NULL DEFAULT 5,
 creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
 CONSTRAINT chk_precio CHECK (precio >= 0),
 CONSTRAINT chk_stock CHECK (stock >= 0),
 CONSTRAINT chk_stock_min CHECK (stock_minimo >= 0)
);
CREATE TABLE IF NOT EXISTS movimientos (
 id BIGINT PRIMARY KEY AUTO_INCREMENT,
 repuesto_id BIGINT NOT NULL,
 tipo ENUM('ENTRADA','SALIDA') NOT NULL,
 cantidad INT NOT NULL,
 motivo VARCHAR(200) NOT NULL,
 fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 CONSTRAINT fk_mov_repuesto FOREIGN KEY(repuesto_id) REFERENCES repuestos(id),
 CONSTRAINT chk_cantidad CHECK(cantidad > 0)
);
INSERT IGNORE INTO repuestos(codigo,nombre,categoria,precio,stock,stock_minimo) VALUES
('MC-001','Filtro de aceite','Filtros',35.00,20,5),
('MC-002','Pastillas de freno','Frenos',120.00,12,4),
('MC-003','Bujía de encendido','Motor',22.50,30,8),
('MC-004','Aceite 5W-30 1L','Lubricantes',48.00,15,5);
