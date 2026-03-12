package com.saas.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "printer.thermal")
@Data
public class PrinterConfig {
    
    /**
     * Dirección IP de la impresora térmica
     */
    private String address = "192.168.1.100";
    
    /**
     * Puerto de la impresora (9100 es el estándar para ESC/POS)
     */
    private int port = 9100;
    
    /**
     * Ancho del papel en caracteres (48 para 58mm, ~64 para 80mm)
     */
    private int paperWidth = 48;
    
    /**
     * Nombre de la tienda
     */
    private String storeName = "Mi Tienda";
    
    /**
     * NIT de la tienda
     */
    private String storeNit = "123456789-0";
    
    /**
     * Dirección de la tienda
     */
    private String storeAddress = "Calle Principal #1";
    
    /**
     * Teléfono de la tienda
     */
    private String storePhone = "300 1234567";
}
