package com.sunny.sunnyfarm.controller;

import com.sunny.sunnyfarm.dto.PlantbookDto;
import com.sunny.sunnyfarm.service.PlantService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/plant")
public class PlantController {
    private final PlantService plantService;

    public PlantController(PlantService plantService) {
        this.plantService = plantService;
    }

    @GetMapping("/book")
    ResponseEntity<List<PlantbookDto>> getPlantBook(HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");

        List<PlantbookDto> plantBooks = plantService.getPlantBook(userId);
        return ResponseEntity.ok(plantBooks);
    }

    @PostMapping("/water")
    ResponseEntity<String> waterPlant(HttpSession session, @RequestParam int userPlantId) {
        Integer userId = (Integer) session.getAttribute("userId");

        return plantService.waterPlant(userId, userPlantId);
    }

    @PostMapping("/sell")
    ResponseEntity<String> sellPlant(HttpSession session, @RequestParam int userPlantId) {
        Integer userId = (Integer) session.getAttribute("userId");

        return plantService.sellPlant(userId, userPlantId);
    }

    @DeleteMapping("/delete")
    ResponseEntity<String> deletePlant(@RequestParam int userPlantId) {
        if (plantService.deletePlant(userPlantId)) {
            return ResponseEntity.ok("식물을 버렸습니다.");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("식물을 버리지 못했습니다.");
        }
    }

    @PostMapping("/growth")
    ResponseEntity<String> updateGrowthStage(@RequestParam int userPlantId) {
        plantService.updateGrowthStage(userPlantId);
        return null;
    }
}