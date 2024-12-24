package com.sunny.sunnyfarm.service;

import com.sunny.sunnyfarm.dto.GoogleAuthResult;
import com.sunny.sunnyfarm.dto.UserDto;
import com.sunny.sunnyfarm.dto.UserLoginDto;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {
    boolean checkEmail(String email);
    boolean checkUserName(String userName, int userId);
    CheckResult register(UserLoginDto userLoginDto);
    CheckResult login(UserLoginDto userLoginDto);
    CheckResult updateUserName(int userId, String userName);
    Integer getUserIdByEmail(String email);
    UserDto getUser(Integer userId);
    CheckResult saveProfilePicture(Integer userId, MultipartFile file);
    GoogleAuthResult processGoogleAuth(String code);
    CheckResult saveLocation(Integer userId, Float latitude, Float longitude);
}
