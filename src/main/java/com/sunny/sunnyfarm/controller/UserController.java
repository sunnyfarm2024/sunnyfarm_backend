package com.sunny.sunnyfarm.controller;

import com.sunny.sunnyfarm.dto.GoogleAuthResult;
import com.sunny.sunnyfarm.dto.LocationDto;
import com.sunny.sunnyfarm.dto.UserDto;
import com.sunny.sunnyfarm.dto.UserLoginDto;
import com.sunny.sunnyfarm.service.CheckResult;
import com.sunny.sunnyfarm.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/check-email")
    public ResponseEntity<Boolean> checkEmail(@RequestParam String email) {
        boolean exists = userService.checkEmail(email);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/check-username")
    public ResponseEntity<Boolean> checkUserName(@RequestParam String userName, HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId"); // 세션에서 userId 가져오기
        boolean exists = userService.checkUserName(userName, userId);
        System.out.println(exists);
        return ResponseEntity.ok(exists);
    }

    @PostMapping("/register")
    public ResponseEntity<CheckResult> register(@RequestBody UserLoginDto userLoginDto) {
        CheckResult result = userService.register(userLoginDto);

        return switch (result) {
            case SUCCESS -> ResponseEntity.ok(CheckResult.SUCCESS);
            case DUPLICATE -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body(CheckResult.DUPLICATE);
            default -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(CheckResult.FAIL);
        };
    }

    @PostMapping("/login")
    public ResponseEntity<CheckResult> login(@RequestBody UserLoginDto userLoginDto, HttpSession session) {
        CheckResult result = userService.login(userLoginDto);

        if (result == CheckResult.SUCCESS) {
            // 세션에 userId 저장
            Integer userId = userService.getUserIdByEmail(userLoginDto.getEmail());
            session.setAttribute("userId", userId);

            return ResponseEntity.ok(CheckResult.SUCCESS);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(CheckResult.FAIL);
        }
    }

    // Google 로그인 처리
    @GetMapping("/google-login")
    public void handleGoogleLogin(@RequestParam("code") String code, HttpSession session, HttpServletResponse response) {
        try {
            // Google 인증 처리
            GoogleAuthResult result = userService.processGoogleAuth(code);

            if (result.getResult() == CheckResult.SUCCESS) {
                Integer userId = userService.getUserIdByEmail(result.getEmail());
                session.setAttribute("userId", userId);

                // 성공 시 메인 페이지로 리디렉션
                response.sendRedirect("http://localhost:3000/location");
            } else {
                // 실패 시 로그인 페이지로 리디렉션
                response.sendRedirect("http://localhost:3000/login?error=google-login-failed");
            }
        } catch (Exception e) {
            // 예외 발생 시 로그인 페이지로 리디렉션
            try {
                response.sendRedirect("http://localhost:3000/login?error=unexpected-error");
            } catch (IOException ioException) {
                throw new RuntimeException("리디렉션 중 오류 발생", ioException);
            }
        }
    }

    @PostMapping("/username")
    public ResponseEntity<CheckResult>  updateUserName(@RequestBody UserDto userDto, HttpSession session) {

        Integer userId = (Integer) session.getAttribute("userId"); // 세션에서 userId 가져오기

        String username = userDto.getUserName();
        CheckResult result = userService.updateUserName(userId, username);

        return switch (result) {
            case SUCCESS -> ResponseEntity.ok(CheckResult.SUCCESS);
            case DUPLICATE -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body(CheckResult.DUPLICATE);
            default -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(CheckResult.FAIL);
        };
    }

    @PostMapping("/profile-picture")
    public ResponseEntity<Map<String, String>> updateProfilePicture(@RequestParam("file") MultipartFile file, HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");

        CheckResult result = userService.saveProfilePicture(userId, file);

        if (result == CheckResult.SUCCESS) {
            // 이미지가 성공적으로 저장된 경우만 경로 반환
            String newProfilePath = "/uploads/profile" + userId + ".png";
            Map<String, String> response = new HashMap<>();
            response.put("newProfilePath", newProfilePath);
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    @PostMapping("/save-location")
    public ResponseEntity<CheckResult> saveLocation(@RequestBody LocationDto locationDto, HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");

        CheckResult result = userService.saveLocation(userId, locationDto.getLatitude(), locationDto.getLongitude());
        return switch (result) {
            case SUCCESS -> ResponseEntity.ok(CheckResult.SUCCESS);
            default -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(CheckResult.FAIL);
        };
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpSession session) {
        session.invalidate(); // 세션 무효화

        return ResponseEntity.ok("로그아웃 되었습니다.");
    }
}
