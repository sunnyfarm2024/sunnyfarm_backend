package com.sunny.sunnyfarm.service;

import com.sunny.sunnyfarm.dto.FarmDto;

public interface FarmService {
    FarmDto getFarm(Integer farmId);
    CheckResult updateFarmDescription(int userId, String farmDescription);
}
