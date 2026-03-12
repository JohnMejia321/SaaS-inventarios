package com.saas.controller;

import com.saas.model.Producto;
import com.saas.model.Venta;
import com.saas.service.ProductoService;
import com.saas.service.VentaService;
import com.saas.service.VentaService.VentaRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Controller
@RequestMapping("/ventas")
@RequiredArgsConstructor
@Slf4j
public class VentaController {

    private final VentaService ventaService;
    private final ProductoService productoService;

    // Carrito de venta en memoria (en una aplicación real sería Sessions o Redis)
    private final Map<String, List<CarritoItem>> carritos = new ConcurrentHashMap<>();

    // ===============================
    // Vistas
    // ===============================

    @GetMapping
    public String listarVentas(Model model) {
        List<Venta> ventas = ventaService.findVentasRecientes();
        model.addAttribute("ventas", ventas);
        return "ventas/lista";
    }

    /**
     * Página principal de ventas con SIMULADOR DE LECTOR DE CÓDIGO DE BARRAS/QR
     */
    @GetMapping("/nueva")
    public String nuevaVenta(Model model) {
        List<Producto> productos = productoService.findAllActivos();
        model.addAttribute("productos", productos);
        model.addAttribute("carrito", new ArrayList<>());
        return "ventas/nueva";
    }

    /**
     * SIMULADOR DE LECTOR DE CÓDIGO DE BARRAS
     * Este método simula lo que hace un lector de código de barras real
     * Cuando pasas el código por el lector, envía el código aquí
     */
    @GetMapping("/simular-lector")
    @ResponseBody
    public Map<String, Object> simularLector(@RequestParam String codigo, 
                                             @RequestParam(defaultValue = "1") Integer cantidad) {
        log.info("SIMULADOR DE LECTOR: Código escaneado: {}, Cantidad: {}", codigo, cantidad);
        
        // Buscar producto por código de barras o QR
        var productoOpt = productoService.findByCodigoBarraOrQr(codigo);
        
        if (productoOpt.isEmpty()) {
            return Map.of(
                "success", false,
                "mensaje", "Producto no encontrado con código: " + codigo,
                "tipo", "error"
            );
        }
        
        Producto producto = productoOpt.get();
        
        // Verificar stock
        if (!producto.tieneStockSuficiente(cantidad)) {
            return Map.of(
                "success", false,
                "mensaje", "Stock insuficiente. Disponible: " + producto.getStockActual(),
                "tipo", "warning"
            );
        }
        
        return Map.of(
            "success", true,
            "producto", Map.of(
                "id", producto.getId(),
                "nombre", producto.getNombre(),
                "codigoBarra", producto.getCodigoBarra(),
                "precio", producto.getPrecio(),
                "stock", producto.getStockActual()
            ),
            "mensaje", "Producto encontrado: " + producto.getNombre(),
            "tipo", "success"
        );
    }

    /**
     * Procesar la venta completa
     */
    @PostMapping("/procesar")
    public String procesarVenta(@RequestParam String metodoPago,
                                 @RequestParam(required = false) String clienteNombre,
                                 @RequestParam(required = false) String clienteDocumento,
                                 @RequestParam String productosJson,
                                 RedirectAttributes redirectAttributes) {
        try {
            // Parsear los productos del JSON simplificado
            List<VentaRequest> items = parsearProductos(productosJson);
            
            if (items.isEmpty()) {
                redirectAttributes.addFlashAttribute("mensaje", "No hay productos en la venta");
                redirectAttributes.addFlashAttribute("tipo", "warning");
                return "redirect:/ventas/nueva";
            }
            
            Venta venta = ventaService.procesarVenta(items, metodoPago, clienteNombre, clienteDocumento);
            
            redirectAttributes.addFlashAttribute("mensaje", "Venta procesada exitosamente. Número: " + venta.getNumeroVenta());
            redirectAttributes.addFlashAttribute("tipo", "success");
            redirectAttributes.addFlashAttribute("venta", venta);
            
            return "redirect:/ventas/detalle/" + venta.getId();
            
        } catch (Exception e) {
            log.error("Error al procesar venta", e);
            redirectAttributes.addFlashAttribute("mensaje", "Error: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipo", "danger");
            return "redirect:/ventas/nueva";
        }
    }

    /**
     * Ver detalle de una venta
     */
    @GetMapping("/detalle/{id}")
    public String detalleVenta(@PathVariable Long id, Model model) {
        Venta venta = ventaService.findVentaById(id)
                .orElseThrow(() -> new IllegalArgumentException("Venta no encontrada"));
        model.addAttribute("venta", venta);
        return "ventas/detalle";
    }

    /**
     * Cancelar/anular venta
     */
    @PostMapping("/cancelar/{id}")
    public String cancelarVenta(@PathVariable Long id,
                                 @RequestParam String motivo,
                                 RedirectAttributes redirectAttributes) {
        try {
            ventaService.cancelarVenta(id, motivo);
            redirectAttributes.addFlashAttribute("mensaje", "Venta cancelada exitosamente");
            redirectAttributes.addFlashAttribute("tipo", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipo", "danger");
        }
        return "redirect:/ventas";
    }

    // ===============================
    // API REST para el lector
    // ===============================

    /**
     * API: Buscar producto por código (para el lector)
     */
    @GetMapping("/api/buscar-producto")
    @ResponseBody
    public Map<String, Object> buscarProducto(@RequestParam String codigo) {
        var productoOpt = productoService.findByCodigoBarraOrQr(codigo);
        
        if (productoOpt.isEmpty()) {
            return Map.of(
                "encontrado", false,
                "mensaje", "Producto no encontrado"
            );
        }
        
        Producto p = productoOpt.get();
        return Map.of(
            "encontrado", true,
            "producto", Map.of(
                "id", p.getId(),
                "nombre", p.getNombre(),
                "codigoBarra", p.getCodigoBarra(),
                "codigoQr", p.getCodigoQr(),
                "precio", p.getPrecio(),
                "stock", p.getStockActual()
            )
        );
    }

    // ===============================
    // Utilidades
    // ===============================

    private List<VentaRequest> parsearProductos(String json) {
        List<VentaRequest> items = new ArrayList<>();
        
        if (json == null || json.isEmpty()) {
            return items;
        }
        
        // Parse simple: formato "id:cantidad,id:cantidad"
        String[] partes = json.split(",");
        for (String parte : partes) {
            String[] cp = parte.trim().split(":");
            if (cp.length == 2) {
                VentaRequest req = new VentaRequest();
                req.setCodigo(cp[0]);
                try {
                    req.setCantidad(Integer.parseInt(cp[1]));
                    items.add(req);
                } catch (NumberFormatException e) {
                    log.warn("Cantidad inválida: {}", cp[1]);
                }
            }
        }
        
        return items;
    }

    // Clase auxiliar para el carrito
    public static class CarritoItem {
        private String codigo;
        private Integer cantidad;
        private BigDecimal descuento;
        
        public CarritoItem(String codigo, Integer cantidad) {
            this.codigo = codigo;
            this.cantidad = cantidad;
            this.descuento = BigDecimal.ZERO;
        }

        // Getters y setters
        public String getCodigo() { return codigo; }
        public void setCodigo(String codigo) { this.codigo = codigo; }
        public Integer getCantidad() { return cantidad; }
        public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
        public BigDecimal getDescuento() { return descuento; }
        public void setDescuento(BigDecimal descuento) { this.descuento = descuento; }
    }
}
