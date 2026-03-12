package com.saas.service;

import com.saas.model.*;
import com.saas.model.Movimiento.TipoMovimiento;
import com.saas.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class VentaService {

    private final VentaRepository ventaRepository;
    private final DetalleVentaRepository detalleVentaRepository;
    private final ProductoRepository productoRepository;
    private final MovimientoRepository movimientoRepository;
    private final ProductoService productoService;

    // ===============================
    // Métodos de consulta
    // ===============================

    public List<Venta> findAllVentas() {
        return ventaRepository.findAll();
    }

    public List<Venta> findVentasRecientes() {
        return ventaRepository.findTop10ByOrderByFechaVentaDesc();
    }

    public Optional<Venta> findVentaById(Long id) {
        return ventaRepository.findById(id);
    }

    public Optional<Venta> findVentaByNumero(String numeroVenta) {
        return ventaRepository.findByNumeroVenta(numeroVenta);
    }

    public List<Venta> findVentasByEstado(Venta.EstadoVenta estado) {
        return ventaRepository.findByEstado(estado);
    }

    public List<Venta> findVentasByFecha(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        return ventaRepository.findByFechaVentaBetweenOrderByFechaVentaDesc(fechaInicio, fechaFin);
    }

    // ===============================
    // Ventas del día
    // ===============================

    public BigDecimal getTotalVentasHoy() {
        LocalDateTime inicioDia = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime finDia = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);
        return ventaRepository.sumTotalByFechaVentaBetweenAndEstado(inicioDia, finDia, Venta.EstadoVenta.COMPLETADA);
    }

    public long getCountVentasHoy() {
        LocalDateTime inicioDia = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        return ventaRepository.countByFechaVentaAfterAndEstado(inicioDia, Venta.EstadoVenta.COMPLETADA);
    }

    // ===============================
    // Procesar venta con código de barras
    // ===============================

    /**
     * Procesa una venta - SIMULA EL LECTOR DE CÓDIGO DE BARRAS/QR
     * Este es el método principal que se usa cuando se lee un código de barras
     */
    @Transactional
    public Venta procesarVenta(List<VentaRequest> items, String metodoPago, String clienteNombre, String clienteDocumento) {
        
        // Crear la venta
        Venta venta = Venta.builder()
                .numeroVenta("V-" + System.currentTimeMillis())
                .estado(Venta.EstadoVenta.PENDIENTE)
                .metodoPago(metodoPago != null ? metodoPago : "EFECTIVO")
                .clienteNombre(clienteNombre)
                .clienteDocumento(clienteDocumento)
                .detalles(new ArrayList<>())
                .build();

        BigDecimal subtotal = BigDecimal.ZERO;

        // Procesar cada item
        for (VentaRequest request : items) {
            // Buscar producto por código de barras o QR
            Optional<Producto> productoOpt = productoService.findByCodigoBarraOrQr(request.getCodigo());
            
            if (productoOpt.isEmpty()) {
                throw new IllegalArgumentException("Producto no encontrado con código: " + request.getCodigo());
            }

            Producto producto = productoOpt.get();

            // Verificar stock
            if (!producto.tieneStockSuficiente(request.getCantidad())) {
                throw new IllegalArgumentException("Stock insuficiente para: " + producto.getNombre() + 
                    ". Stock disponible: " + producto.getStockActual());
            }

            // Crear detalle de venta
            DetalleVenta detalle = DetalleVenta.builder()
                    .venta(venta)
                    .producto(producto)
                    .cantidad(request.getCantidad())
                    .precioUnitario(producto.getPrecio())
                    .costoUnitario(producto.getCosto() != null ? producto.getCosto() : BigDecimal.ZERO)
                    .descuento(request.getDescuento() != null ? request.getDescuento() : BigDecimal.ZERO)
                    .build();
            detalle.calcularSubtotal();

            venta.getDetalles().add(detalle);
            subtotal = subtotal.add(detalle.getSubtotal());
        }

        // Calcular totales
        venta.setSubtotal(subtotal);
        BigDecimal impuesto = subtotal.multiply(new BigDecimal("0.19")); // 19% IVA
        venta.setImpuesto(impuesto);
        venta.setTotal(subtotal.add(impuesto));

        // Guardar venta
        Venta ventaGuardada = ventaRepository.save(venta);

        // Actualizar stock y registrar movimientos
        for (DetalleVenta detalle : ventaGuardada.getDetalles()) {
            Producto producto = detalle.getProducto();
            
            // Reducir stock
            Integer stockAnterior = producto.getStockActual();
            Integer stockNuevo = stockAnterior - detalle.getCantidad();
            producto.setStockActual(stockNuevo);
            productoRepository.save(producto);

            // Registrar movimiento de salida
            Movimiento movimiento = Movimiento.builder()
                    .producto(producto)
                    .tipoMovimiento(TipoMovimiento.SALIDA)
                    .cantidad(detalle.getCantidad())
                    .stockAnterior(stockAnterior)
                    .stockNuevo(stockNuevo)
                    .precioUnitario(detalle.getPrecioUnitario())
                    .total(detalle.getSubtotal())
                    .observaciones("Venta: " + ventaGuardada.getNumeroVenta())
                    .build();
            movimientoRepository.save(movimiento);

            log.info("Venta {} - Producto: {}, Cantidad: {}, Stock anterior: {}, Stock nuevo: {}",
                    ventaGuardada.getNumeroVenta(), producto.getNombre(), detalle.getCantidad(), stockAnterior, stockNuevo);
        }

        // Cambiar estado a completada
        ventaGuardada.setEstado(Venta.EstadoVenta.COMPLETADA);
        
        log.info("Venta {} procesada exitosamente. Total: {}", ventaGuardada.getNumeroVenta(), ventaGuardada.getTotal());
        
        return ventaRepository.save(ventaGuardada);
    }

    /**
     * Agrega un producto a la venta actual usando código de barras/QR
     * ÚTIL PARA SIMULAR EL LECTOR
     */
    @Transactional
    public DetalleVenta agregarProductoAVenta(Venta venta, String codigoBarraOQr, Integer cantidad) {
        
        // Buscar producto por código de barras o QR
        Optional<Producto> productoOpt = productoService.findByCodigoBarraOrQr(codigoBarraOQr);
        
        if (productoOpt.isEmpty()) {
            throw new IllegalArgumentException("Producto no encontrado con código: " + codigoBarraOQr);
        }

        Producto producto = productoOpt.get();

        // Verificar stock
        if (!producto.tieneStockSuficiente(cantidad)) {
            throw new IllegalArgumentException("Stock insuficiente. Stock actual: " + producto.getStockActual());
        }

        // Crear detalle
        DetalleVenta detalle = DetalleVenta.builder()
                .venta(venta)
                .producto(producto)
                .cantidad(cantidad)
                .precioUnitario(producto.getPrecio())
                .costoUnitario(producto.getCosto() != null ? producto.getCosto() : BigDecimal.ZERO)
                .build();
        detalle.calcularSubtotal();

        // Guardar detalle
        detalleVentaRepository.save(detalle);

        // Recalcular totales de la venta
        venta.calcularTotal();
        ventaRepository.save(venta);

        log.info("Producto agregado a venta {}: {} x {}", 
                venta.getNumeroVenta(), producto.getNombre(), cantidad);

        return detalle;
    }

    // ===============================
    // Cancelar/Anular venta
    // ===============================

    @Transactional
    public void cancelarVenta(Long ventaId, String motivo) {
        Venta venta = ventaRepository.findById(ventaId)
                .orElseThrow(() -> new IllegalArgumentException("Venta no encontrada"));

        if (venta.getEstado() == Venta.EstadoVenta.CANCELADA) {
            throw new IllegalArgumentException("La venta ya está cancelada");
        }

        // Devolver stock
        for (DetalleVenta detalle : venta.getDetalles()) {
            Producto producto = detalle.getProducto();
            
            Integer stockAnterior = producto.getStockActual();
            Integer stockNuevo = stockAnterior + detalle.getCantidad();
            producto.setStockActual(stockNuevo);
            productoRepository.save(producto);

            // Registrar movimiento de devolución
            Movimiento movimiento = Movimiento.builder()
                    .producto(producto)
                    .tipoMovimiento(TipoMovimiento.DEVOLUCION)
                    .cantidad(detalle.getCantidad())
                    .stockAnterior(stockAnterior)
                    .stockNuevo(stockNuevo)
                    .precioUnitario(detalle.getPrecioUnitario())
                    .observaciones("Devolución por cancelación de venta: " + venta.getNumeroVenta() + ". Motivo: " + motivo)
                    .build();
            movimientoRepository.save(movimiento);
        }

        // Cambiar estado
        venta.setEstado(Venta.EstadoVenta.CANCELADA);
        venta.setObservaciones(venta.getObservaciones() + " | CANCELADA. Motivo: " + motivo);
        ventaRepository.save(venta);

        log.info("Venta {} cancelada. Stock devuelto.", venta.getNumeroVenta());
    }

    // ===============================
    // DTO para solicitud de venta
    // ===============================

    @lombok.Data
    public static class VentaRequest {
        private String codigo;  // Código de barras o QR
        private Integer cantidad;
        private BigDecimal descuento;
    }
}
