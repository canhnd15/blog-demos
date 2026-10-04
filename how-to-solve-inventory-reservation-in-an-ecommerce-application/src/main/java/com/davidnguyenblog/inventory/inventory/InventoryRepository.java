package com.davidnguyenblog.inventory.inventory;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

public interface InventoryRepository extends JpaRepository<Inventory, String> {

    // Tra ve so dong bi anh huong: 1 = tru thanh cong, 0 = khong du hang
    @Modifying
    @Query("""
        UPDATE Inventory i
           SET i.available = i.available - :qty
         WHERE i.sku = :sku
           AND i.available >= :qty
        """)
    int decreaseIfEnough(@Param("sku") String sku, @Param("qty") int qty);

    @Modifying
    @Query("UPDATE Inventory i SET i.available = i.available + :qty WHERE i.sku = :sku")
    int increase(@Param("sku") String sku, @Param("qty") int qty);

    // Sinh ra SELECT ... FOR UPDATE, cac transaction khac phai cho
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Inventory i WHERE i.sku = :sku")
    Optional<Inventory> findBySkuForUpdate(@Param("sku") String sku);
}
