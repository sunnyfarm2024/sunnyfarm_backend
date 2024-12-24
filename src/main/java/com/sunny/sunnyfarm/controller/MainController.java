package com.sunny.sunnyfarm.controller;

import com.sunny.sunnyfarm.dto.MainDto;
import com.sunny.sunnyfarm.service.MainService;
import com.sunny.sunnyfarm.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/main")
@RequiredArgsConstructor
public class MainController {

    private final MainService mainService;
    private final UserService userService;

    @GetMapping("/info")
    public ResponseEntity<MainDto> getMainData(HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");

        MainDto mainData = mainService.getMainData(userId);
        return ResponseEntity.ok(mainData);
    }

    @GetMapping("/friend-info")
    public ResponseEntity<MainDto> getFriendMainData(@RequestParam Integer friendId) {
        MainDto mainData = mainService.getMainData(friendId);
        return ResponseEntity.ok(mainData);
    }

}
