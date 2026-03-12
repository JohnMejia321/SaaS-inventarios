package com.saas.controller;

import com.saas.model.Categoria;
import com.saas.model.Producto;
import com.saas.service.ProductoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/productos")
@RequiredArgsConstructor
@Slf4j
public class ProductoController {

    private final ProductoService productoService;

    // ===============================
    // Vistas
    // ===============================

    @GetMapping
    public String listarProductos(Model model) {
        List<Producto> productos = productoService.findAllActivos();
        List<Categoria> categorias = productoService.findCategoriasActivas();
        
        model.addAttribute("productos", productos);
        model.addAttribute("categorias", categorias);
        model.addAttribute("producto", new Producto());
        
        return "productos/lista";
    }

    @GetMapping("/nuevo")
    public String nuevoProducto(Model model) {
        model.addAttribute("producto", new Producto());
        model.addAttribute("categorias", productoService.findCategoriasActivas());
        return "productos/formulario";
    }

    @GetMapping("/editar/{id}")
    public String editarProducto(@PathVariable Long id, Model model) {
        Producto producto = productoService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));
        
        model.addAttribute("producto", producto);
        model.addAttribute("categorias", productoService.findCategoriasActivas());
        return "productos/formulario";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarProducto(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            productoService.delete(id);
            redirectAttributes.addFlashAttribute("mensaje", "Producto eliminado exitosamente");
            redirectAttributes.addFlashAttribute("tipo", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error al eliminar: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipo", "danger");
        }
        return "redirect:/productos";
    }

    // ===============================
    // Acciones
    // ===============================

    @PostMapping("/guardar")
    public String guardarProducto(@ModelAttribute Producto producto, 
                                   @RequestParam(required = false) Long categoriaId,
                                   RedirectAttributes redirectAttributes) {
        try {
            if (categoriaId != null) {
                Categoria categoria = new Categoria();
                categoria.setId(categoriaId);
                producto.setCategoria(categoria);
            }
            
            productoService.save(producto);
            redirectAttributes.addFlashAttribute("mensaje", "Producto guardado exitosamente");
            redirectAttributes.addFlashAttribute("tipo", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error al guardar: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipo", "danger");
            log.error("Error al guardar producto", e);
        }
        return "redirect:/productos";
    }

    // ===============================
    // Inventario
    // ===============================

    @GetMapping("/stock-bajo")
    public String productosStockBajo(Model model) {
        List<Producto> productos = productoService.findProductosConStockBajo();
        model.addAttribute("productos", productos);
        return "productos/stock-bajo";
    }

    @GetMapping("/inventario")
    public String verInventario(Model model) {
        List<Producto> productos = productoService.findAllActivos();
        model.addAttribute("productos", productos);
        return "productos/inventario";
    }

    @PostMapping("/agregar-stock")
    public String agregarStock(@RequestParam Long productoId,
                                @RequestParam Integer cantidad,
                                @RequestParam(required = false) BigDecimal precioUnitario,
                                @RequestParam(required = false) String observaciones,
                                RedirectAttributes redirectAttributes) {
        try {
            productoService.agregarStock(productoId, cantidad, 
                    precioUnitario != null ? precioUnitario : BigDecimal.ZERO,
                    observaciones);
            redirectAttributes.addFlashAttribute("mensaje", "Stock agregado exitosamente");
            redirectAttributes.addFlashAttribute("tipo", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipo", "danger");
        }
        return "redirect:/productos/inventario";
    }

    @PostMapping("/ajustar-stock")
    public String ajustarStock(@RequestParam Long productoId,
                                @RequestParam Integer nuevaCantidad,
                                @RequestParam(required = false) String observaciones,
                                RedirectAttributes redirectAttributes) {
        try {
            productoService.ajustarStock(productoId, nuevaCantidad, observaciones);
            redirectAttributes.addFlashAttribute("mensaje", "Stock ajustado exitosamente");
            redirectAttributes.addFlashAttribute("tipo", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipo", "danger");
        }
        return "redirect:/productos/inventario";
    }

    // ===============================
    // Búsqueda
    // ===============================

    @GetMapping("/buscar")
    @ResponseBody
    public List<Producto> buscarProductos(@RequestParam String q) {
        return productoService.searchByNombre(q);
    }

    @GetMapping("/buscar-codigo")
    @ResponseBody
    public Producto buscarPorCodigo(@RequestParam String codigo) {
        return productoService.findByCodigoBarraOrQr(codigo).orElse(null);
    }
}
