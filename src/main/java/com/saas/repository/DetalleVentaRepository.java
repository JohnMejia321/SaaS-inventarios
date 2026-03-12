package com.saas.repository;

import com.saas.model.DetalleVenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DetalleVentaRepository extends JpaRepository<DetalleVenta, Long> {

    List<DetalleVenta> findByVentaId(Long ventaId);

    List<DetalleVenta> findByProductoId(Long productoId);

    @Query("SELECT SUM(d.cantidad) FROM DetalleVenta d WHERE d.producto.id = :productoId")
    Integer sumCantidadVendidaByProductoId(@Param("productoId") Long productoId);

    @Query("SELECT d.producto.id, SUM(d.cantidad) FROM DetalleVenta d GROUP BY d.producto.id ORDER BY SUM(d.cantidad) DESC")
    List<Object[]> findProductosMasVendidos();
}
