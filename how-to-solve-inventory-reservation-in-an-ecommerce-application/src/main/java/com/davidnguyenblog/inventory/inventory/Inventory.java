package com.davidnguyenblog.inventory.inventory;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "inventory")
@Getter
@Setter
public class Inventory {

    @Id
    private String sku;

    private int available;

    // Co y KHONG co @Version o entity dung chung nay.
    // Optimistic lock dung mot entity rieng: xem VersionedInventory.
}
