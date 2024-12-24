package com.sunny.sunnyfarm.service.impl;

import com.sunny.sunnyfarm.dto.GoogleAuthResult;
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
import java.nio.file.StandardCopyOption;
import java.util.Random;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

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
    private final GoogleOAuthService googleOAuthService;
    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/src/main/resources/static/uploads";

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
    public boolean checkUserName(String userName, int userId) {
        return userRepository.findByUserName(userName)
                .filter(user -> user.getUserId() != userId) // 기본 타입 비교
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
        Path sourcePath = Paths.get("src/main/resources/static/image/profile/profile.png");
        Path targetPath = Paths.get("src/main/resources/static/uploads/profile" + savedUser.getUserId() + ".png");

        try {
            // 업로드 폴더가 없으면 생성
            if (!Files.exists(targetPath.getParent())) {
                Files.createDirectories(targetPath.getParent());
            }
            // 파일 복사
            Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("파일 복사 성공. Target: " + targetPath);
        } catch (IOException e) {
            System.err.println("파일 복사 실패. Source: " + sourcePath + ", Target: " + targetPath);
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
        // 이메일로 사용자 조회
        User user = userRepository.findByEmail(userLoginDto.getEmail()).orElse(null);

        if (user == null) {
            return CheckResult.FAIL; // 사용자를 찾을 수 없음
        }

        // 비밀번호 검증
        if (userLoginDto.getPassword() != null) {
            // 일반 로그인: 비밀번호 검증
            if (!passwordEncoder.matches(userLoginDto.getPassword(), user.getPassword())) {
                return CheckResult.FAIL; // 비밀번호 불일치
            }
        }

        return CheckResult.SUCCESS; // 로그인 성공
    }

    public GoogleAuthResult processGoogleAuth(String code) {
        try {
            // Access Token 요청
            String email = googleOAuthService.getUserEmail(code);

            // UserLoginDto 생성
            UserLoginDto userLoginDto = new UserLoginDto();
            userLoginDto.setEmail(email); // Google에서 가져온 이메일 추가

            // 이메일 기반 사용자 검증
            User user = userRepository.findByEmail(email).orElse(null);

            // 회원가입 후 로그인 또는 바로 로그인
            if (user == null) {
                // 사용자 등록
                userLoginDto.setPassword(generateRandomPassword()); // 랜덤비밀번호 설정
                CheckResult registerResult = register(userLoginDto);
                if (registerResult != CheckResult.SUCCESS) {
                    return new GoogleAuthResult(CheckResult.FAIL, email); // 회원가입 실패
                }
            } else {
                userLoginDto.setPassword(null);
            }

            // 로그인 시도
            CheckResult loginResult = login(userLoginDto);
            if (loginResult != CheckResult.SUCCESS) {
                return new GoogleAuthResult(CheckResult.FAIL, email); // 로그인 실패
            }

            return new GoogleAuthResult(CheckResult.SUCCESS, email); // 회원가입 또는 로그인 성공
        } catch (Exception e) {
            // 예외 발생 시 실패 처리
            System.err.println("Google 인증 처리 중 오류: " + e.getMessage());
            return new GoogleAuthResult(CheckResult.FAIL, null);
        }
    }

    // 랜덤 비밀번호 생성
    private String generateRandomPassword() {
        int length = 8;
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder password = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            password.append(characters.charAt(random.nextInt(characters.length())));
        }
        return password.toString();
    }

    // 위치 정보 저장
    public CheckResult saveLocation(Integer userId, Float latitude, Float longitude) {
        try {
            // 사용자 존재 여부 확인 및 처리
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. ID: " + userId));
            user.setLatitude(latitude);
            user.setLongitude(longitude);
            userRepository.save(user);

            // 성공 시 SUCCESS 반환
            return CheckResult.SUCCESS;
        } catch (Exception e) {
            // 오류 발생 시 로그 기록
            System.err.println("위치 정보 저장 중 오류: " + e.getMessage());
            return CheckResult.FAIL;
        }
    }

    // 닉네임 수정
    public CheckResult updateUserName(int userId, String userName) {
        if(checkUserName(userName, userId)){
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
            // 저장 디렉토리 확인 및 생성
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 기존 파일 삭제 경로
            String targetFileName = "profile" + userId + ".png";
            Path targetFilePath = uploadPath.resolve(targetFileName);

            if (Files.exists(targetFilePath)) {
                Files.delete(targetFilePath); // 기존 파일 삭제
            }

            // 받은 파일을 BufferedImage로 읽음 (JPG/PNG 변환 지원)
            BufferedImage bufferedImage = ImageIO.read(file.getInputStream());

            // 파일 저장 경로
            File outputFile = targetFilePath.toFile();

            // PNG 형식으로 저장
            ImageIO.write(bufferedImage, "png", outputFile);

            // 파일 저장이 완료된 후 응답 반환
            if (Files.exists(targetFilePath)) {
                return CheckResult.SUCCESS;
            } else {
                return CheckResult.FAIL;
            }

        } catch (IOException e) {
            e.printStackTrace();
            return CheckResult.FAIL;
        }
    }

}