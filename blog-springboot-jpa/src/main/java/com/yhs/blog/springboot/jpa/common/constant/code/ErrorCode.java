package com.yhs.blog.springboot.jpa.common.constant.code;

public enum ErrorCode {
    // ================== 비즈니스 예외 (4xx)

      

    // 포스트 관련
    POST_NOT_FOUND(404, "BP001", "게시글을 찾을 수 없음"),

    // 카테고리 관련
    CATEGORY_NOT_FOUND(404, "BC001", "카테고리를 찾을 수 없음"),
    CATEGORY_HAS_CHILDREN(400, "BC002", "하위 카테고리가 존재하여 카테고리를 삭제할 수 없음"),
    CATEGORY_HAS_POSTS(400, "BC003", "게시글이 존재하여 카테고리를 삭제할 수 없음"),

    // 사용자 관련
    USER_NOT_FOUND(404, "BU001", "사용자를 찾을 수 없음"),

    // 인증 및 토큰 관련
    // AUTHENTICATION_FAILED(401, "BA001", "로그인 인증 실패"),
    ACCESS_TOKEN_EMPTY(401, "BA002", "토큰 헤더가 비어있거나 Bearer 로 시작하지 않음"),
    REFRESH_TOKEN_EXPIRED(401, "BA003", "리프레시 토큰 만료"),

    // 횟수 제한 초과
    RATE_LIMIT_EXCEEDED(429, "BR001", "1분 3회 초과 요청"),
    DUPLICATE_CHECK_LIMIT_EXCEEDED(429, "BD001", "1분 3회 초과 요청"),

    // ================== 시스템 예외 (5xx)

    // 게시글 관련

    // 게시글 작성 실패
    POST_CREATE_ERROR(500, "SP001", "게시글 작성 중 오류 발생"),

    // 직렬화 역직렬화
    SERIALIZATION_ERROR(500, "SS001", "객체 직렬화 중 오류 발생"),
    DESERIALIZATION_ERROR(500, "SDS001", "객체 역직렬화 중 오류 발생"),

    // 배치 작업 실패
    BATCH_JOB_EXECUTION_ERROR(500, "SB001", "배치 작업 실행 중 오류 발생"),

    // S3 파일 업로드 실패
    S3_UPLOAD_ERROR(500, "SAWS001", "S3 파일 업로드 중 오류 발생"),

    // 사용자 관련
    USER_CREATE_ERROR(500, "SU001", "사용자 생성 중 오류 발생"),
    USER_PROFILE_UPDATE_ERROR(500, "SU002", "프로필 업데이트 중 오류 발생"),

    // OAuth2 관련
    // OAuth2 사용자 정보 로드 실패
    OAUTH2_USER_LOAD_FAIL(500, "SO001", "OAuth2 사용자 정보를 로드 실패"),

    // OAuth2 추가 정보 입력 시 이메일이 없음
    OAUTH2_EMAIL_EMPTY(500, "SO002", "OAuth2 인증 실패"),

    // querydsl 관련 오류
    QUERY_DSL_ERROR(500, "SQS001", "QueryDSL 검색 중 오류 발생"),

    // clientIp를 가져올때 RequestContextHolder가 없을 때
    REQUEST_CONTEXT_NOT_FOUND(500, "SR001", "웹 요청 컨텍스트를 찾을 수 없음");

    private final int status;
    private final String code;
    private final String message;

    ErrorCode(int status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}