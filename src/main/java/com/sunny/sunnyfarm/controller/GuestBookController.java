package com.sunny.sunnyfarm.controller;

import com.sunny.sunnyfarm.dto.GuestbookDto;
import com.sunny.sunnyfarm.service.GuestBookService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/guestbook")
public class GuestBookController {
    private final GuestBookService guestbookService;

    public GuestBookController(GuestBookService guestbookService) {
        this.guestbookService = guestbookService;
    }

    @GetMapping("/list")
    ResponseEntity<List<GuestbookDto>> getGuestbook (@RequestParam(required = false) Integer friendId, HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        List<GuestbookDto> guestbookList;

        if (friendId != null) {
            // friendId에 대한 방명록 가져오기
            guestbookList = guestbookService.getGuestbook(friendId);
        } else {
            // 로그인한 사용자의 방명록 가져오기
            guestbookList = guestbookService.getGuestbook(userId);
        }

        return ResponseEntity.ok(guestbookList);
    }


    @PostMapping("/write")
    ResponseEntity<String> writeGuestbook(HttpSession session, @RequestParam int friendUserId, @RequestParam String content) {
        Integer userId = (Integer) session.getAttribute("userId");
        try {
            guestbookService.writeGuestbook(userId, friendUserId, content);
            return ResponseEntity.ok("방명록을 작성했습니다.");
        } catch (Exception e) {
            System.out.println(e);
            return ResponseEntity.status(404).body("방명록 작성에 실패했습니다.");
        }
    }

    @PostMapping("/check")
    ResponseEntity<String> checkGuestbook(HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");

        try {
            guestbookService.checkRead(userId);
            return ResponseEntity.ok("방명록을 읽었습니다.");
        } catch (Exception e) {
            System.out.println(e);
            return ResponseEntity.status(404).body("방명록 읽기에 실패했습니다.");
        }
    }
}
