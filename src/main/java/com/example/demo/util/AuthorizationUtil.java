package com.example.demo.util;

import com.example.demo.entity.JobPost;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.util.AuthorizationUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
  import com.example.demo.entity.User;
import com.example.demo.util.AuthorizationUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.example.demo.util.AuthorizationUtil;
import com.example.demo.exception.CustomException;
import com.example.demo.exception.ErrorCode;
  

public class AuthorizationUtil {

    /**
     * 현재 로그인한 유저 반환
     */
    public static User getLoginUser() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }

    /**
     * COMPANY 또는 MANAGER 권한 검증
     */
    public static void validateCompanyOrManager(User user) {
        if (user.getRole() != Role.COMPANY && user.getRole() != Role.MANAGER) {
            throw new CustomException(ErrorCode.COMPANY_ONLY);
        }
    }

    /**
     * 본인 공고 여부 검증
     */
    public static boolean isMyJobPost(JobPost post, User loginUser) {
        Long companyId = loginUser.getRole() == Role.MANAGER
                ? loginUser.getCompany().getId()
                : loginUser.getId();
        return post.getUser().getId().equals(companyId) ||
               post.getUser().getId().equals(loginUser.getId());
    }

    /**
     * MANAGER의 실제 회사 유저 반환
     */
    public static User getCompanyUser(User loginUser) {
        return loginUser.getRole() == Role.MANAGER
                ? loginUser.getCompany()
                : loginUser;
    }
}