package com.sunny.sunnyfarm.controller;

import com.sunny.sunnyfarm.dto.LocationDto;
import com.sunny.sunnyfarm.dto.MainDto;
import com.sunny.sunnyfarm.service.CheckResult;
import com.sunny.sunnyfarm.service.MainService;
import com.sunny.sunnyfarm.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/main")
@RequiredArgsConstructor
public class MainController {

    private final MainService mainService;
    private final UserService userService;

    @PostMapping("/info")
    public ResponseEntity<MainDto> getMainData(@RequestBody LocationDto locationDto, HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");

        CheckResult result = userService.saveLocation(userId, locationDto.getLatitude(), locationDto.getLongitude());
        if (result == CheckResult.SUCCESS) {
            MainDto mainData = mainService.getMainData(userId);
            return ResponseEntity.ok(mainData);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}
