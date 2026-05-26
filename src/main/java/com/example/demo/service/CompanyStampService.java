package com.example.demo.service;

import com.example.demo.entity.CompanyStamp;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.exception.CustomException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.CompanyStampRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CompanyStampService {

    private final CompanyStampRepository companyStampRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${file.base-url}")
    private String fileBaseUrl;

    // 도장 업로드
    @Transactional
    public String uploadStamp(MultipartFile file, User loginUser) {
        if (loginUser.getRole() != Role.COMPANY) {
            throw new CustomException(ErrorCode.COMPANY_ONLY);
        }

        if (file.isEmpty()) {
            throw new CustomException(ErrorCode.FILE_NOT_FOUND);
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new CustomException(ErrorCode.FILE_NOT_FOUND);
        }

        String ext = originalFilename
                .substring(originalFilename.lastIndexOf(".")).toLowerCase();

        if (!ext.equals(".jpg") && !ext.equals(".jpeg")
                && !ext.equals(".png") && !ext.equals(".pdf")) {
            throw new CustomException(ErrorCode.FILE_TYPE_NOT_ALLOWED);
        }

        String dirPath = System.getProperty("user.dir") + "/" + uploadDir + "/stamps";
        File dir = new File(dirPath);
        if (!dir.exists()) dir.mkdirs();

        // 기존 파일 삭제
        companyStampRepository.findByUser(loginUser).ifPresent(stamp -> {
            String oldFileName = stamp.getStampUrl()
                    .substring(stamp.getStampUrl().lastIndexOf("/") + 1);
            File oldFile = new File(dirPath + "/" + oldFileName);
            if (oldFile.exists()) oldFile.delete();
        });

        String savedFilename = "stamp_" + loginUser.getId() + "_"
                + UUID.randomUUID() + ext;
        File savedFile = new File(dirPath + "/" + savedFilename);

        try {
            file.transferTo(savedFile);
        } catch (IOException e) {
            throw new CustomException(ErrorCode.FILE_UPLOAD_FAILED);
        }

        String url = fileBaseUrl + "/uploads/stamps/" + savedFilename;

        CompanyStamp stamp = companyStampRepository.findByUser(loginUser)
                .orElse(new CompanyStamp());
        stamp.setUser(loginUser);
        stamp.setStampUrl(url);
        companyStampRepository.save(stamp);

        return url;
    }

    // 도장 조회
    @Transactional(readOnly = true)
    public String getStamp(User loginUser) {
        if (loginUser.getRole() != Role.COMPANY) {
            throw new CustomException(ErrorCode.COMPANY_ONLY);
        }

        return companyStampRepository.findByUser(loginUser)
                .map(CompanyStamp::getStampUrl)
                .orElseThrow(() -> new CustomException(ErrorCode.STAMP_NOT_FOUND));
    }

    // 도장 삭제
    @Transactional
    public void deleteStamp(User loginUser) {
        if (loginUser.getRole() != Role.COMPANY) {
            throw new CustomException(ErrorCode.COMPANY_ONLY);
        }

        CompanyStamp stamp = companyStampRepository.findByUser(loginUser)
                .orElseThrow(() -> new CustomException(ErrorCode.STAMP_NOT_FOUND));

        String dirPath = System.getProperty("user.dir") + "/" + uploadDir + "/stamps";
        String fileName = stamp.getStampUrl()
                .substring(stamp.getStampUrl().lastIndexOf("/") + 1);
        File file = new File(dirPath + "/" + fileName);
        if (file.exists()) file.delete();

        companyStampRepository.delete(stamp);
    }
}