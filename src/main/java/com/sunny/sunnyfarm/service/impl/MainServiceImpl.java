package com.sunny.sunnyfarm.service.impl;


import com.sunny.sunnyfarm.dto.*;
import com.sunny.sunnyfarm.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MainServiceImpl implements MainService {

    private final UserServiceImpl userServiceImpl;
    private final WeatherServiceImpl weatherServiceImpl;
    private final FarmServiceImpl farmServiceImpl;
    private final PlantServiceImpl plantServiceImpl;

    public MainDto getMainData(Integer userId) {

        UserDto userDto = userServiceImpl.getUser(userId);
        WeatherDto weatherDto = weatherServiceImpl.getWeather(userId);
        FarmDto farmDto = farmServiceImpl.getFarm(userId);
        List<PlantDto> plantDtos = plantServiceImpl.getPlant(userId);

        return new MainDto(userDto, weatherDto, farmDto, plantDtos);
    }
}
