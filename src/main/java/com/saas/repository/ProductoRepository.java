package com.saas.repository;

import com.saas.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    // Buscar por código de barras
    Optional<Producto> findByCodigoBarra(String codigoBarra);

    // Buscar por código QR
    Optional<Producto> findByCodigoQr(String codigoQr);

    // Buscar por código de barras o QR (para el lector)
    @Query("SELECT p FROM Producto p WHERE p.codigoBarra = :codigo OR p.codigoQr = :codigo")
    Optional<Producto> findByCodigoBarraOrQr(@Param("codigo") String codigo);

    // Productos activos
    List<Producto> findByEstaActivoTrue();

    // Productos por categoría
    List<Producto> findByCategoriaIdAndEstaActivoTrue(Long categoriaId);

    // Productos con stock bajo el mínimo
    @Query("SELECT p FROM Producto p WHERE p.stockActual < p.stockMinimo AND p.estaActivo = true")
    List<Producto> findProductosConStockBajo();

    // Productos sin stock
    @Query("SELECT p FROM Producto p WHERE p.stockActual = 0 AND p.estaActivo = true")
    List<Producto> findProductosSinStock();

    // Buscar productos por nombre (para autocompletado)
    @Query("SELECT p FROM Producto p WHERE p.nombre LIKE %:nombre% AND p.estaActivo = true")
    List<Producto> findByNombreContainingIgnoreCase(@Param("nombre") String nombre);

    // Verificar si existe código de barras
    boolean existsByCodigoBarra(String codigoBarra);

    // Verificar si existe código QR
    boolean existsByCodigoQr(String codigoQr);

    // Contar productos activos
    long countByEstaActivoTrue();

    // Productos con stock bajo
    @Query("SELECT COUNT(p) FROM Producto p WHERE p.stockActual < p.stockMinimo AND p.estaActivo = true")
    long countProductosStockBajo();
}
