package com.sunny.sunnyfarm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class UserPlantDto {
    int userPlantId;
    String plantType;
    String growthStage;
    int waterLevel;
    int livesLeft;
}
