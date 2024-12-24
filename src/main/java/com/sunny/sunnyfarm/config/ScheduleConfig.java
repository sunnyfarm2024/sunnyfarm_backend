package com.sunny.sunnyfarm.config;

import com.sunny.sunnyfarm.repository.UserPlantRepository;
import com.sunny.sunnyfarm.service.PlantService;
import com.sunny.sunnyfarm.service.QuestService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;

@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class ScheduleConfig {
    private final PlantService plantService;
    private final UserPlantRepository userPlantRepository;
    private final QuestService questService; // 퀘스트 서비스 추가

    @Scheduled(cron = "0 0,30 * * * ?") // 매 정각 및 30분마다 실행
    public void updateAllUserPlants() {
        List<Integer> userPlantIds = userPlantRepository.findAllUserPlantIds();
        for (Integer userPlantId : userPlantIds) {
            try {
                plantService.updateGrowthStage(userPlantId);
            } catch (Exception e) {
                System.err.println("userPlantId: " + userPlantId + "의 성장 단계 업데이트에 실패했습니다.");
                e.printStackTrace();
            }
        }

        System.out.println("모든 UserPlant의 성장 단계가 성공적으로 업데이트되었습니다.");
    }

    @Scheduled(cron = "0 0 0 * * ?") // 매일 밤 12시 실행
    public void resetDailyQuests() {
        try {
            questService.resetDailyQuests(); // 퀘스트 초기화 호출
            System.out.println("일일 퀘스트가 성공적으로 초기화되었습니다.");
        } catch (Exception e) {
            System.err.println("일일 퀘스트 초기화에 실패했습니다.");
            e.printStackTrace();
        }
    }
}
