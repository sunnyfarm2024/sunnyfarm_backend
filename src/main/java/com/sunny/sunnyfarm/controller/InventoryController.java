package com.sunny.sunnyfarm.controller;

import com.sunny.sunnyfarm.dto.InventoryDto;
import com.sunny.sunnyfarm.service.CheckResult;
import com.sunny.sunnyfarm.service.InventoryService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping("/info")
    public ResponseEntity<List<InventoryDto>> getInventoryItem(HttpSession session){
        Integer userId = (Integer) session.getAttribute("userId");

        List<InventoryDto> inventoryItems = inventoryService.getInventory(userId);

        return ResponseEntity.ok(inventoryItems);
    }

    @PostMapping("/use")
    public ResponseEntity<CheckResult> useInventoryItem(@RequestParam int slotNumber,
                                                        @RequestParam String location,
                                                        @RequestParam String plantName,
                                                        HttpSession session){
        Integer userId = (Integer) session.getAttribute("userId");

        CheckResult result = inventoryService.useItem(userId, slotNumber, location, plantName);

        return switch (result) {
            case SUCCESS -> ResponseEntity.ok(CheckResult.SUCCESS);
            default -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(CheckResult.FAIL);
        };
    }

    @PostMapping("/delete")
    public ResponseEntity<CheckResult> deleteInventoryItem(@RequestParam int slotNumber, HttpSession session){
        Integer userId = (Integer) session.getAttribute("userId");

        CheckResult result = inventoryService.deleteItem(userId, slotNumber);

        return switch (result) {
            case SUCCESS -> ResponseEntity.ok(CheckResult.SUCCESS);
            default -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(CheckResult.FAIL);
        };
    }
}
