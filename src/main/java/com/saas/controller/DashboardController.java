package com.saas.controller;

import com.saas.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@Controller
@RequestMapping("/")
@RequiredArgsConstructor
@Slf4j
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public String index(Model model) {
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        Map<String, Object> stats = dashboardService.getEstadisticasDashboard();
        model.addAttribute("stats", stats);
        
        model.addAttribute("productosStockBajo", dashboardService.getProductosStockBajo());
        model.addAttribute("ventasRecientes", dashboardService.getVentasRecientes());
        model.addAttribute("movimientosRecientes", dashboardService.getMovimientosRecientes(10));
        
        return "dashboard/index";
    }
}
