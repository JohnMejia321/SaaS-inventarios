-- =====================================================
-- DATOS INICIALES PARA EL SISTEMA DE INVENTARIO SAAS
-- =====================================================

-- Insertar categorías
INSERT INTO categorias (nombre, descripcion, esta_activa) VALUES 
('Electrónica', 'Dispositivos electrónicos y accesorios', true),
('Alimentos y Bebidas', 'Productos de consumo alimenticio', true),
('Ropa y Accesorios', 'Vestimenta y complementos', true),
('Hogar y Decoración', 'Artículos para el hogar', true),
('Papelería', 'Útiles de oficina y escolares', true);

-- Insertar productos de ejemplo (con códigos de barra para el lector)
-- Electrónica
INSERT INTO productos (nombre, codigo_barra, codigo_qr, descripcion, precio, costo, stock_actual, stock_minimo, stock_maximo, categoria_id, esta_activo) VALUES
('Audífonos Bluetooth', '1234567890001', 'QR001', 'Audífonos inalámbricos de alta calidad', 89900.00, 45000.00, 25, 5, 50, 1, true),
('Cargador USB', '1234567890002', 'QR002', 'Cargador rápido 20W', 35000.00, 15000.00, 50, 10, 100, 1, true),
('Mouse Inalámbrico', '1234567890003', 'QR003', 'Mouse optical wireless', 45000.00, 20000.00, 30, 8, 60, 1, true),
('Teclado Mecánico', '1234567890004', 'QR004', 'Teclado RGB mechanical', 120000.00, 70000.00, 15, 3, 30, 1, true);

-- Alimentos y Bebidas
INSERT INTO productos (nombre, codigo_barra, codigo_qr, descripcion, precio, costo, stock_actual, stock_minimo, stock_maximo, categoria_id, esta_activo) VALUES
('Agua Mineral 500ml', '2234567890001', 'QR101', 'Agua mineral sin gas', 2500.00, 1200.00, 100, 20, 200, 2, true),
('Galletas Chocolate', '2234567890002', 'QR102', 'Galletas con cobertura de chocolate', 3500.00, 1800.00, 80, 15, 150, 2, true),
('Jugo Naranja 1L', '2234567890003', 'QR103', 'Jugo natural de naranja', 5500.00, 3000.00, 40, 10, 80, 2, true),
('Café Instantáneo', '2234567890004', 'QR104', 'Café instantáneo premium', 12000.00, 6000.00, 25, 5, 50, 2, true);

-- Ropa y Accesorios
INSERT INTO productos (nombre, codigo_barra, codigo_qr, descripcion, precio, costo, stock_actual, stock_minimo, stock_maximo, categoria_id, esta_activo) VALUES
('Camiseta Algodón', '3234567890001', 'QR201', 'Camiseta 100% algodón básica', 25000.00, 12000.00, 45, 10, 80, 3, true),
('Jeans Slim', '3234567890002', 'QR202', 'Pantalón jeans corte slim', 65000.00, 35000.00, 20, 5, 40, 3, true),
('Gorra Deportiva', '3234567890003', 'QR203', 'Gorra adjustable sport', 18000.00, 8000.00, 30, 8, 50, 3, true);

-- Hogar y Decoración
INSERT INTO productos (nombre, codigo_barra, codigo_qr, descripcion, precio, costo, stock_actual, stock_minimo, stock_maximo, categoria_id, esta_activo) VALUES
('Lámpara LED', '4234567890001', 'QR301', 'Lámpara de escritorio LED', 45000.00, 22000.00, 18, 4, 35, 4, true),
('Almohada memory', '4234567890002', 'QR302', 'Almohada viscoelástica', 55000.00, 28000.00, 12, 3, 25, 4, true),
('Set de vasos', '4234567890003', 'QR303', 'Set 6 vasos de vidrio', 28000.00, 14000.00, 22, 5, 40, 4, true);

-- Papelería
INSERT INTO productos (nombre, codigo_barra, codigo_qr, descripcion, precio, costo, stock_actual, stock_minimo, stock_maximo, categoria_id, esta_activo) VALUES
('Cuaderno Rayado', '5234567890001', 'QR401', 'Cuaderno 100 hojas rayado', 8500.00, 4000.00, 60, 15, 100, 5, true),
('Lápices x12', '5234567890002', 'QR402', 'Caja de 12 lápices de colores', 12000.00, 5500.00, 35, 10, 60, 5, true),
('Borrador Grande', '5234567890003', 'QR403', 'Borrador de nata grande', 2500.00, 1000.00, 80, 20, 150, 5, true),
('Pegamento Stick', '5234567890004', 'QR404', 'Pegamento en barra grande', 4500.00, 2000.00, 50, 12, 80, 5, true);

-- Productos con stock bajo (para probar alertas)
INSERT INTO productos (nombre, codigo_barra, codigo_qr, descripcion, precio, costo, stock_actual, stock_minimo, stock_maximo, categoria_id, esta_activo) VALUES
('Pendrive 16GB', '6234567890001', 'QR501', 'Pendrive USB 2.0 16GB', 22000.00, 12000.00, 3, 5, 30, 1, true),
('Cable HDMI', '6234567890002', 'QR502', 'Cable HDMI 1.5 metros', 18000.00, 9000.00, 2, 4, 25, 1, true);

-- Sin stock (para probar alertas)
INSERT INTO productos (nombre, codigo_barra, codigo_qr, descripcion, precio, costo, stock_actual, stock_minimo, stock_maximo, categoria_id, esta_activo) VALUES
('Power Bank 10000mAh', '7234567890001', 'QR601', 'Cargador portátil 10000mAh', 55000.00, 30000.00, 0, 5, 20, 1, true);

-- Nota: Los movimientos se crean automáticamente cuando se hacen ventas
-- La fecha de creación se maneja automáticamente por @PrePersist
