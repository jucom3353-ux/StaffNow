// CertificateRequestDto.java
package com.example.demo.dto;

import lombok.Getter;

@Getter
public class CertificateRequestDto {
    private String name;
    private String issuer;
    private String acquiredDate;
}