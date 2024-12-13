package com.sunny.sunnyfarm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlantDto {
    private String plantName;
    private String plantType;
    private String growthStage;
    private String growthProgress;
    private int waterLevel;
    private int livesLeft;
    private String plantLocation;
    private String plantImage;
    private String difficulty;
    private LocalDateTime fertilizerEndsAt;
}
