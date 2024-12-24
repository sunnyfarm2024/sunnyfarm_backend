package com.sunny.sunnyfarm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlantDto {
    private int userPlantId;
    private String plantName;
    private String plantType;
    private String growthStage;
    private float progressRatio;
    private int waterLevel;
    private int livesLeft;
    private String plantLocation;
    private String plantImage;
    private LocalDateTime fertilizerEndsAt;
    private String fertilizerType;
}
