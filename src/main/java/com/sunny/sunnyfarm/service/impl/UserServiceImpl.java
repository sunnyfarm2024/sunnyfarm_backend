package com.sunny.sunnyfarm.service.impl;

import com.sunny.sunnyfarm.dto.UserDto;
import com.sunny.sunnyfarm.dto.UserLoginDto;
import com.sunny.sunnyfarm.entity.*;
import com.sunny.sunnyfarm.repository.*;
import com.sunny.sunnyfarm.service.CheckResult;
import com.sunny.sunnyfarm.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final FarmRepository farmRepository;
    private final InventoryRepository inventoryRepository;
    private final UserQuestRepository userQuestRepository;
    private final QuestRepository questRepository;
    private final TitleRepository titleRepository;
    private final UserTitleRepository userTitleRepository;
    private final ShopRepository shopRepository;
    private static final String UPLOAD_DIR = "/Applications/sunnyfarm/src/main/resources/static/uploads/";

    // user_id
    public Integer getUserIdByEmail(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("이메일을 찾을 수 없습니다."));
        return user.getUserId();
    }

    // User Dto
    public UserDto getUser(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. ID: " + userId));

        String title = null;
        if (user.getSelectedTitle() != null && user.getSelectedTitle().getTitle() != null) {
            title = titleRepository.findTitleNameById(user.getSelectedTitle().getTitle().getTitleId())
                    .orElse(null);
        }

        return new UserDto(
                user.getUserName(),
                user.getProfilePicture(),
                title,
                user.getWaterBalance(),
                user.getCoinBalance(),
                user.getDiamondBalance()
        );
    }

    // 이메일 중복 체크
    public boolean checkEmail(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    // 닉네임 중복 체크
    public boolean checkUserName(String userName) {
        return userRepository.findByUserName(userName)
                .filter(user -> !user.getUserName().equals(userName)) // 현재 사용자의 ID 제외
                .isPresent();
    }

    // 비밀번호 암호화
    private String encryptPassword(String password) {
        return passwordEncoder.encode(password);
    }

    // 회원가입
    public CheckResult register(UserLoginDto userLoginDto) {
        if (checkEmail(userLoginDto.getEmail())) {
            return CheckResult.DUPLICATE;
        }

        String encryptedPassword = encryptPassword(userLoginDto.getPassword());

        // 기본 데이터 추가 (팜, 인벤토리, 유저퀘스트, 유저 타이틀)
        User newUser = new User();
        newUser.setEmail(userLoginDto.getEmail());
        newUser.setPassword(encryptedPassword);

        User savedUser = userRepository.save(newUser);

        // 프로필 사진 경로 설정
        String profilePicturePath = "/uploads/profile" + savedUser.getUserId() + ".png";
        savedUser.setProfilePicture(profilePicturePath);
        userRepository.save(savedUser);

        // 기본 프로필 이미지 복사
        Path sourcePath = Paths.get("/Applications/sunnyfarm/src/main/resources/static/image/profile/profile.png");
        Path targetPath = Paths.get("/Applications/sunnyfarm/src/main/resources/static/uploads/profile" + savedUser.getUserId() + ".png");

        try {
            Files.createDirectories(targetPath.getParent()); // 대상 디렉토리 생성
            Files.copy(sourcePath, targetPath); // 파일 복사
        } catch (IOException e) {
            throw new RuntimeException("프로필 이미지 복사 중 오류가 발생했습니다.", e);
        }

        Shop sign = shopRepository.findById(12).orElse(null);

        Farm newFarm = new Farm();
        newFarm.setUser(savedUser);
        newFarm.setSign(sign);
        farmRepository.save(newFarm);

        for (int i = 1; i <= 15; i++) {
            Inventory inventorySlot = new Inventory();
            inventorySlot.setUser(savedUser);
            inventorySlot.setSlotNumber(i);
            inventoryRepository.save(inventorySlot);
        }

        for (int i = 1; i <= 12; i++) {
            final int questId = i;

            Quest quest = questRepository.findById(i)
                    .orElseThrow(() -> new IllegalArgumentException("QuestID:" + questId + "를 찾을 수 없습니다."));

            UserQuest userQuest = new UserQuest();
            userQuest.setUser(savedUser);
            userQuest.setQuest(quest);
            userQuestRepository.save(userQuest);
        }

        for (int i = 1; i <= 11; i++) {

            final int titleId = i;

            Title title = titleRepository.findById(i)
                    .orElseThrow(() -> new IllegalArgumentException("TitleID:" + titleId + "를 찾을 수 없습니다."));

            UserTitle userTitle = new UserTitle();
            userTitle.setUser(savedUser);
            userTitle.setTitle(title);
            userTitleRepository.save(userTitle);
        }

        // 기본 칭호 설정
        // is_title_completed, is_active 활성화
        Integer userId = savedUser.getUserId();
        Integer titleId = 8;  // 타이틀 ID는 8로 고정
        userTitleRepository.updateTitleStatus(userId, titleId);

        // 유저 칭호 설정
        UserTitle userTitle = userTitleRepository.findByUserIdAndTitleId(userId, titleId)
                .orElseThrow(() -> new IllegalArgumentException("해당 타이틀을 찾을 수 없습니다."));

        userRepository.updateSelectedTitleId(userId, userTitle);

        return CheckResult.SUCCESS;
    }

    // 로그인
    public CheckResult login(UserLoginDto userLoginDto) {
        User user = userRepository.findByEmail(userLoginDto.getEmail()).orElse(null);

        if (user != null && passwordEncoder.matches(userLoginDto.getPassword(), user.getPassword())) {

            // 위치 정보 저장
            Float latitude = userLoginDto.getLatitude();
            Float longitude = userLoginDto.getLongitude();
            saveLocation(user, latitude, longitude);

            return CheckResult.SUCCESS;
            }

        return CheckResult.FAIL;
    }

    // 위치 정보 저장
    private void saveLocation(User user, Float latitude, Float longitude) {
        if (latitude != null && longitude != null) {
            user.setLatitude(latitude);
            user.setLongitude(longitude);
            userRepository.save(user);
        }
    }

    // 닉네임 수정
    public CheckResult updateUserName(int userId, String userName) {
        if(checkUserName(userName)){
            return CheckResult.DUPLICATE;
        }

        User user = userRepository.findById(userId).orElse(null);
        if (user != null){
            user.setUserName(userName);
            userRepository.save(user);
            return CheckResult.SUCCESS;
        }

        return CheckResult.FAIL;
    }

    // 프로필 사진 수정
    public CheckResult saveProfilePicture(Integer userId, MultipartFile file){
        try {
            // 원본 파일 확장자 추출
            String originalFilename = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            // 파일 이름 변경: profile{userId}.{확장자}
            String newFileName = "profile" + userId + fileExtension;

            // 저장 경로 생성
            Path filePath = Paths.get(UPLOAD_DIR, newFileName);

            // 기존 파일 삭제
            if (Files.exists(filePath)) {
                Files.delete(filePath); // 파일 삭제
            }

            // 파일 저장
            file.transferTo(filePath.toFile());

            return CheckResult.SUCCESS;
        } catch (IOException e) {
            return CheckResult.FAIL;
        }
    }

//    // 구글로 로그인
//    public User googleLogin(String email) {
//        if (checkEmail(email)) {
//            return userRepository.findByEmail(email).orElse(null);
//        } else {
//            UserLoginDto userLoginDto = new UserLoginDto();
//            userLoginDto.setEmail(email);
//            userLoginDto.setPassword(generateRandomPassword()); // 랜덤 비밀번호 설정
//
//            register(userLoginDto);
//
//            // 새로 생성된 사용자 반환
//            return userRepository.findByEmail(email).orElse(null);
//        }
//    }
//
//    // 랜덤 비밀번호 생성
//    private String generateRandomPassword() {
//        int length = 12; // 비밀번호 길이
//        String charPool = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%";
//        StringBuilder password = new StringBuilder();
//
//        Random random = new Random();
//        for (int i = 0; i < length; i++) {
//            int index = random.nextInt(charPool.length());
//            password.append(charPool.charAt(index));
//        }
//
//        return password.toString();
//    }

}