package com.sunny.sunnyfarm.controller;

import com.sunny.sunnyfarm.dto.FarmDto;
import com.sunny.sunnyfarm.service.CheckResult;
import com.sunny.sunnyfarm.service.FarmService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/farm")
@RequiredArgsConstructor
public class FarmController {

    private final FarmService farmService;

    @PostMapping("/farm-description")
    public ResponseEntity<CheckResult> updateFarmDescription(@RequestBody FarmDto farmDto, HttpSession session) {

        Integer userId = (Integer) session.getAttribute("userId"); // 세션에서 userId 가져오기

        String farmDescription = farmDto.getFarmDescription();
        CheckResult result = farmService.updateFarmDescription(userId, farmDescription);

        return switch (result) {
            case SUCCESS -> ResponseEntity.ok(CheckResult.SUCCESS);
            default -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(CheckResult.FAIL);
        };
    }
}
