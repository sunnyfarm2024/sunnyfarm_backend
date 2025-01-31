package com.sunny.sunnyfarm.service;

import com.sunny.sunnyfarm.dto.WeatherDto;

public interface WeatherService {
    WeatherDto getWeather(Integer userId);
    WeatherDto fetchWeather(float lat, float lon);
}
