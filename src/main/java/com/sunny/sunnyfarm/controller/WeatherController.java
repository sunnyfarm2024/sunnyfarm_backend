package com.sunny.sunnyfarm.controller;

import com.sunny.sunnyfarm.dto.WeatherDto;
import com.sunny.sunnyfarm.service.impl.WeatherServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/weather")
@RequiredArgsConstructor
public class WeatherController {

    private final WeatherServiceImpl weatherService;

    // 날씨 정보 테스트용 엔드포인트
    @GetMapping("/test")
    public WeatherDto getWeather(@RequestParam("userId") int userId) {
        // 날씨 데이터를 가져와서 반환
        return weatherService.getWeather(userId);
    }
}
