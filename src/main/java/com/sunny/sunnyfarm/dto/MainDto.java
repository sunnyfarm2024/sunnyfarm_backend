package com.sunny.sunnyfarm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MainDto {
    private UserDto user;
    private WeatherDto weather;
    private FarmDto farm;
    private List<PlantDto> plants;
}
