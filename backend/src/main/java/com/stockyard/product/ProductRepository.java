package com.stockyard.product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByMakerIdAndArchivedOrderByTitleAsc(Long makerId, boolean archived);

    Optional<Product> findByIdAndMakerId(Long id, Long makerId);

    boolean existsByMakerIdAndSkuIgnoreCase(Long makerId, String sku);

    @Query("""
            select p from Product p
            where p.maker.id = :makerId and p.archived = false and p.stock <= p.lowStockThreshold
            order by p.stock asc, p.title asc""")
    List<Product> findLowStock(@Param("makerId") Long makerId);

    /** Atomic guarded decrement: succeeds only if enough stock remains. Returns rows updated (0 or 1). */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update Product p set p.stock = p.stock - :qty
            where p.id = :id and p.maker.id = :makerId and p.archived = false and p.stock >= :qty""")
    int decrementStock(@Param("id") Long id, @Param("makerId") Long makerId, @Param("qty") int qty);
}
