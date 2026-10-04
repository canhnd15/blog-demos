package com.davidnguyenblog.inventory.inventory;

import org.springframework.data.jpa.repository.JpaRepository;

public interface VersionedInventoryRepository extends JpaRepository<VersionedInventory, String> {
}
