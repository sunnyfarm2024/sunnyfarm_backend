package com.sunny.sunnyfarm.service.impl;

import com.sunny.sunnyfarm.dto.InventoryDto;
import com.sunny.sunnyfarm.entity.*;
import com.sunny.sunnyfarm.repository.*;
import com.sunny.sunnyfarm.service.CheckResult;
import com.sunny.sunnyfarm.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final UserPlantRepository userPlantRepository;
    private final FarmRepository farmRepository;
    private final PlantRepository plantRepository;
    private final ShopRepository shopRepository;

    @Override
    public List<InventoryDto> getInventory(Integer userId) {
        // 1. 기본 인벤토리 데이터 가져오기
        List<Object[]> rawInventoryData = inventoryRepository.findBasicInventoryData(userId);
        List<InventoryDto> inventoryItems = new ArrayList<>();

        // 3. Fertilizer, Gnome 데이터 가져오기
        List<Object[]> fertilizerTimes = inventoryRepository.findFertilizerTimes(userId);
        LocalDateTime gnomeEndsAt = inventoryRepository.findGnomeEndsAt(userId);


        // 2. Farm 상태 가져오기
        List<Object[]> farmPlantIds = inventoryRepository.findPlantIdsInFarm(userId);

        int leftPlantId = 0;
        int centerPlantId = 0;
        int rightPlantId = 0;

        if (!farmPlantIds.isEmpty()) {
            Object[] plantIds = farmPlantIds.get(0);
            leftPlantId = plantIds[0] != null ? (Integer) plantIds[0] : 0;
            centerPlantId = plantIds[1] != null ? (Integer) plantIds[1] : 0;
            rightPlantId = plantIds[2] != null ? (Integer) plantIds[2] : 0;
        }

        // 4. 난이도별 개수 계산
        int level2Count = (leftPlantId >= 3 && leftPlantId <= 5 ? 1 : 0) +
                (centerPlantId >= 3 && centerPlantId <= 5 ? 1 : 0) +
                (rightPlantId >= 3 && rightPlantId <= 5 ? 1 : 0);

        int level3Count = (leftPlantId >= 6 && leftPlantId <= 7 ? 1 : 0) +
                (centerPlantId >= 6 && centerPlantId <= 7 ? 1 : 0) +
                (rightPlantId >= 6 && rightPlantId <= 7 ? 1 : 0);

        // 5. Inventory 상태 설정
        for (Object[] row : rawInventoryData) {
            int itemId = (Integer) row[0]; // itemId
            int slotNumber = (Integer) row[1]; // slotNumber
            String itemImg = (String) row[2];
            String itemName = (String) row[3];
            String itemDescription = (String) row[4];
            Shop.ItemCategory itemCategory = (Shop.ItemCategory) row[5];

            // InventoryDto 생성
            InventoryDto inventoryItem = new InventoryDto(
                    slotNumber,
                    itemImg,
                    itemName,
                    itemDescription,
                    itemCategory,
                    true,
                    null,
                    null,
                    null,
                    null
            );

            // SEED 상태 설정
            if (itemCategory == Shop.ItemCategory.SEED) {
                if (leftPlantId != 0 && centerPlantId != 0 && rightPlantId != 0) {
                    inventoryItem.setInventoryItemStatus(false); // 슬롯 모두 사용 중
                } else if (itemId == 1 || itemId == 2) {
                    inventoryItem.setInventoryItemStatus(true); // Always usable
                } else if (itemId >= 3 && itemId <= 5) {
                    inventoryItem.setInventoryItemStatus(level2Count < 2); // 난이도 2 제한
                } else if (itemId >= 6 && itemId <= 7) {
                    inventoryItem.setInventoryItemStatus(level3Count < 1); // 난이도 3 제한
                }
            }

            // Fertilizer 시간 설정
            if (!fertilizerTimes.isEmpty()) {
                Object[] times = fertilizerTimes.get(0);
                inventoryItem.setLeftFertilizerAt((LocalDateTime) times[0]);
                inventoryItem.setCenterFertilizerAt((LocalDateTime) times[1]);
                inventoryItem.setRightFertilizerAt((LocalDateTime) times[2]);
            }

            // Gnome 시간 설정
            inventoryItem.setGnomeAt(gnomeEndsAt);

            // Inventory 리스트에 추가
            inventoryItems.add(inventoryItem);
        }

        return inventoryItems;
    }

    public CheckResult useItem(Integer userId, int slotNumber, String location, String plantName) {
        // Inventory 해당 아이템을 찾음
        Optional<Inventory> inventoryItem = inventoryRepository.findByUserIdAndSlotNumber(userId, slotNumber);

        if (inventoryItem.isPresent()) {
            Inventory item = inventoryItem.get();
            int itemId = item.getItem().getItemId();
            String category = String.valueOf(item.getItem().getCategory());  // 카테고리 확인

            // 카테고리에 따른 처리
            switch (category) {
                case "SEED":
                    // 씨앗 사용 처리
                    useSeed(userId, itemId, plantName, location);
                    deleteItem(userId, slotNumber);
                    break;
                case "FERTILIZER":
                    // 비료 사용 처리
                    useFertilizer(userId, itemId, location);
                    deleteItem(userId, slotNumber);
                    break;
                case "GNOME":
                    // 노움 사용 처리
                    useGnome(userId, itemId);
                    deleteItem(userId, slotNumber);
                    break;
                case "DECORATION":
                    // 장식 사용 처리
                    useDecoration(userId, itemId);
                    deleteItem(userId, slotNumber);
                    break;
                default:
                    return CheckResult.FAIL; // 잘못된 카테고리
            }
            return CheckResult.SUCCESS;
        }
        return CheckResult.FAIL;  // 아이템이 존재하지 않으면 실패
    }

    private void useDecoration(Integer userId, int itemId) {
        // 유저의 농장 정보 조회
        Farm farm = farmRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("농장을 찾을 수 없습니다."));

        if (itemId >= 12 && itemId <= 16) { // 12~16 아이템은 sign_id
            if (farm.getSign() != null) {
                addItem(userId, farm.getSign().getItemId());
            }
            Shop shop = shopRepository.findById(itemId).orElseThrow(() -> new IllegalArgumentException("아이템을 찾을 수 없습니다."));
            farm.setSign(shop);
        } else if (itemId >= 17) { // 17 이상 아이템은 corner_id
            if (farm.getCorner() != null) {
                addItem(userId, farm.getCorner().getItemId());
            }
            Shop shop = shopRepository.findById(itemId).orElseThrow(() -> new IllegalArgumentException("아이템을 찾을 수 없습니다."));
            farm.setCorner(shop);
        }

        // 변경된 farm 저장
        farmRepository.save(farm);
    }


    private void useGnome(Integer userId, int itemId) {
        // 현재 시간 구하기
        LocalDateTime currentTime = LocalDateTime.now();
        LocalDateTime gnomeEndTime = itemId == 10
                ? currentTime.plusHours(10)  // itemId가 10이면 10시간 뒤
                : currentTime.plusHours(20);  // itemId가 11이면 20시간 뒤

        // Farm에서 userId로 찾기 (farmId로 취급)
        Farm farm = farmRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("농장을 찾을 수 없습니다."));

        // 노움 종료 시간 업데이트
        farm.setGnomeEndsAt(gnomeEndTime);
        farmRepository.save(farm);  // Farm 업데이트
    }

    private void useFertilizer(Integer userId, int itemId, String location) {
        // 현재 시간 구하기
        LocalDateTime currentTime = LocalDateTime.now();
        LocalDateTime fertilizerEndTime = itemId == 8
                ? currentTime.plusHours(10)  // itemId가 8이면 10시간 뒤
                : currentTime.plusHours(20);  // itemId가 9이면 20시간 뒤

        // Farm에서 userId로 찾기
        Farm farm = farmRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("농장을 찾을 수 없습니다."));

        // 위치에 맞는 UserPlant를 찾기
        switch (location) {
            case "left":
                if (farm.getLeftPlant() != null) {
                    farm.getLeftPlant().setFertilizerEndsAt(fertilizerEndTime);
                    farmRepository.save(farm);
                } else {
                    throw new IllegalArgumentException("left 위치에 식물이 없습니다.");
                }
                break;
            case "center":
                if (farm.getCenterPlant() != null) {
                    farm.getCenterPlant().setFertilizerEndsAt(fertilizerEndTime);
                    farmRepository.save(farm);
                } else {
                    throw new IllegalArgumentException("center 위치에 식물이 없습니다.");
                }
                break;
            case "right":
                if (farm.getRightPlant() != null) {
                    farm.getRightPlant().setFertilizerEndsAt(fertilizerEndTime);
                    farmRepository.save(farm);
                } else {
                    throw new IllegalArgumentException("right 위치에 식물이 없습니다.");
                }
                break;
            default:
                throw new IllegalArgumentException("유효하지 않은 위치입니다.");
        }
    }

    private void useSeed(Integer userId, int itemId, String plantName, String location) {
        // 현재 시간 구하기
        LocalDateTime currentTime = LocalDateTime.now();

        // UserPlant 객체 생성
        UserPlant userPlant = new UserPlant();

        // userId로 Farm 조회
        Farm farm = farmRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("농장을 찾을 수 없습니다."));
        userPlant.setFarm(farm);  // Farm 객체 설정

        // itemId로 Plant 조회
        Plant plant = plantRepository.findById(itemId).orElseThrow(() -> new IllegalArgumentException("식물을 찾을 수 없습니다."));
        userPlant.setPlant(plant);  // Plant 객체 설정

        userPlant.setPlantName(plantName);
        userPlant.setLastWateredAt(currentTime);  // 현재 시간으로 last_watered_at 설정
        userPlantRepository.save(userPlant);  // UserPlant에 저장

        // Farm에서 userId로 찾기 (farmId로 취급)
        switch (location) {
            case "left":
                farm.setLeftPlant(userPlant);
                break;
            case "center":
                farm.setCenterPlant(userPlant);
                break;
            case "right":
                farm.setRightPlant(userPlant);
                break;
            default:
                throw new IllegalArgumentException("잘못된 위치");
        }

        // 변경된 farm 저장
        farmRepository.save(farm);
    }

    // 추가하기
    public void addItem(Integer userId, Integer itemId) {
        // NULL인 슬롯 찾기
        Optional<Inventory> emptySlot = inventoryRepository.findFirstByUserIdAndItemIsNull(userId);

        if (emptySlot.isPresent()) {
            // 슬롯이 존재하면 itemId 추가
            Inventory inventory = emptySlot.get();
            Shop item = shopRepository.findById(itemId)
                    .orElseThrow(() -> new IllegalArgumentException("Item을 찾을 수 없습니다."));
            inventory.setItem(item); // itemId 설정
            inventoryRepository.save(inventory); // 변경된 슬롯 저장
        }
    }

    // 버리기
    public CheckResult deleteItem(Integer userId, int slotNumber){
        Optional<Inventory> inventoryItem = inventoryRepository.findByUserIdAndSlotNumber(userId, slotNumber);

        if (inventoryItem.isPresent()) {
            // 아이템 삭제
            Inventory item = inventoryItem.get();
            item.setItem(null); // item 관계를 null로 변경
            inventoryRepository.save(item);
            return CheckResult.SUCCESS;
        } else {
            // 삭제할 아이템이 없는 경우
            return CheckResult.FAIL;
        }
    }
}