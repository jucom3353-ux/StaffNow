package com.example.demo.service;

import com.example.demo.entity.Contract;
import com.example.demo.entity.ContractStatus;
import com.example.demo.entity.Payroll;
import com.example.demo.entity.PayrollStatus;
import com.example.demo.entity.User;
import com.example.demo.util.AuthorizationUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.example.demo.exception.CustomException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.PayrollRepository;
import com.itextpdf.html2pdf.HtmlConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@Service
@RequiredArgsConstructor
public class PayrollPdfService {

    private final PayrollRepository payrollRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${file.base-url}")
    private String fileBaseUrl;

    @Transactional
    public String generatePayrollPdf(Long payrollId, User loginUser) {

        Payroll payroll = payrollRepository.findById(payrollId)
                .orElseThrow(() -> new CustomException(ErrorCode.PAYROLL_NOT_FOUND));

        // 본인 정산만 다운로드 가능 (근로자 or 기업)
        if (!payroll.getWorker().getId().equals(loginUser.getId()) &&
            !payroll.getJobPost().getUser().getId().equals(loginUser.getId())) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        // 확정 또는 지급 완료 상태만 다운로드 가능
        if (payroll.getStatus() == PayrollStatus.PENDING ||
            payroll.getStatus() == PayrollStatus.REJECTED) {
            throw new CustomException(ErrorCode.INVALID_STATUS_TRANSITION,
                    "확정 또는 지급 완료된 정산만 명세서를 발급할 수 있습니다.");
        }

        String html = buildHtml(payroll);

        String dirPath = System.getProperty("user.dir") + "/" + uploadDir + "/payrolls";
        File dir = new File(dirPath);
        if (!dir.exists()) dir.mkdirs();

        String fileName = "payroll_" + payrollId + "_" +
                payroll.getWorker().getId() + ".pdf";
        String filePath = dirPath + "/" + fileName;

        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            HtmlConverter.convertToPdf(html, fos);
        } catch (IOException e) {
            throw new CustomException(ErrorCode.PDF_GENERATION_FAILED);
        }

        return fileBaseUrl + "/uploads/payrolls/" + fileName;
    }

    private String buildHtml(Payroll payroll) {

        String fontPath = getClass().getClassLoader()
                .getResource("fonts/NanumGothic.ttf").toExternalForm();

        String wageType = payroll.getJobPost().getWageType() != null
                ? payroll.getJobPost().getWageType().name() : "-";

        return """
                <!DOCTYPE html>
                <html>
                <head>
                  <meta charset="UTF-8"/>
                  <style>
                    @font-face {
                      font-family: 'NanumGothic';
                      src: url('%s');
                    }
                    body { font-family: 'NanumGothic', Arial, sans-serif; padding: 40px; font-size: 14px; }
                    h1 { text-align: center; font-size: 22px; margin-bottom: 30px; }
                    table { width: 100%%; border-collapse: collapse; margin-bottom: 20px; }
                    td { padding: 8px 12px; border: 1px solid #ccc; }
                    .label { background-color: #f5f5f5; font-weight: bold; width: 40%%; }
                    .section { margin-top: 30px; font-weight: bold; font-size: 16px; border-left: 4px solid #333; padding-left: 8px; }
                    .total { font-size: 16px; font-weight: bold; color: #333; }
                  </style>
                </head>
                <body>
                  <h1>급여 명세서</h1>

                  <div class="section">1. 근로자 정보</div>
                  <table>
                    <tr><td class="label">근로자명</td><td>%s</td></tr>
                    <tr><td class="label">연락처</td><td>%s</td></tr>
                  </table>

                  <div class="section">2. 사업장 정보</div>
                  <table>
                    <tr><td class="label">기업명</td><td>%s</td></tr>
                    <tr><td class="label">공고명</td><td>%s</td></tr>
                    <tr><td class="label">근무 장소</td><td>%s</td></tr>
                  </table>

                  <div class="section">3. 근무 내역</div>
                  <table>
                    <tr><td class="label">정산 기간</td><td>%s ~ %s</td></tr>
                    <tr><td class="label">임금 유형</td><td>%s</td></tr>
                    <tr><td class="label">시급</td><td>%s 원</td></tr>
                    <tr><td class="label">총 근무시간</td><td>%.1f 시간</td></tr>
                  </table>

                  <div class="section">4. 급여 내역</div>
                  <table>
                    <tr><td class="label">기본급</td><td>%s 원</td></tr>
                    <tr><td class="label">주휴수당</td><td>%s 원</td></tr>
                    <tr><td class="label">총 급여</td><td>%s 원</td></tr>
                    <tr><td class="label">공제액 (3.3%%)</td><td>%s 원</td></tr>
                    <tr><td class="label total">실수령액</td><td class="total">%s 원</td></tr>
                  </table>

                  <div class="section">5. 정산 상태</div>
                  <table>
                    <tr><td class="label">정산 상태</td><td>%s</td></tr>
                    <tr><td class="label">확정일시</td><td>%s</td></tr>
                    <tr><td class="label">지급일시</td><td>%s</td></tr>
                  </table>

                </body>
                </html>
                """.formatted(
                fontPath,
                nullSafe(payroll.getWorker().getName()),
                nullSafe(payroll.getWorker().getPhone()),
                nullSafe(payroll.getJobPost().getUser().getCompanyName()),
                nullSafe(payroll.getJobPost().getTitle()),
                nullSafe(payroll.getJobPost().getWorkLocation()),
                nullSafe(payroll.getWorkWeekStart()),
                nullSafe(payroll.getWorkWeekEnd()),
                wageType,
                String.valueOf(payroll.getHourlyWage()),
                payroll.getTotalWorkHours(),
                String.valueOf(payroll.getBasicPay()),
                String.valueOf(payroll.getHolidayPay()),
                String.valueOf(payroll.getTotalPay()),
                String.valueOf(payroll.getTotalPay() - payroll.getNetPay()),
                String.valueOf(payroll.getNetPay()),
                nullSafe(payroll.getStatus().name()),
                payroll.getConfirmedAt() != null
                        ? payroll.getConfirmedAt().toString() : "미확정",
                payroll.getPaidAt() != null
                        ? payroll.getPaidAt().toString() : "미지급"
        );
    }

    private String nullSafe(String value) {
        return value != null ? value : "-";
    }
}