package com.sunny.sunnyfarm.controller;

import com.sunny.sunnyfarm.dto.TitleDto;
import com.sunny.sunnyfarm.service.TitleService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/title")
@RequiredArgsConstructor
public class TitleController {

    private final TitleService titleService;

    @GetMapping("/list")
    public ResponseEntity<List<TitleDto>> getTitleList(HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        return titleService.getTitleList(userId);
    }

    @PutMapping("/change")
    public ResponseEntity<String> changeTitle(HttpSession session, @RequestParam int titleId) {
        Integer userId = (Integer) session.getAttribute("userId");
        boolean result = titleService.changeTitle(titleId, userId);

        if (result) {
            return ResponseEntity.ok("칭호 변경 성공");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("칭호 변경 실패");
        }
    }

    @PutMapping("/progress")
    public ResponseEntity<String> changeProgress(HttpSession session, @RequestParam int plantId) {
        Integer userId = (Integer) session.getAttribute("userId");
        boolean result = titleService.archiveTitle(plantId, userId);

        if (result) {
            return ResponseEntity.ok("칭호 진행 성공");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("칭호 진행 실패");
        }
    }

}
