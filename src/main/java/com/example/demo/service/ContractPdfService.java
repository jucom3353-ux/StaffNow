package com.example.demo.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entity.Contract;
import com.example.demo.entity.ContractStatus;
import com.example.demo.exception.CustomException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.ContractRepository;
import com.itextpdf.html2pdf.HtmlConverter;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ContractPdfService {

    private final ContractRepository contractRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${file.base-url}")
    private String fileBaseUrl;

    @Transactional
    public String generateContractPdf(Long contractId, Long loginUserId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new CustomException(ErrorCode.CONTRACT_NOT_FOUND));

        if (!contract.getCompany().getId().equals(loginUserId) &&
            !contract.getWorker().getId().equals(loginUserId)) {
            throw new CustomException(ErrorCode.NOT_MY_CONTRACT);
        }

        if (contract.getStatus() == ContractStatus.DOWNLOAD_EXPIRED) {
            throw new CustomException(ErrorCode.INVALID_STATUS_TRANSITION,
                    "다운로드 기간이 만료된 계약서입니다. (완료 후 1년)");
        }
        if (contract.getStatus() == ContractStatus.EXPIRED) {
            throw new CustomException(ErrorCode.INVALID_STATUS_TRANSITION,
                    "만료된 계약서입니다. (1개월 내 미서명)");
        }
        if (contract.getStatus() == ContractStatus.CANCELLED) {
            throw new CustomException(ErrorCode.CONTRACT_ALREADY_CANCELLED);
        }

        String html = buildHtml(contract);
        String dirPath = System.getProperty("user.dir") + "/" + uploadDir + "/contracts";
        File dir = new File(dirPath);
        if (!dir.exists()) dir.mkdirs();

        String fileName = "contract_" + contractId + ".pdf";
        String filePath = dirPath + "/" + fileName;

        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            HtmlConverter.convertToPdf(html, fos);
        } catch (IOException e) {
            throw new CustomException(ErrorCode.PDF_GENERATION_FAILED);
        }

        return fileBaseUrl + "/uploads/contracts/" + fileName;
    }

    private String buildHtml(Contract contract) {
        String fontPath = getClass().getClassLoader()
                .getResource("fonts/NanumGothic.ttf").toExternalForm();

        String companySignatureHtml = contract.getCompanySignatureUrl() != null
                ? "<img src='" + toLocalPath(contract.getCompanySignatureUrl())
                  + "' style='height:60px; object-fit:contain;'/>"
                : "미서명";

        String workerSignatureHtml = contract.getWorkerSignatureUrl() != null
                ? "<img src='" + toLocalPath(contract.getWorkerSignatureUrl())
                  + "' style='height:60px; object-fit:contain;'/>"
                : "미서명";

        return """
                <!DOCTYPE html>
                <html>
                <head>
                  <meta charset="UTF-8"/>
                  <style>
                    @font-face { font-family: 'NanumGothic'; src: url('%s'); }
                    body { font-family: 'NanumGothic', Arial, sans-serif; padding: 40px; font-size: 14px; }
                    h1 { text-align: center; font-size: 22px; margin-bottom: 30px; }
                    table { width: 100%%; border-collapse: collapse; margin-bottom: 20px; }
                    td { padding: 8px 12px; border: 1px solid #ccc; }
                    .label { background-color: #f5f5f5; font-weight: bold; width: 30%%; }
                    .section { margin-top: 30px; font-weight: bold; font-size: 16px; }
                    .sign-cell { height: 80px; vertical-align: middle; text-align: center; }
                  </style>
                </head>
                <body>
                  <h1>근로계약서</h1>
                  <div class="section">1. 계약 당사자</div>
                  <table>
                    <tr><td class="label">기업명</td><td>%s</td></tr>
                    <tr><td class="label">근로자명</td><td>%s</td></tr>
                    <tr><td class="label">근로자 연락처</td><td>%s</td></tr>
                  </table>
                  <div class="section">2. 근무 정보</div>
                  <table>
                    <tr><td class="label">공고명</td><td>%s</td></tr>
                    <tr><td class="label">근무 장소</td><td>%s</td></tr>
                    <tr><td class="label">근무 시간</td><td>%s ~ %s</td></tr>
                    <tr><td class="label">급여 유형</td><td>%s</td></tr>
                    <tr><td class="label">급여</td><td>%s 원</td></tr>
                  </table>
                  <div class="section">3. 계약 기간</div>
                  <table>
                    <tr><td class="label">계약 시작일</td><td>%s</td></tr>
                    <tr><td class="label">계약 종료일</td><td>%s</td></tr>
                    <tr><td class="label">계약 상태</td><td>%s</td></tr>
                  </table>
                  <div class="section">4. 서명</div>
                  <table>
                    <tr>
                      <td class="label">기업 서명</td>
                      <td class="sign-cell">%s</td>
                    </tr>
                    <tr>
                      <td class="label">기업 서명일시</td>
                      <td>%s</td>
                    </tr>
                    <tr>
                      <td class="label">근로자 서명</td>
                      <td class="sign-cell">%s</td>
                    </tr>
                    <tr>
                      <td class="label">근로자 서명일시</td>
                      <td>%s</td>
                    </tr>
                  </table>
                </body>
                </html>
                """.formatted(
                fontPath,
                nullSafe(contract.getCompany().getCompanyName()),
                nullSafe(contract.getWorker().getName()),
                nullSafe(contract.getWorker().getPhone()),
                nullSafe(contract.getJobPost().getTitle()),
                nullSafe(contract.getJobPost().getWorkLocation()),
                nullSafe(contract.getJobPost().getStartTime()),
                nullSafe(contract.getJobPost().getEndTime()),
                contract.getJobPost().getWageType() != null
                        ? contract.getJobPost().getWageType().name() : "-",
                contract.getJobPost().getWageAmount() != null
                        ? contract.getJobPost().getWageAmount().toString() : "-",
                nullSafe(contract.getContractStartDate()),
                nullSafe(contract.getContractEndDate()),
                nullSafe(contract.getStatus().name()),
                companySignatureHtml,
                contract.getCompanySignedAt() != null
                        ? contract.getCompanySignedAt().toString() : "미서명",
                workerSignatureHtml,
                contract.getWorkerSignedAt() != null
                        ? contract.getWorkerSignedAt().toString() : "미서명"
        );
    }

    private String toLocalPath(String url) {
        if (url == null) return "";
        if (url.startsWith("http://") || url.startsWith("https://")) return url;
        String localPath = System.getProperty("user.dir") + "/" + uploadDir + "/"
                + url.replace(fileBaseUrl + "/uploads/", "");
        return "file:///" + localPath.replace("\\", "/");
    }

    private String nullSafe(String value) {
        return value != null ? value : "-";
    }
}