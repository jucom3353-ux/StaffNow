package com.example.demo.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.demo.dto.BusinessValidationResponseDto;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class NtsApiService {

    @Value("${nts.api.key}")
    private String apiKey;

    public BusinessValidationResponseDto validateBusinessNumber(String businessNumber) {
        try {
            WebClient webClient = WebClient.builder()
                    .baseUrl("https://api.odcloud.kr/api/nts-businessman/v1")
                    .build();

            Map<String, Object> requestBody = Map.of(
                    "b_no", List.of(businessNumber.replaceAll("-", ""))
            );

            Map response = webClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/status")
                            .queryParam("serviceKey", apiKey)
                            .build())
                    .header("Content-Type", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response != null) {
                var dataList = (List<?>) response.get("data");
                if (dataList != null && !dataList.isEmpty()) {
                    var data = (Map<?, ?>) dataList.get(0);
                    String bStt = (String) data.get("b_stt"); // 계속사업자/휴업자/폐업자
                    String bSttCd = (String) data.get("b_stt_cd"); // 01:계속, 02:휴업, 03:폐업

                    return new BusinessValidationResponseDto(
                            businessNumber,
                            "01".equals(bSttCd), // 계속사업자만 유효
                            (String) data.get("co_nm"),
                            (String) data.get("p_nm"),
                            bStt
                    );
                }
            }
        } catch (Exception e) {
            log.warn("국세청 API 호출 실패: businessNumber={}, error={}", businessNumber, e.getMessage());
        }
        return new BusinessValidationResponseDto(businessNumber, false, null, null, "조회 실패");
    }
}