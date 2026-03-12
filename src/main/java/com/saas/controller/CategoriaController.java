package com.saas.controller;

import com.saas.model.Categoria;
import com.saas.service.ProductoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/categorias")
@RequiredArgsConstructor
@Slf4j
public class CategoriaController {

    private final ProductoService productoService;

    @GetMapping
    public String listarCategorias(Model model) {
        List<Categoria> categorias = productoService.findAllCategorias();
        model.addAttribute("categorias", categorias);
        model.addAttribute("categoria", new Categoria());
        return "categorias/lista";
    }

    @PostMapping("/guardar")
    public String guardarCategoria(@ModelAttribute Categoria categoria, 
                                    RedirectAttributes redirectAttributes) {
        try {
            productoService.saveCategoria(categoria);
            redirectAttributes.addFlashAttribute("mensaje", "Categoría guardada exitosamente");
            redirectAttributes.addFlashAttribute("tipo", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipo", "danger");
        }
        return "redirect:/categorias";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarCategoria(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            productoService.deleteCategoria(id);
            redirectAttributes.addFlashAttribute("mensaje", "Categoría eliminada exitosamente");
            redirectAttributes.addFlashAttribute("tipo", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipo", "danger");
        }
        return "redirect:/categorias";
    }
}
