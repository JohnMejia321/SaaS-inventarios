package com.saas.service;

import com.saas.model.Categoria;
import com.saas.model.Movimiento;
import com.saas.model.Movimiento.TipoMovimiento;
import com.saas.model.Producto;
import com.saas.repository.CategoriaRepository;
import com.saas.repository.MovimientoRepository;
import com.saas.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final MovimientoRepository movimientoRepository;
    private final CategoriaRepository categoriaRepository;

    // ===============================
    // Métodos de consulta
    // ===============================

    public List<Producto> findAll() {
        return productoRepository.findAll();
    }

    public List<Producto> findAllActivos() {
        return productoRepository.findByEstaActivoTrue();
    }

    public Optional<Producto> findById(Long id) {
        return productoRepository.findById(id);
    }

    public Optional<Producto> findByCodigoBarra(String codigoBarra) {
        return productoRepository.findByCodigoBarra(codigoBarra);
    }

    public Optional<Producto> findByCodigoQr(String codigoQr) {
        return productoRepository.findByCodigoQr(codigoQr);
    }

    /**
     * Busca un producto por código de barras o QR - ÚTIL PARA EL LECTOR
     */
    public Optional<Producto> findByCodigoBarraOrQr(String codigo) {
        return productoRepository.findByCodigoBarraOrQr(codigo);
    }

    public List<Producto> findByCategoriaId(Long categoriaId) {
        return productoRepository.findByCategoriaIdAndEstaActivoTrue(categoriaId);
    }

    public List<Producto> findProductosConStockBajo() {
        return productoRepository.findProductosConStockBajo();
    }

    public List<Producto> findProductosSinStock() {
        return productoRepository.findProductosSinStock();
    }

    public List<Producto> searchByNombre(String nombre) {
        return productoRepository.findByNombreContainingIgnoreCase(nombre);
    }

    public long countProductosActivos() {
        return productoRepository.countByEstaActivoTrue();
    }

    public long countProductosStockBajo() {
        return productoRepository.countProductosStockBajo();
    }

    // ===============================
    // Métodos de guardado
    // ===============================

    @Transactional
    public Producto save(Producto producto) {
        // Validar código de barras único
        if (producto.getCodigoBarra() != null && !producto.getCodigoBarra().isEmpty()) {
            Optional<Producto> existing = productoRepository.findByCodigoBarra(producto.getCodigoBarra());
            if (existing.isPresent() && !existing.get().getId().equals(producto.getId())) {
                throw new IllegalArgumentException("Ya existe un producto con este código de barras");
            }
        }
        
        // Validar código QR único
        if (producto.getCodigoQr() != null && !producto.getCodigoQr().isEmpty()) {
            Optional<Producto> existingQr = productoRepository.findByCodigoQr(producto.getCodigoQr());
            if (existingQr.isPresent() && !existingQr.get().getId().equals(producto.getId())) {
                throw new IllegalArgumentException("Ya existe un producto con este código QR");
            }
        }

        return productoRepository.save(producto);
    }

    @Transactional
    public void delete(Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));
        producto.setEstaActivo(false);
        productoRepository.save(producto);
    }

    // ===============================
    // Métodos de inventario
    // ===============================

    /**
     * Agrega stock a un producto (entrada de inventario)
     */
    @Transactional
    public Movimiento agregarStock(Long productoId, Integer cantidad, BigDecimal precioUnitario, String observaciones) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
        }

        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));

        Integer stockAnterior = producto.getStockActual();
        Integer stockNuevo = stockAnterior + cantidad;
        
        producto.setStockActual(stockNuevo);
        productoRepository.save(producto);

        // Registrar movimiento
        Movimiento movimiento = Movimiento.builder()
                .producto(producto)
                .tipoMovimiento(TipoMovimiento.ENTRADA)
                .cantidad(cantidad)
                .stockAnterior(stockAnterior)
                .stockNuevo(stockNuevo)
                .precioUnitario(precioUnitario)
                .total(precioUnitario.multiply(new BigDecimal(cantidad)))
                .observaciones(observaciones)
                .build();

        log.info("Stock agregado: {} unidades al producto {}. Stock anterior: {}, Stock nuevo: {}", 
                cantidad, producto.getNombre(), stockAnterior, stockNuevo);

        return movimientoRepository.save(movimiento);
    }

    /**
     * Reduce stock de un producto (salida de inventario)
     */
    @Transactional
    public Movimiento reducirStock(Long productoId, Integer cantidad, BigDecimal precioUnitario, String observaciones) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
        }

        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));

        if (!producto.tieneStockSuficiente(cantidad)) {
            throw new IllegalArgumentException("Stock insuficiente. Stock actual: " + producto.getStockActual());
        }

        Integer stockAnterior = producto.getStockActual();
        Integer stockNuevo = stockAnterior - cantidad;
        
        producto.setStockActual(stockNuevo);
        productoRepository.save(producto);

        // Registrar movimiento
        Movimiento movimiento = Movimiento.builder()
                .producto(producto)
                .tipoMovimiento(TipoMovimiento.SALIDA)
                .cantidad(cantidad)
                .stockAnterior(stockAnterior)
                .stockNuevo(stockNuevo)
                .precioUnitario(precioUnitario)
                .total(precioUnitario.multiply(new BigDecimal(cantidad)))
                .observaciones(observaciones)
                .build();

        log.info("Stock reducido: {} unidades del producto {}. Stock anterior: {}, Stock nuevo: {}", 
                cantidad, producto.getNombre(), stockAnterior, stockNuevo);

        return movimientoRepository.save(movimiento);
    }

    /**
     * Ajusta el stock (puede ser positivo o negativo)
     */
    @Transactional
    public Movimiento ajustarStock(Long productoId, Integer nuevaCantidad, String observaciones) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));

        Integer stockAnterior = producto.getStockActual();
        Integer diferencia = nuevaCantidad - stockAnterior;
        
        producto.setStockActual(nuevaCantidad);
        productoRepository.save(producto);

        TipoMovimiento tipo = diferencia > 0 ? TipoMovimiento.AJUSTE_POSITIVO : TipoMovimiento.AJUSTE_NEGATIVO;

        Movimiento movimiento = Movimiento.builder()
                .producto(producto)
                .tipoMovimiento(tipo)
                .cantidad(Math.abs(diferencia))
                .stockAnterior(stockAnterior)
                .stockNuevo(nuevaCantidad)
                .observaciones(observaciones != null ? observaciones : "Ajuste de inventario")
                .build();

        log.info("Stock ajustado: producto {}. Anterior: {}, Nuevo: {}, Diferencia: {}", 
                producto.getNombre(), stockAnterior, nuevaCantidad, diferencia);

        return movimientoRepository.save(movimiento);
    }

    // ===============================
    // Métodos de categoría
    // ===============================

    public List<Categoria> findAllCategorias() {
        return categoriaRepository.findAll();
    }

    public List<Categoria> findCategoriasActivas() {
        return categoriaRepository.findByEstaActivaTrue();
    }

    public Categoria saveCategoria(Categoria categoria) {
        return categoriaRepository.save(categoria);
    }

    public void deleteCategoria(Long id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada"));
        categoria.setEstaActiva(false);
        categoriaRepository.save(categoria);
    }
}
