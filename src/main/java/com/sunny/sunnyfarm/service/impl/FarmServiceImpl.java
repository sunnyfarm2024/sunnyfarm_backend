package com.sunny.sunnyfarm.service.impl;

import com.sunny.sunnyfarm.dto.FarmDto;
import com.sunny.sunnyfarm.entity.Farm;
import com.sunny.sunnyfarm.repository.FarmRepository;
import com.sunny.sunnyfarm.service.CheckResult;
import com.sunny.sunnyfarm.service.FarmService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FarmServiceImpl implements FarmService {

    private final FarmRepository farmRepository;

    public FarmDto getFarm(Integer farmId) {
        return farmRepository.findFarmDto(farmId);
    }


    public CheckResult updateFarmDescription(int farmId, String farmDescription) {
        Farm farm = farmRepository.findById(farmId).orElse(null);

        if (farm != null) {
            farm.setFarmDescription(farmDescription);
            farmRepository.save(farm);
            return CheckResult.SUCCESS;
        }

        return CheckResult.FAIL;
    }
}
