package com.sunny.sunnyfarm.dto;

import com.sunny.sunnyfarm.entity.Shop;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InventoryDto {
    private int slotNumber;
    private String inventoryItemImg;
    private String inventoryItemName;
    private String inventoryItemDescription;
    private Shop.ItemCategory inventoryItemCategory;
    private boolean inventoryItemStatus;
    private LocalDateTime leftFertilizerAt;
    private LocalDateTime centerFertilizerAt;
    private LocalDateTime rightFertilizerAt;
    private LocalDateTime gnomeAt;
}
