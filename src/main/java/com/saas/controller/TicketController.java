package com.saas.controller;

import com.saas.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    /**
     * Muestra la vista previa del tiquete en texto
     */
    @GetMapping("/ventas/{id}/tiquete")
    public String verTiquete(@PathVariable Long id, org.springframework.ui.Model model) {
        try {
            String tiqueteTexto = ticketService.generarTiqueteTexto(id);
            model.addAttribute("tiquete", tiqueteTexto);
            model.addAttribute("ventaId", id);
            return "ventas/tiquete";
        } catch (Exception e) {
            model.addAttribute("error", "Error al generar tiquete: " + e.getMessage());
            return "error";
        }
    }

    /**
     * Descarga el tiquete como archivo de texto
     */
    @GetMapping("/api/ventas/{id}/tiquete/descargar")
    public ResponseEntity<byte[]> descargarTiquete(@PathVariable Long id) {
        try {
            byte[] bytes = ticketService.generarTiqueteBytes(id);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("text/plain; charset=UTF-8"));
            headers.setContentDispositionFormData("attachment", "tiquete_" + id + ".txt");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(bytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Imprime directamente el tiquete (requiere impresora configurada)
     */
    @PostMapping("/api/ventas/{id}/tiquete/imprimir")
    @ResponseBody
    public ResponseEntity<String> imprimirTiquete(@PathVariable Long id) {
        try {
            String resultado = ticketService.imprimirTiquete(id);
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Error al imprimir: " + e.getMessage());
        }
    }

    /**
     * Endpoint para obtener tiquete en texto (para APIs o previsualización)
     */
    @GetMapping("/api/ventas/{id}/tiquete")
    @ResponseBody
    public ResponseEntity<String> obtenerTiquete(@PathVariable Long id) {
        try {
            String tiqueteTexto = ticketService.generarTiqueteTexto(id);
            return ResponseEntity.ok(tiqueteTexto);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Error: " + e.getMessage());
        }
    }
}
