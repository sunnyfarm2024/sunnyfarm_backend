package com.sunny.sunnyfarm.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sunny.sunnyfarm.dto.WeatherDto;
import com.sunny.sunnyfarm.entity.User;
import com.sunny.sunnyfarm.repository.UserRepository;
import com.sunny.sunnyfarm.service.WeatherServise;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class WeatherServiceImpl implements WeatherServise {

    private final UserRepository userRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private static final String API_URL = "https://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getUltraSrtFcst";
    @Value("${weather.api-key}")
    private String apiKey;

    // 사용자 별 날씨 가져오기
    public WeatherDto getWeather(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. ID: " + userId));

        return fetchWeather(user.getLatitude(), user.getLongitude());
    }

    // 날씨 가져오기
    public WeatherDto fetchWeather(float lat, float lon) {
        // 격자 좌표 변환
        double[] grid = convertToGrid((double) lat, (double) lon);
        int nx = (int) grid[0];
        int ny = (int) grid[1];

        // 현재 시간 기준 base_date, base_time
        String[] baseDateTime = getBaseDateTime();
        String baseDate = baseDateTime[0];
        String baseTime = baseDateTime[1];

        // 기상청 api 요청 url
        String url = API_URL +
                "?serviceKey=" + apiKey +
                "&pageNo=1" +
                "&numOfRows=1000" +
                "&dataType=JSON" +
                "&base_date=" + baseDate +
                "&base_time=" + baseTime +
                "&nx=" + nx +
                "&ny=" + ny;

        // api 호출
        URI uri = URI.create(url);
        String response = restTemplate.getForObject(uri, String.class);

        System.out.println("========");
        System.out.println(uri);
        System.out.println("========");

        // 날씨 데이터 분석
        return parseWeatherData(response);
    }

    // 날씨 데이터 분석
    private WeatherDto parseWeatherData(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode items = root.path("response").path("body").path("items").path("item");

            WeatherDto weatherDto = new WeatherDto();

            // 카테고리별 첫 번째 값을 추적하기 위한 Set
            Set<String> categoriesSet = new HashSet<>();

            for (JsonNode item : items) {
                String category = item.path("category").asText();
                String value = item.path("fcstValue").asText();

                // 카테고리별로 첫 번째 항목만 저장
                if (!categoriesSet.contains(category)) {
                    categoriesSet.add(category);  // 첫 번째 카테고리 등장 시 기록

                    // 카테고리별 첫 번째 예측값만 처리
                    switch (category) {
                        case "T1H": weatherDto.setTemperature(value); break; // 기온
                        case "SKY": weatherDto.setSkyStatus(value); break;   // 하늘 상태
                        case "REH": weatherDto.setHumidity(value); break;   // 습도
                        case "PTY": weatherDto.setPrecipitationType(value); break; // 강수 형태
                        case "WSD": weatherDto.setWindSpeed(value); break;  // 풍속
                        case "LGT": weatherDto.setLightning(value); break;  // 낙뢰
                    }
                }
            }

            return weatherDto;
        } catch (Exception e) {
            throw new RuntimeException("날씨 데이터를 분석할 수 없습니다.", e);
        }
    }

    // 현재 시간 기준 base_date, base_time
    private String[] getBaseDateTime() {
        LocalDateTime now = LocalDateTime.now().minusMinutes(30);
        String baseDate = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String baseTime = now.format(DateTimeFormatter.ofPattern("HHmm"));
        return new String[]{baseDate, baseTime};
    }

    // 기상청에서 사용하는 격자 좌표 변환
    private static double[] convertToGrid(double lat, double lon) {
        final double RE = 6371.00877; // 지구 반경 (km)
        final double GRID = 5.0;      // 격자 간격 (km)
        final double SLAT1 = 30.0;    // 투영 위도 1 (degree)
        final double SLAT2 = 60.0;    // 투영 위도 2 (degree)
        final double OLON = 126.0;    // 기준점 경도 (degree)
        final double OLAT = 38.0;     // 기준점 위도 (degree)
        final double XO = 43;         // 기준점 X 좌표 (격자)
        final double YO = 136;        // 기준점 Y 좌표 (격자)

        final double DEGRAD = Math.PI / 180.0;

        // 계산용 변수
        double re = RE / GRID;
        double slat1 = SLAT1 * DEGRAD;
        double slat2 = SLAT2 * DEGRAD;
        double olon = OLON * DEGRAD;
        double olat = OLAT * DEGRAD;

        double sn = Math.tan(Math.PI * 0.25 + slat2 * 0.5) / Math.tan(Math.PI * 0.25 + slat1 * 0.5);
        sn = Math.log(Math.cos(slat1) / Math.cos(slat2)) / Math.log(sn);
        double sf = Math.tan(Math.PI * 0.25 + slat1 * 0.5);
        sf = Math.pow(sf, sn) * Math.cos(slat1) / sn;
        double ro = Math.tan(Math.PI * 0.25 + olat * 0.5);
        ro = re * sf / Math.pow(ro, sn);

        double ra = Math.tan(Math.PI * 0.25 + lat * DEGRAD * 0.5);
        ra = re * sf / Math.pow(ra, sn);
        double theta = lon * DEGRAD - olon;
        if (theta > Math.PI) theta -= 2.0 * Math.PI;
        if (theta < -Math.PI) theta += 2.0 * Math.PI;
        theta *= sn;

        // 격자 좌표 계산
        double x = ra * Math.sin(theta) + XO + 0.5;
        double y = ro - ra * Math.cos(theta) + YO + 0.5;

        return new double[]{Math.floor(x), Math.floor(y)};
    }


}
