package com.example.demo.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    // 400 Bad Request
    INVALID_STATUS_TRANSITION(HttpStatus.BAD_REQUEST, "잘못된 상태 전환입니다."),
    ALREADY_APPLIED(HttpStatus.BAD_REQUEST, "이미 지원한 공고입니다."),
    APPLICATION_CANCEL_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "완료된 지원은 취소할 수 없습니다."),
    CONTRACT_ALREADY_CANCELLED(HttpStatus.BAD_REQUEST, "취소된 계약서입니다."),
    CONTRACT_ALREADY_SIGNED(HttpStatus.BAD_REQUEST, "서명 완료된 계약서는 취소할 수 없습니다."),
    JOB_POST_CLOSED(HttpStatus.BAD_REQUEST, "마감된 공고입니다."),
    JOB_POST_DRAFT(HttpStatus.BAD_REQUEST, "임시저장된 공고입니다."),
    RECRUIT_FULL(HttpStatus.BAD_REQUEST, "모집 인원이 꽉 찼습니다."),
    WORK_DATE_INVALID(HttpStatus.BAD_REQUEST, "근무 시작일이 종료일보다 늦습니다."),
    WORK_DATE_NOT_SET(HttpStatus.BAD_REQUEST, "공고에 근무 시작일/종료일이 설정되어 있지 않습니다."),
    PAYROLL_PENDING_ONLY(HttpStatus.BAD_REQUEST, "대기 상태의 정산만 처리 가능합니다."),
    PAYROLL_CONFIRMED_ONLY(HttpStatus.BAD_REQUEST, "확정된 정산만 지급 처리 가능합니다."),
    SUBSCRIPTION_REQUIRED(HttpStatus.BAD_REQUEST, "구독 플랜이 필요합니다."),
    GPS_OUT_OF_RANGE(HttpStatus.BAD_REQUEST, "근무지에서 너무 멀리 떨어져 있습니다."),
    UNSUPPORTED_WAGE_TYPE(HttpStatus.BAD_REQUEST, "지원하지 않는 임금 타입입니다."),
    FILE_NOT_FOUND(HttpStatus.BAD_REQUEST, "파일이 없습니다."),
    FILE_TYPE_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "허용되지 않는 파일 형식입니다."),
    INVALID_TIME_TYPE(HttpStatus.BAD_REQUEST, "유효하지 않은 시간대입니다."),
    INVALID_SKILL_NAME(HttpStatus.BAD_REQUEST, "스킬명을 입력해주세요."),
    INVALID_MESSAGE_CONTENT(HttpStatus.BAD_REQUEST, "메시지 내용을 입력해주세요."),
    INVALID_REPORT_REASON(HttpStatus.BAD_REQUEST, "신고 사유를 선택해주세요."),
    PASSWORD_TOO_SHORT(HttpStatus.BAD_REQUEST, "비밀번호는 8자 이상이어야 합니다."),
    DEADLINE_INVALID(HttpStatus.BAD_REQUEST, "마감일은 오늘 이후 날짜여야 합니다."),
    DEADLINE_FORMAT_INVALID(HttpStatus.BAD_REQUEST, "마감일 형식이 올바르지 않습니다. (yyyy-MM-dd)"),
    PROFILE_IMAGE_LIMIT(HttpStatus.BAD_REQUEST, "프로필 사진은 최대 10장까지 등록 가능합니다."),
    PREFERRED_TIME_REQUIRED(HttpStatus.BAD_REQUEST, "선호 시간대를 1개 이상 선택해주세요."),
    BUSINESS_LICENSE_PENDING_ONLY(HttpStatus.BAD_REQUEST, "검토 중인 사업자등록증만 처리 가능합니다."),
    NO_PROFILE_IMAGE(HttpStatus.BAD_REQUEST, "프로필 사진이 없습니다."),
    SELF_BLOCK_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "본인을 차단할 수 없습니다."),
    SELF_MESSAGE_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "본인에게 메시지를 보낼 수 없습니다."),
    SELF_REPORT_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "본인 메시지는 신고할 수 없습니다."),
    SUBSCRIPTION_NOT_FOUND(HttpStatus.BAD_REQUEST, "활성 구독이 없습니다."),
    VERIFY_CODE_REQUIRED(HttpStatus.BAD_REQUEST, "인증코드를 먼저 요청해주세요."),
    VERIFY_CODE_EXPIRED(HttpStatus.BAD_REQUEST, "인증코드가 만료되었습니다."),
    VERIFY_CODE_INVALID(HttpStatus.BAD_REQUEST, "인증코드가 일치하지 않습니다."),
    ADMIN_SUSPEND_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "관리자 계정은 정지할 수 없습니다."),
    ADMIN_DELETE_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "관리자 계정은 삭제할 수 없습니다."),
    CATEGORY_REQUIRED(HttpStatus.BAD_REQUEST, "공고에 카테고리가 설정되어 있지 않습니다."),
    WORK_NOT_COMPLETED(HttpStatus.BAD_REQUEST, "근무 완료 후에만 리뷰 작성 가능합니다."),
    JOB_POST_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "공고 등록 가능 횟수를 초과했습니다."),
    INVITATION_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "초대 가능 인원을 초과했습니다."),
    INVALID_REFERRAL_CODE(HttpStatus.BAD_REQUEST, "유효하지 않은 추천 코드입니다."),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "비밀번호가 틀렸습니다."),
    INVALID_EMAIL(HttpStatus.BAD_REQUEST, "이메일이 존재하지 않습니다."),

    // 401 Unauthorized
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "토큰이 만료되었습니다."),
    TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "Refresh Token이 없습니다."),
    REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "Refresh Token이 만료되었습니다."),
    REFRESH_TOKEN_BLACKLISTED(HttpStatus.UNAUTHORIZED, "이미 사용된 Refresh Token입니다."),

    // 403 Forbidden
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    COMPANY_ONLY(HttpStatus.FORBIDDEN, "기업 회원만 접근 가능합니다."),
    WORKER_ONLY(HttpStatus.FORBIDDEN, "구직자만 접근 가능합니다."),
    ADMIN_ONLY(HttpStatus.FORBIDDEN, "관리자만 접근 가능합니다."),
    NOT_MY_JOB_POST(HttpStatus.FORBIDDEN, "본인 공고만 접근 가능합니다."),
    NOT_MY_APPLICATION(HttpStatus.FORBIDDEN, "본인 지원만 접근 가능합니다."),
    NOT_MY_CONTRACT(HttpStatus.FORBIDDEN, "본인 계약서만 접근 가능합니다."),
    NOT_MY_APPLICATION_COMPANY(HttpStatus.FORBIDDEN, "본인 공고의 지원자만 접근 가능합니다."),
    WORKER_PROFILE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "본인에게 온 초대만 접근 가능합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "권한이 없습니다."),

    // 404 Not Found
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    JOB_POST_NOT_FOUND(HttpStatus.NOT_FOUND, "공고를 찾을 수 없습니다."),
    APPLICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "지원 내역을 찾을 수 없습니다."),
    WORK_SESSION_NOT_FOUND(HttpStatus.NOT_FOUND, "근무회차를 찾을 수 없습니다."),
    WORK_ATTENDANCE_NOT_FOUND(HttpStatus.NOT_FOUND, "출퇴근 기록을 찾을 수 없습니다."),
    PAYROLL_NOT_FOUND(HttpStatus.NOT_FOUND, "정산 내역을 찾을 수 없습니다."),
    CONTRACT_NOT_FOUND(HttpStatus.NOT_FOUND, "계약서를 찾을 수 없습니다."),
    RESUME_NOT_FOUND(HttpStatus.NOT_FOUND, "이력서를 찾을 수 없습니다."),
    NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "알림을 찾을 수 없습니다."),
    JOB_POST_ROLE_NOT_FOUND(HttpStatus.NOT_FOUND, "직무를 찾을 수 없습니다."),
    INVITATION_NOT_FOUND(HttpStatus.NOT_FOUND, "초대를 찾을 수 없습니다."),
    DISPUTE_NOT_FOUND(HttpStatus.NOT_FOUND, "분쟁을 찾을 수 없습니다."),
    CONTRACT_PDF_NOT_FOUND(HttpStatus.NOT_FOUND, "계약서를 찾을 수 없습니다."),
    MESSAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "메시지를 찾을 수 없습니다."),
    REPORT_NOT_FOUND(HttpStatus.NOT_FOUND, "신고를 찾을 수 없습니다."),
    SKILL_NOT_FOUND(HttpStatus.NOT_FOUND, "스킬을 찾을 수 없습니다."),
    BOOKMARK_NOT_FOUND(HttpStatus.NOT_FOUND, "북마크를 찾을 수 없습니다."),
    PLAN_NOT_FOUND(HttpStatus.NOT_FOUND, "플랜을 찾을 수 없습니다."),
    PROFILE_IMAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "사진을 찾을 수 없습니다."),
    WORKER_NOT_FOUND(HttpStatus.NOT_FOUND, "근로자를 찾을 수 없습니다."),
    EDUCATION_NOT_FOUND(HttpStatus.NOT_FOUND, "학력을 찾을 수 없습니다."),
    CAREER_NOT_FOUND(HttpStatus.NOT_FOUND, "경력을 찾을 수 없습니다."),
    CERTIFICATE_NOT_FOUND(HttpStatus.NOT_FOUND, "자격증을 찾을 수 없습니다."),
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "카테고리를 찾을 수 없습니다."),
    NOTICE_NOT_FOUND(HttpStatus.NOT_FOUND, "공지사항을 찾을 수 없습니다."),
    FAQ_NOT_FOUND(HttpStatus.NOT_FOUND, "FAQ를 찾을 수 없습니다."),
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다."),
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "댓글을 찾을 수 없습니다."),
    BANNER_NOT_FOUND(HttpStatus.NOT_FOUND, "배너를 찾을 수 없습니다."),
    POPUP_NOT_FOUND(HttpStatus.NOT_FOUND, "팝업을 찾을 수 없습니다."),
    ALREADY_SCRAPPED(HttpStatus.CONFLICT, "이미 스크랩한 인재입니다."),
    SCRAP_NOT_FOUND(HttpStatus.NOT_FOUND, "스크랩을 찾을 수 없습니다."),
    MEMO_NOT_FOUND(HttpStatus.NOT_FOUND, "메모를 찾을 수 없습니다."),
    QUESTION_NOT_FOUND(HttpStatus.NOT_FOUND, "사전질문을 찾을 수 없습니다."),
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "결제 내역을 찾을 수 없습니다."),
    APPEAL_NOT_FOUND(HttpStatus.NOT_FOUND, "소명 내역을 찾을 수 없습니다."),
    ATTENDANCE_DISPUTE_NOT_FOUND(HttpStatus.NOT_FOUND, "출퇴근 분쟁 내역을 찾을 수 없습니다."),
    INQUIRY_NOT_FOUND(HttpStatus.NOT_FOUND, "문의를 찾을 수 없습니다."),
    TERMS_NOT_FOUND(HttpStatus.NOT_FOUND, "약관을 찾을 수 없습니다."),
    STAMP_NOT_FOUND(HttpStatus.NOT_FOUND, "도장을 찾을 수 없습니다."),
    BOOST_NOT_FOUND(HttpStatus.NOT_FOUND, "활성화된 부스트를 찾을 수 없습니다."),
    EVENT_NOT_FOUND(HttpStatus.NOT_FOUND, "이벤트를 찾을 수 없습니다."),


    // 409 Conflict
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),
    ALREADY_CHECKED_IN(HttpStatus.CONFLICT, "이미 출근 처리된 지원입니다."),
    ALREADY_CHECKED_OUT(HttpStatus.CONFLICT, "이미 퇴근 처리된 기록입니다."),
    ALREADY_ASSIGNED(HttpStatus.CONFLICT, "이미 배정된 지원자입니다."),
    PAYROLL_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 해당 주차 정산이 존재합니다."),
    ALREADY_REPORTED(HttpStatus.CONFLICT, "이미 신고한 내역입니다."),
    ALREADY_DISPUTED(HttpStatus.CONFLICT, "이미 분쟁이 신청된 내역입니다."),
    ALREADY_NO_SHOW(HttpStatus.CONFLICT, "이미 노쇼 처리된 지원입니다."),
    ALREADY_ABSENT(HttpStatus.CONFLICT, "이미 결근 처리된 지원입니다."),
    ALREADY_COMPLETED(HttpStatus.CONFLICT, "이미 완료된 지원입니다."),
    ALREADY_BLOCKED(HttpStatus.CONFLICT, "이미 차단한 사용자입니다."),
    ALREADY_BOOKMARKED(HttpStatus.CONFLICT, "이미 북마크한 공고입니다."),
    ALREADY_INVITED(HttpStatus.CONFLICT, "이미 초대한 근로자입니다."),
    ALREADY_REVIEWED(HttpStatus.CONFLICT, "이미 리뷰 작성 완료입니다."),
    ALREADY_SKILL(HttpStatus.CONFLICT, "이미 등록된 스킬입니다."),
    BLACKLIST_NOT_FOUND(HttpStatus.NOT_FOUND, "채용부적합 내역을 찾을 수 없습니다."),
    ALREADY_BLACKLISTED(HttpStatus.CONFLICT, "이미 채용부적합으로 등록된 인재입니다."),
    ALREADY_APPEALED(HttpStatus.CONFLICT, "이미 소명을 신청한 출퇴근 기록입니다."),
    ALREADY_DISPUTED_ATTENDANCE(HttpStatus.CONFLICT, "이미 분쟁을 신청한 출퇴근 기록입니다."),
    APPLICATION_CANCEL_TIME_EXCEEDED(HttpStatus.BAD_REQUEST, "지원 후 48시간이 초과되어 취소할 수 없습니다."),
    BOOST_ALREADY_ACTIVE(HttpStatus.CONFLICT, "이미 활성화된 부스트가 있습니다."),

    // 500 Internal Server Error
    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드에 실패했습니다."),
    PDF_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "PDF 생성에 실패했습니다.");
    

    private final HttpStatus httpStatus;
    private final String message;

    ErrorCode(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }

    public HttpStatus getHttpStatus() { return httpStatus; }
    public String getMessage() { return message; }
}