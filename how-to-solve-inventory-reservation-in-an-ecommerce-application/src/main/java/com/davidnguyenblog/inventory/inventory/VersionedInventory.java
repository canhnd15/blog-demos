package com.davidnguyenblog.inventory.inventory;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;

// Tro cung bang "inventory" nhu Inventory, nhung map them cot version
// de optimistic lock khong anh huong toi cac cach con lai.
@Entity
@Table(name = "inventory")
@Getter
@Setter
public class VersionedInventory {

    @Id
    private String sku;

    private int available;

    @Version
    private long version;
}
