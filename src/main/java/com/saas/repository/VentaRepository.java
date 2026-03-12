package com.saas.repository;

import com.saas.model.Venta;
import com.saas.model.Venta.EstadoVenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {

    Optional<Venta> findByNumeroVenta(String numeroVenta);

    List<Venta> findByEstado(EstadoVenta estado);

    List<Venta> findByFechaVentaBetweenOrderByFechaVentaDesc(LocalDateTime fechaInicio, LocalDateTime fechaFin);

    @Query("SELECT SUM(v.total) FROM Venta v WHERE v.fechaVenta BETWEEN :fechaInicio AND :fechaFin AND v.estado = :estado")
    java.math.BigDecimal sumTotalByFechaVentaBetweenAndEstado(
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin,
            @Param("estado") EstadoVenta estado);

    @Query("SELECT COUNT(v) FROM Venta v WHERE v.fechaVenta >= :fecha AND v.estado = :estado")
    long countByFechaVentaAfterAndEstado(@Param("fecha") LocalDateTime fecha, @Param("estado") EstadoVenta estado);

    @Query("SELECT v FROM Venta v ORDER BY v.fechaVenta DESC")
    List<Venta> findTop10ByOrderByFechaVentaDesc();

    @Query("SELECT v FROM Venta v WHERE v.estado = 'COMPLETADA' AND v.fechaVenta >= :fecha ORDER BY v.fechaVenta DESC")
    List<Venta> findVentasCompletadasRecientes(@Param("fecha") LocalDateTime fecha);
}
