package com.saas.repository;

import com.saas.model.Movimiento;
import com.saas.model.Movimiento.TipoMovimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {

    List<Movimiento> findByProductoIdOrderByFechaMovimientoDesc(Long productoId);

    List<Movimiento> findByTipoMovimiento(TipoMovimiento tipoMovimiento);

    @Query("SELECT m FROM Movimiento m WHERE m.fechaMovimiento BETWEEN :fechaInicio AND :fechaFin ORDER BY m.fechaMovimiento DESC")
    List<Movimiento> findByFechaMovimientoBetween(@Param("fechaInicio") LocalDateTime fechaInicio, 
                                                    @Param("fechaFin") LocalDateTime fechaFin);

    @Query("SELECT m FROM Movimiento m WHERE m.producto.id = :productoId AND m.fechaMovimiento BETWEEN :fechaInicio AND :fechaFin ORDER BY m.fechaMovimiento DESC")
    List<Movimiento> findByProductoIdAndFechaMovimientoBetween(@Param("productoId") Long productoId,
                                                                  @Param("fechaInicio") LocalDateTime fechaInicio,
                                                                  @Param("fechaFin") LocalDateTime fechaFin);

    @Query("SELECT SUM(m.cantidad) FROM Movimiento m WHERE m.producto.id = :productoId AND m.tipoMovimiento = :tipo")
    Integer sumCantidadByProductoIdAndTipo(@Param("productoId") Long productoId, @Param("tipo") TipoMovimiento tipo);

    @Query("SELECT COUNT(m) FROM Movimiento m WHERE m.fechaMovimiento >= :fecha")
    long countMovimientosRecientes(@Param("fecha") LocalDateTime fecha);
}
