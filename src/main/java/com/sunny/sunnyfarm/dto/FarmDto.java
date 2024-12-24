package com.sunny.sunnyfarm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FarmDto {
    private String signImage;
    private String cornerImage;
    private String farmDescription;
    private LocalDateTime gnomeEndsAt;
    private String gnomeImageUrl;
}
