package com.saas.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ventas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Venta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_venta", unique = true, length = 20)
    private String numeroVenta;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EstadoVenta estado = EstadoVenta.PENDIENTE;

    @Column(name = "subtotal", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(name = "impuesto", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal impuesto = BigDecimal.ZERO;

    @Column(name = "descuento", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal descuento = BigDecimal.ZERO;

    @Column(name = "total", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal total = BigDecimal.ZERO;

    @Column(name = "metodo_pago", length = 50)
    @Builder.Default
    private String metodoPago = "EFECTIVO";

    @Column(name = "cliente_nombre", length = 150)
    private String clienteNombre;

    @Column(name = "cliente_documento", length = 50)
    private String clienteDocumento;

    @Column(length = 500)
    private String observaciones;

    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private List<DetalleVenta> detalles = new ArrayList<>();

    @Column(name = "fecha_venta")
    private LocalDateTime fechaVenta;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @PrePersist
    protected void onCreate() {
        fechaVenta = LocalDateTime.now();
        fechaActualizacion = LocalDateTime.now();
        if (numeroVenta == null) {
            numeroVenta = "V-" + System.currentTimeMillis();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }

    public void calcularTotal() {
        if (detalles != null && !detalles.isEmpty()) {
            subtotal = detalles.stream()
                    .map(DetalleVenta::getSubtotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            // Calcular impuesto (19% por defecto en Colombia)
            impuesto = subtotal.multiply(new BigDecimal("0.19"));
            
            // Aplicar descuento
            BigDecimal totalBruto = subtotal.add(impuesto);
            total = totalBruto.subtract(descuento != null ? descuento : BigDecimal.ZERO);
        }
    }

    public enum EstadoVenta {
        PENDIENTE,
        COMPLETADA,
        CANCELADA,
        ANULADA
    }
}
