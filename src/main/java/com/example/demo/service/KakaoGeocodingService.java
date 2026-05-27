package com.example.demo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoGeocodingService {

    @Value("${kakao.api.key}")
    private String kakaoApiKey;

    public double[] getCoordinates(String address) {
        try {
            WebClient webClient = WebClient.builder()
                    .baseUrl("https://dapi.kakao.com")
                    .defaultHeader("Authorization", "KakaoAK " + kakaoApiKey)
                    .build();

            Map response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v2/local/search/address.json")
                            .queryParam("query", address)
                            .build())
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response != null) {
                var documents = (java.util.List<?>) response.get("documents");
                if (documents != null && !documents.isEmpty()) {
                    var first = (Map<?, ?>) documents.get(0);
                    double lat = Double.parseDouble((String) first.get("y"));
                    double lng = Double.parseDouble((String) first.get("x"));
                    return new double[]{lat, lng};
                }
            }
        } catch (Exception e) {
            log.warn("Geocoding 실패: address={}, error={}", address, e.getMessage());
        }
        return null;
    }
}