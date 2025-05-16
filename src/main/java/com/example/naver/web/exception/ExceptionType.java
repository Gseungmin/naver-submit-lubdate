package com.example.naver.web.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ExceptionType {

    /* Common Exception */
    UNKNOWN_ERROR( 90000, "알수없는 에러가 발생하였습니다."),
    TOKEN_NOT_EXIST( 90100, "JWT Token이 존재하지 않습니다."),
    TOKEN_INVALID( 90100, "유효하지 않은 JWT Token 입니다."),
    TOKEN_EXPIRED( 91000, "토큰 만료기간이 지났습니다."),
    MULTI_LOGIN( 90101, "다른 기기에서 로그인 되었습니다."),
    BLOCKED_IP( 90200, "의심스러운 활동으로 IP가 잠시 차단되었어요! 관리자에게 문의해주세요."),
    SQS_ERROR( 90201, "SQS에 문제가 발생했어요."),
    NOT_AVAILABLE_NOW( 90202, "현재 많은 사용자로 인해 작성할수없어요! 잠시만 기다려주세요."),
    CAN_NOT_PHONE_AUTH( 90203, "현재 번호 인증서비스가 이용불가능해요! 관리자에게 문의해주세요."),
    ENCRYPT_FAIL( 92000, "암호화에 실패하였습니다."),
    DECRYPT_FAIL( 92001, "복호화에 실패하였습니다."),
    BULK_UPDATE_FAIL(92002, "벌크 업데이트에 실패하였습니다."),
    SYSTEM_TIME_EXCEPTION(93000, "시간 정보가 잘못되었어요"),

    /* Member Exception */
    MEMBER_NOT_EXIST( 10000, "회원이 존재하지 않아요."),
    LOGIN_FAILED(10001, "로그인에 실패하였습니다."),
    PHONE_INFO_EXIST( 10002, "이미 가입된 휴대폰 정보입니다."),
    MAX_PHONE_REQUEST( 10003, "동일한 번호로는 하루에 3번까지 요청이 가능해요."),
    SOCIAL_CONNECT_FAILED(10005, "소셜계정 연결에 실패하였습니다. 다시 시도해주세요."),
    INVALID_INVITE_CODE( 10006, "올바른 커플 코드를 입력해주세요."),
    MAX_STORY_REQUEST( 10007, "하루에 최대 20개의 사진을 업로드할 수 있어요."),
    MAX_EMOTION_REQUEST( 10008, "하루에 최대 30개의 감정을 보낼수 있어요."),
    SAME_PHONE_NUMBER( 10009, "이미 동일한 번호를 사용하고 있어요"),
    ALREADY_CHANGED_PHONE( 10010, "번호는 한달에 한번 수정가능해요! 추가 변경을 원하시면 관리자에게 문의해주세요"),
    DELETED_MEMBER( 10011, "이미 탈퇴한 회원입니다."),
    DISCONNECT_FOR_DELETE( 10012, "탈퇴하려면 먼저 커플 연결을 해제해주세요!"),
    DELETED_PHONE_NUMBER( 10013, "탈퇴한 휴대전화 번호입니다."),
    ADMIN_ONLY( 10014, "관리자만 접근가능합니다."),

    /* Couple Exception */
    COUPLE_ALREADY_EXIST( 20000, "기존에 있던 커플 연결을 해제해야해요!"),
    COUPLE_CODE_INVALID( 20001, "잘못된 커플 코드입니다. 관리자에게 문의해주세요!"),
    COUPLE_NOT_EXIST( 20003, "존재하지 않는 커플 정보입니다. 관리자에게 문의해주세요!"),
    RECOVER_EXPIRED( 20004, "복구기간이 만료되어 복구할 수 없어요."),
    RECOVER_INVALID( 20005, "복구할 수 없는 커플코드입니다. 관리자에게 문의해주세요!"),
    COUPLE_CAN_NOT_RECOVER( 20007, "커플 상태에서는 복구신청을 할 수 없습니다."),
    ALREADY_DIS_CONNECTED( 20009, "이미 해제된 연결입니다. 관리자에게 문의해주세요."),
    ALREADY_RE_CONNECTED_ING( 20010, "이미 복구신청중입니다. 응답을 기다려주세요!"),
    RECOVER_STATE_CAN_NOT_CONNECT( 20011, "복구 신청중에는 새연결을 진행할 수 없어요!"),
    SELF_INVITE_IMPOSSIBLE( 20015, "스스로를 초대할 수 없어요!"),
    COUPLE_NEED( 20016, "커플 연결이 먼저 필요합니다!"),
    INVALID_COUPLE( 20017, "잘못된 커플정보입니다. 관리자에게 문의해주세요!"),

    /* Story Exception */
    STORY_NOT_EXIST( 40000, "존재하지 않는 스토리입니다."),
    COUPLE_CAN_WRITE_STORY( 40001, "스토리를 작성하려면 커플 연결이 필요해요."),
    STORY_ALREADY_DELETED( 40002, "이미 삭제된 스토리입니다."),
    ALBUM_ALREADY_DELETED( 40003, "이미 삭제된 앨범입니다."),
    COUPLE_CAN_WRITE_ALBUM( 40004, "앨범을 만드려면 커플 연결이 필요해요."),
    ALBUM_NOT_EXIST( 40005, "존재하지 않는 앨범입니다."),
    UN_AUTH_ALBUM( 40006, "앨범에 접근권한이 존재하지 않습니다."),
    SELECT_ONE_STORY_FOR_MOVE( 40007, "이동할 스토리를 선택해주세요!"),
    CAN_NOT_MOVE( 40008, "해당 앨범으로 이동할 수 없어요. 존재하는지 앨범을 다시 확인해주세요!"),
    SELECT_ONE_STORY_FOR_DELETE( 40009, "삭제할 사진을 선택해주세요!"),
    STORY_IN_ALBUM_DELETED_OR_MOVE( 40010, "스토리 사진이 이동되었거나 삭제되었는지 확인해주세요!"),
    STORY_DELETED( 40011, "스토리가 삭제되었어요!"),
    ALBUM_DELETED( 40012, "앨범이 삭제되었어요!"),
    UN_AUTH_STORY( 40013, "스토리에 접근권한이 없어요!"),

    COUPLE_CAN_WRITE_BOOK(41000, "스토리북 작성을 위해 커플 연결이 필요해요!"),
    BOOK_NOT_EXIST(41001, "존재하지 않는 스토리북입니다."),
    DELETED_BOOK(41002, "스토리북이 삭제되었어요!"),
    UN_AUTH_BOOK( 41003, "스토리북에 접근권한이 없어요!"),
    BOOK_ALREADY_DELETED( 41004, "이미 삭제된 스토리북입니다."),
    PAGE_NOT_EXIST(41005, "존재하지 않는 페이지입니다."),
    DELETED_PAGE(41006, "페이지가 삭제되었어요!"),

    /* REDIS Exception */
    REDIS_CONNECT_ERROR(60000, "레디스 서버 연결에 실패했습니다"),
    REDIS_TIMEOUT_ERROR(60001, "레디스 연결 시간이 초과되었습니다"),
    REDIS_INSERT_ERROR(60002, "레디스 메시지 삽입 시 오류가 발생했습니다"),
    REDIS_GET_ERROR(60003, "메시지 조회 시 오류가 발생했습니다"),
    REDIS_DELETE_ERROR(60004, "레디스 메시지 삭제 시 오류가 발생했습니다"),
    REDIS_WRITE_ERROR(60005, "메시지 작성 시 오류가 발생했습니다"),
    REDIS_DELETE_CHECK_ERROR(60006, "메시지 삭제 체크 시 오류가 발생했습니다"),
    REDIS_UPDATE_CHECK_ERROR(60007, "메시지 업데이트 체크 시 오류가 발생했습니다"),
    REDIS_DELETE_CACHE_REMOVE_ERROR(60008, "삭제 캐시 제거 오류가 발생했습니다"),
    REDIS_UPDATE_CACHE_REMOVE_ERROR(60009, "업데이트 캐시 제거 오류가 발생했습니다"),

    /* Event Exception */
    COUPLE_CAN_SUMMIT_EVENT( 70000, "커플만 이벤트를 신청할 수 있어요."),
    ONLY_ONE_CAN_SUMMIT_EVENT( 70001, "이미 신청을 완료하였어요."),

    /* DTO Exception */
    EVENT_DTO_INVALID( 80000, "신청 양식을 다시 확인해주세요."),
    MEMBER_DTO_INVALID(80000, "회원정보를 다시 입력해주세요."),
    MEMBER_DTO_INVALID_NAME(80000, "이름은 10자 이내로 작성해주세요!"),
    MEMBER_DTO_INVALID_PHONE(80000, "올바른 번호를 입력해주세요!"),
    MEMBER_DTO_INVALID_DATE(80000, "올바른 날짜 정보를 입력해주세요!"),
    MEMBER_DTO_INVALID_DATE_AFTER(80000, "아직 선택할 수 없는 날짜입니다!"),
    MEMBER_DTO_INVALID_GENDER(80000, "성별을 다시 선택해주세요!"),
    MEMBER_DTO_INVALID_PROFILE(80000, "프로필 이미지를 다시 선택해주세요!"),
    MEMBER_DTO_INVALID_ALARM(80000, "알림을 다시 선택해주세요."),

    STORY_DTO_INVALID(80000, "스토리 정보를 다시 입력해주세요!"),
    STORY_DTO_INVALID_MEMO(80000, "스토리 메모는 200자 이내로 작성해주세요!"),
    STORY_DTO_INVALID_LOCATION(80000, "위치정보는 10자 이내로 작성해주세요!"),
    STORY_DTO_INVALID_DATE(80000, "날짜 정보를 다시 작성해주세요!"),
    STORY_DTO_INVALID_URL(80000, "이미지를 다시 업로드 해주세요!"),
    STORY_COUNT_INVALID(80000, "한번에 최대 5장의 사진을 입력할 수 있어요!");

    private final int code;
    private final String errorMessage;
}
