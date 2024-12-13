package com.sunny.sunnyfarm.service;

import com.sunny.sunnyfarm.dto.InventoryDto;

import java.util.List;

public interface InventoryService {
    List<InventoryDto> getInventory(Integer userId);
    CheckResult useItem(Integer userId, int slotNumber, String location, String plantName);
    void addItem(Integer userId, Integer itemId);
    CheckResult deleteItem(Integer userId, int slotNumber);
}
