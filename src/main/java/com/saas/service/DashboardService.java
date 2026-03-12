package com.saas.service;

import com.saas.model.*;
import com.saas.model.Venta.EstadoVenta;
import com.saas.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService {

    private final ProductoRepository productoRepository;
    private final VentaRepository ventaRepository;
    private final MovimientoRepository movimientoRepository;
    private final CategoriaRepository categoriaRepository;

    /**
     * Obtiene las estadísticas principales para el dashboard
     */
    public Map<String, Object> getEstadisticasDashboard() {
        Map<String, Object> stats = new HashMap<>();

        // Productos
        long totalProductos = productoRepository.countByEstaActivoTrue();
        long productosStockBajo = productoRepository.countProductosStockBajo();
        long productosSinStock = productoRepository.findProductosSinStock().size();

        // Ventas de hoy
        LocalDateTime inicioDia = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime finDia = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);
        
        BigDecimal ventasHoy = ventaRepository.sumTotalByFechaVentaBetweenAndEstado(
                inicioDia, finDia, EstadoVenta.COMPLETADA);
        
        long cantidadVentasHoy = ventaRepository.countByFechaVentaAfterAndEstado(
                inicioDia, EstadoVenta.COMPLETADA);

        // Ventas del mes
        LocalDateTime inicioMes = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        BigDecimal ventasMes = ventaRepository.sumTotalByFechaVentaBetweenAndEstado(
                inicioMes, finDia, EstadoVenta.COMPLETADA);

        // Categorías
        long totalCategorias = categoriaRepository.count();

        // Movimientos recientes
        long movimientosRecientes = movimientoRepository.countMovimientosRecientes(
                LocalDateTime.now().minusDays(7));

        // Valor del inventario
        List<Producto> productos = productoRepository.findByEstaActivoTrue();
        BigDecimal valorInventario = productos.stream()
                .map(p -> {
                    BigDecimal costo = p.getCosto() != null ? p.getCosto() : BigDecimal.ZERO;
                    return costo.multiply(new BigDecimal(p.getStockActual()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Productos más vendidos (últimos 30 días)
        LocalDateTime hace30Dias = LocalDateTime.now().minusDays(30);

        // Llenar el mapa
        stats.put("totalProductos", totalProductos);
        stats.put("productosStockBajo", productosStockBajo);
        stats.put("productosSinStock", productosSinStock);
        stats.put("ventasHoy", ventasHoy != null ? ventasHoy : BigDecimal.ZERO);
        stats.put("cantidadVentasHoy", cantidadVentasHoy);
        stats.put("ventasMes", ventasMes != null ? ventasMes : BigDecimal.ZERO);
        stats.put("totalCategorias", totalCategorias);
        stats.put("movimientosRecientes", movimientosRecientes);
        stats.put("valorInventario", valorInventario);

        log.info("Estadísticas del dashboard obtenidas: {} productos, {} ventas hoy", 
                totalProductos, cantidadVentasHoy);

        return stats;
    }

    /**
     * Obtiene productos con stock bajo
     */
    public List<Producto> getProductosStockBajo() {
        return productoRepository.findProductosConStockBajo();
    }

    /**
     * Obtiene ventas recientes
     */
    public List<Venta> getVentasRecientes() {
        return ventaRepository.findVentasCompletadasRecientes(LocalDateTime.now().minusDays(7));
    }

    /**
     * Obtiene movimientos recientes
     */
    public List<Movimiento> getMovimientosRecientes(int limite) {
        LocalDateTime fechaInicio = LocalDateTime.now().minusDays(7);
        List<Movimiento> movimientos = movimientoRepository.findByFechaMovimientoBetween(fechaInicio, LocalDateTime.now());
        
        // Limitar resultados
        if (movimientos.size() > limite) {
            return movimientos.subList(0, limite);
        }
        return movimientos;
    }

    /**
     * Obtiene el resumen de inventario
     */
    public Map<String, Object> getResumenInventario() {
        Map<String, Object> resumen = new HashMap<>();

        List<Producto> productos = productoRepository.findByEstaActivoTrue();

        int totalStock = productos.stream()
                .mapToInt(p -> p.getStockActual() != null ? p.getStockActual() : 0)
                .sum();

        BigDecimal valorTotal = productos.stream()
                .map(p -> {
                    BigDecimal costo = p.getCosto() != null ? p.getCosto() : BigDecimal.ZERO;
                    return costo.multiply(new BigDecimal(p.getStockActual() != null ? p.getStockActual() : 0));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal valorVentas = productos.stream()
                .map(p -> p.getPrecio().multiply(new BigDecimal(p.getStockActual() != null ? p.getStockActual() : 0)))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        resumen.put("totalProductos", productos.size());
        resumen.put("totalStock", totalStock);
        resumen.put("valorTotalCosto", valorTotal);
        resumen.put("valorTotalVenta", valorVentas);
        resumen.put("potencialGanancia", valorVentas.subtract(valorTotal));

        return resumen;
    }
}
