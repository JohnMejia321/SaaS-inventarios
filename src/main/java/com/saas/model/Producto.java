package com.saas.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "productos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(name = "codigo_barra", unique = true, length = 50)
    private String codigoBarra;

    @Column(name = "codigo_qr", unique = true, length = 255)
    private String codigoQr;

    @Column(length = 500)
    private String descripcion;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    @Column(name = "costo", precision = 10, scale = 2)
    private BigDecimal costo;

    @Column(name = "stock_actual")
    @Builder.Default
    private Integer stockActual = 0;

    @Column(name = "stock_minimo")
    @Builder.Default
    private Integer stockMinimo = 0;

    @Column(name = "stock_maximo")
    @Builder.Default
    private Integer stockMaximo = 100;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    @Column(name = "esta_activo")
    @Builder.Default
    private Boolean estaActivo = true;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Movimiento> movimientos = null;

    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<DetalleVenta> detallesVenta = null;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        fechaActualizacion = LocalDateTime.now();
        if (stockActual == null) stockActual = 0;
        if (stockMinimo == null) stockMinimo = 0;
        if (stockMaximo == null) stockMaximo = 100;
        if (estaActivo == null) estaActivo = true;
    }

    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }

    public Producto(String nombre, String codigoBarra, BigDecimal precio) {
        this.nombre = nombre;
        this.codigoBarra = codigoBarra;
        this.precio = precio;
        this.estaActivo = true;
    }

    public boolean tieneStockSuficiente(Integer cantidad) {
        return stockActual != null && stockActual >= cantidad;
    }

    public boolean estaPorDebajoDelMinimo() {
        return stockActual != null && stockMinimo != null && stockActual < stockMinimo;
    }
}
