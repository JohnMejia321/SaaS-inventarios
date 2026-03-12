package com.saas;

import com.saas.model.Categoria;
import com.saas.model.Producto;
import com.saas.model.Usuario;
import com.saas.repository.CategoriaRepository;
import com.saas.repository.ProductoRepository;
import com.saas.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;

@SpringBootApplication
@RequiredArgsConstructor
@Slf4j
public class SaasApplication {

    public static void main(String[] args) {
        SpringApplication.run(SaasApplication.class, args);
    }

    @Bean
    public CommandLineRunner initData(CategoriaRepository categoriaRepository, 
                                      ProductoRepository productoRepository,
                                      UsuarioRepository usuarioRepository,
                                      PasswordEncoder passwordEncoder) {
        return args -> {
            // Crear usuario admin si no existe
            if (!usuarioRepository.existsByUsername("admin")) {
                Usuario admin = Usuario.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin"))
                    .nombre("Administrador")
                    .email("admin@saas.com")
                    .rol(Usuario.Rol.ADMIN)
                    .enabled(true)
                    .build();
                usuarioRepository.save(admin);
                log.info("Usuario admin creado con contraseña: admin");
            }

            // Verificar si ya hay datos
            if (categoriaRepository.count() > 0) {
                log.info("Los datos ya están inicializados");
                return;
            }

            log.info("Inicializando datos de prueba...");

            // Crear categorías
            Categoria electronica = categoriaRepository.save(
                Categoria.builder().nombre("Electrónica")
                    .descripcion("Dispositivos electrónicos y accesorios").estaActiva(true).build());
            
            Categoria alimentos = categoriaRepository.save(
                Categoria.builder().nombre("Alimentos y Bebidas")
                    .descripcion("Productos de consumo alimenticio").estaActiva(true).build());
            
            Categoria ropa = categoriaRepository.save(
                Categoria.builder().nombre("Ropa y Accesorios")
                    .descripcion("Vestimenta y complementos").estaActiva(true).build());
            
            Categoria hogar = categoriaRepository.save(
                Categoria.builder().nombre("Hogar y Decoración")
                    .descripcion("Artículos para el hogar").estaActiva(true).build());
            
            Categoria papelería = categoriaRepository.save(
                Categoria.builder().nombre("Papelería")
                    .descripcion("Útiles de oficina y escolares").estaActiva(true).build());

            // Crear productos de ejemplo
            productoRepository.save(Producto.builder()
                .nombre("Audífonos Bluetooth")
                .codigoBarra("1234567890001")
                .codigoQr("QR001")
                .descripcion("Audífonos inalámbricos de alta calidad")
                .precio(new BigDecimal("89900"))
                .costo(new BigDecimal("45000"))
                .stockActual(25)
                .stockMinimo(5)
                .stockMaximo(50)
                .categoria(electronica)
                .estaActivo(true)
                .build());

            productoRepository.save(Producto.builder()
                .nombre("Cargador USB")
                .codigoBarra("1234567890002")
                .codigoQr("QR002")
                .descripcion("Cargador rápido 20W")
                .precio(new BigDecimal("35000"))
                .costo(new BigDecimal("15000"))
                .stockActual(50)
                .stockMinimo(10)
                .stockMaximo(100)
                .categoria(electronica)
                .estaActivo(true)
                .build());

            productoRepository.save(Producto.builder()
                .nombre("Mouse Inalámbrico")
                .codigoBarra("1234567890003")
                .codigoQr("QR003")
                .descripcion("Mouse optical wireless")
                .precio(new BigDecimal("45000"))
                .costo(new BigDecimal("20000"))
                .stockActual(30)
                .stockMinimo(8)
                .stockMaximo(60)
                .categoria(electronica)
                .estaActivo(true)
                .build());

            productoRepository.save(Producto.builder()
                .nombre("Teclado Mecánico")
                .codigoBarra("1234567890004")
                .codigoQr("QR004")
                .descripcion("Teclado RGB mechanical")
                .precio(new BigDecimal("120000"))
                .costo(new BigDecimal("70000"))
                .stockActual(15)
                .stockMinimo(3)
                .stockMaximo(30)
                .categoria(electronica)
                .estaActivo(true)
                .build());

            // Alimentos
            productoRepository.save(Producto.builder()
                .nombre("Agua Mineral 500ml")
                .codigoBarra("2234567890001")
                .codigoQr("QR101")
                .descripcion("Agua mineral sin gas")
                .precio(new BigDecimal("2500"))
                .costo(new BigDecimal("1200"))
                .stockActual(100)
                .stockMinimo(20)
                .stockMaximo(200)
                .categoria(alimentos)
                .estaActivo(true)
                .build());

            productoRepository.save(Producto.builder()
                .nombre("Galletas Chocolate")
                .codigoBarra("2234567890002")
                .codigoQr("QR102")
                .descripcion("Galletas con cobertura de chocolate")
                .precio(new BigDecimal("3500"))
                .costo(new BigDecimal("1800"))
                .stockActual(80)
                .stockMinimo(15)
                .stockMaximo(150)
                .categoria(alimentos)
                .estaActivo(true)
                .build());

            // Ropa
            productoRepository.save(Producto.builder()
                .nombre("Camiseta Algodón")
                .codigoBarra("3234567890001")
                .codigoQr("QR201")
                .descripcion("Camiseta 100% algodón básica")
                .precio(new BigDecimal("25000"))
                .costo(new BigDecimal("12000"))
                .stockActual(45)
                .stockMinimo(10)
                .stockMaximo(80)
                .categoria(ropa)
                .estaActivo(true)
                .build());

            // Hogar
            productoRepository.save(Producto.builder()
                .nombre("Lámpara LED")
                .codigoBarra("4234567890001")
                .codigoQr("QR301")
                .descripcion("Lámpara de escritorio LED")
                .precio(new BigDecimal("45000"))
                .costo(new BigDecimal("22000"))
                .stockActual(18)
                .stockMinimo(4)
                .stockMaximo(35)
                .categoria(hogar)
                .estaActivo(true)
                .build());

            // Papelería
            productoRepository.save(Producto.builder()
                .nombre("Cuaderno Rayado")
                .codigoBarra("5234567890001")
                .codigoQr("QR401")
                .descripcion("Cuaderno 100 hojas rayado")
                .precio(new BigDecimal("8500"))
                .costo(new BigDecimal("4000"))
                .stockActual(60)
                .stockMinimo(15)
                .stockMaximo(100)
                .categoria(papelería)
                .estaActivo(true)
                .build());

            // Productos con stock bajo (para probar alertas)
            productoRepository.save(Producto.builder()
                .nombre("Pendrive 16GB")
                .codigoBarra("6234567890001")
                .codigoQr("QR501")
                .descripcion("Pendrive USB 2.0 16GB")
                .precio(new BigDecimal("22000"))
                .costo(new BigDecimal("12000"))
                .stockActual(3)
                .stockMinimo(5)
                .stockMaximo(30)
                .categoria(electronica)
                .estaActivo(true)
                .build());

            // Sin stock
            productoRepository.save(Producto.builder()
                .nombre("Power Bank 10000mAh")
                .codigoBarra("7234567890001")
                .codigoQr("QR601")
                .descripcion("Cargador portátil 10000mAh")
                .precio(new BigDecimal("55000"))
                .costo(new BigDecimal("30000"))
                .stockActual(0)
                .stockMinimo(5)
                .stockMaximo(20)
                .categoria(electronica)
                .estaActivo(true)
                .build());

            log.info("Datos de prueba inicializados correctamente");
            log.info("Total de categorías: {}", categoriaRepository.count());
            log.info("Total de productos: {}", productoRepository.count());
        };
    }
}
