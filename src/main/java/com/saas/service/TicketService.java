package com.saas.service;

import com.saas.config.PrinterConfig;
import com.saas.model.*;
import com.saas.repository.VentaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final VentaRepository ventaRepository;
    private final PrinterConfig printerConfig;

    /**
     * Genera un tiquete simple para pruebas (sin conexión a impresora)
     * Retorna una cadena de texto formateada para visualización
     */
    public String generarTiqueteTexto(Long ventaId) {
        Venta venta = ventaRepository.findById(ventaId)
                .orElseThrow(() -> new IllegalArgumentException("Venta no encontrada: " + ventaId));
        
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        
        StringBuilder sb = new StringBuilder();
        
        // Encabezado
        sb.append("================================\n");
        sb.append("      *** TIQUETE DE CAJA ***\n");
        sb.append("================================\n");
        sb.append(printerConfig.getStoreName()).append("\n");
        sb.append("NIT: ").append(printerConfig.getStoreNit()).append("\n");
        sb.append("================================\n");
        
        // Info venta
        sb.append("Fecha: ").append(venta.getFechaVenta().format(dateFormatter)).append("\n");
        sb.append("Venta #: ").append(venta.getNumeroVenta()).append("\n");
        
        if (venta.getClienteNombre() != null && !venta.getClienteNombre().isEmpty()) {
            sb.append("Cliente: ").append(venta.getClienteNombre()).append("\n");
        }
        
        if (venta.getClienteDocumento() != null && !venta.getClienteDocumento().isEmpty()) {
            sb.append("Doc: ").append(venta.getClienteDocumento()).append("\n");
        }
        
        sb.append("================================\n");
        
        // Productos
        sb.append("CANT  PRODUCTO              SUBTOTAL\n");
        sb.append("--------------------------------\n");
        
        List<DetalleVenta> detalles = venta.getDetalles();
        for (DetalleVenta detalle : detalles) {
            String nombre = detalle.getProducto().getNombre();
            if (nombre.length() > 20) {
                nombre = nombre.substring(0, 17) + "...";
            }
            sb.append(String.format("%-5d %-20s %10s\n", 
                    detalle.getCantidad(),
                    nombre,
                    formatter.format(detalle.getSubtotal())));
            
            // Mostrar descuento si existe
            if (detalle.getDescuento() != null && detalle.getDescuento().doubleValue() > 0) {
                sb.append(String.format("    Desc: -%s\n", formatter.format(detalle.getDescuento())));
            }
            
            // Precio unitario
            sb.append(String.format("    x %s c/u\n", formatter.format(detalle.getPrecioUnitario())));
        }
        
        sb.append("================================\n");
        
        // Totales
        sb.append(String.format("%-25s %10s\n", "SUBTOTAL:", formatter.format(venta.getSubtotal())));
        
        if (venta.getDescuento() != null && venta.getDescuento().doubleValue() > 0) {
            sb.append(String.format("%-25s %10s\n", "DESCUENTO:", "-" + formatter.format(venta.getDescuento())));
        }
        
        if (venta.getImpuesto() != null && venta.getImpuesto().doubleValue() > 0) {
            sb.append(String.format("%-25s %10s\n", "IVA (19%):", formatter.format(venta.getImpuesto())));
        }
        
        sb.append("--------------------------------\n");
        sb.append(String.format("%-25s %10s\n", "TOTAL:", formatter.format(venta.getTotal())));
        sb.append("================================\n");
        
        if (venta.getMetodoPago() != null) {
            sb.append("Método de pago: ").append(venta.getMetodoPago()).append("\n");
        }
        
        // Pie
        sb.append("\n");
        sb.append("     ¡Gracias por su compra!\n");
        sb.append("        Vuelva pronto\n");
        sb.append("\n");
        sb.append("================================\n");
        sb.append("Documento soporte POS\n");
        sb.append("No válido como factura electrónica\n");
        sb.append("================================\n");
        
        return sb.toString();
    }
    
    /**
     * Genera los bytes del tiquete para descargar
     */
    public byte[] generarTiqueteBytes(Long ventaId) throws IOException {
        String tiqueteTexto = generarTiqueteTexto(ventaId);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(tiqueteTexto.getBytes("UTF-8"));
        return outputStream.toByteArray();
    }
    
    /**
     * Envía el tiquete a la impresora térmica (requiere configuración)
     * Por ahora retorna el texto del tiquete
     */
    public String imprimirTiquete(Long ventaId) {
        String tiquete = generarTiqueteTexto(ventaId);
        // En una implementación real, aquí se enviaría a la impresora
        // usando sockets TCP/IP al puerto 9100 (ESC/POS)
        // Por ejemplo:
        // Socket socket = new Socket(printerConfig.getAddress(), printerConfig.getPort());
        // OutputStream out = socket.getOutputStream();
        // out.write(tiquete.getBytes("UTF-8"));
        return "Tiquete generado. Configura la IP de la impresora en application.properties";
    }
    
    /**
     * Obtiene la configuración de la impresora
     */
    public PrinterConfig getPrinterConfig() {
        return printerConfig;
    }
}
