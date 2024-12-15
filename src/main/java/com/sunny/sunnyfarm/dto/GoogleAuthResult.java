package com.sunny.sunnyfarm.dto;

import com.sunny.sunnyfarm.service.CheckResult;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoogleAuthResult {
    private CheckResult result;
    private String email;
}
