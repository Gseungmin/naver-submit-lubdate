package com.example.naver.web.util;

import lombok.extern.slf4j.Slf4j;

import java.util.Set;

@Slf4j
public class Util {

    /*유효성 검사*/
    public static final int MAX_MEMBER_NAME_LIMIT = 10;
    public static final int MAX_MEMBER_PHONE_LIMIT = 11;
    public static final int PHONE_REQUEST_LIMIT = 3;
    public static final int STORY_REQUEST_LIMIT = 20;
    public static final int EMOTION_REQUEST_LIMIT = 30;
    public static final int MAX_PLAN_TITLE_LENGTH = 20;
    public static final int MAX_PLAN_CONTENT_LENGTH = 250;
    public static final int MAX_STORY_MEMO_LENGTH = 200;
    public static final int MAX_STORY_LOCATION_LENGTH = 10;
    public static final int MAX_STORY_DATE_LENGTH = 10;
    public static final int MAX_STORY_DELETE_COUNT = 10;
    public static final int MAX_MEMO_CONTENT_LENGTH = 500;
    public static final int MAX_EMOTION_NAME = 6;
    public static final int MAX_EMOTION_COUNT = 30;
    public static final int DELETE_DATE = 30;
    public static final int MAX_BOOK_LENGTH = 20;
    public static final int MAX_PAGE_LENGTH = 20;
    public static final int MAX_PAGE_CONTENT_LENGTH = 50;
    public static final int MAX_PAGE_STORY_SIZE = 15;
    public static final int MAX_BOOK_DELETE_COUNT = 10;
    public static final int MAX_STORY_INSERT_LIMIT = 5;

    /*페이징*/

    /*게시글 상태*/
    public static final boolean ITEM_CREATED = true;
    public static final boolean ITEM_DELETED = false;

    /*상수*/
    public static final String SLACK_WEBHOOK_URL = "SLACK_WEBHOOK_URL";
    public static final String KAKAO_API = "https://kapi.kakao.com/v2/user/me";
    public static final String APPLE_API = "https://appleid.apple.com/auth/keys";
    public static final String ORIGINAL_PREFIX = "기본이미지프리픽스";
    public static final String IMAGE_PREFIX = "람다이미지프리픽스";
    public static final String DEFAULT_PROFILE_IMAGE = "기본이미지";

    public static final Set<String> POSSIBLE_GET_ROUTE = Set.of("/couple/synchronize", "/couple");

    /*시간*/
    public static final long EPOCH = 1721865600000L;
    public static final long HALF_MINUTE = 30 * 1000;
    public static final long ONE_MINUTE = 60 * 1000;
    public static final long TEM_MINUTE = 10 * 60 * 1000;
    public static final long ONE_HOUR = 60 * 60 * 1000;
    public static final long ONE_DAY = 24 * 60 * 60 * 1000;
    public static final long ACCESS_TOKEN_EXPIRED = ONE_DAY * 30;
    public static final long REFRESH_TOKEN_EXPIRED = ONE_DAY * 60;

    public static final String ACCESS_TOKEN = "accessToken";
    public static final String REFRESH_TOKEN = "refreshToken";

    //캐시 키
    public static final String QUEUE_PREFIX = "queue:";
    public static final String QUEUE_SEQ_PREFIX = "queue_seq:";

    public static final String UPDATE_STORY_CACHE = "updatedStoryCache";
    public static final String DELETED_STORY_CACHE = "deletedStoryCache";
    public static final String UPDATE_PLAN_CACHE = "updatedPlanCache";
    public static final String DELETED_PLAN_CACHE = "deletedPlanCache";
    public static final String UPDATE_MEMO_CACHE = "updatedMemoCache";
    public static final String DELETED_MEMO_CACHE = "deletedMemoCache";

    public static final String DELETED_BOOK_CACHE = "deletedBookCache";
    public static final String UPDATE_BOOK_CACHE = "updatedBookCache";
    public static final String DELETED_PAGE_CACHE = "deletedPageCache";
    public static final String UPDATE_PAGE_CACHE = "updatedPageCache";
    public static final String UPDATE_TAG_CACHE = "updatedTagCache";

    //메시지 큐
    public static final String EXCHANGE_NAME = "chat_exchange";

    public static final String MESSAGE_PROFILE = "-2";
    public static final String MESSAGE_CHAT = "0";
    public static final String MESSAGE_PLAN = "1";
    public static final String MESSAGE_STORY = "2";
    public static final String MESSAGE_MEMO = "3";
    public static final String MESSAGE_EMOTION = "4";
    public static final String MESSAGE_BOOK = "5";
    public static final String MESSAGE_PAGE = "6";
    public static final String MESSAGE_TAG = "7";
    public static final String MESSAGE_PLAN_BULK = "8";
    public static final String MESSAGE_PLAN_BULK_DELETE = "9";
    public static final String MESSAGE_PLAN_BULK_SYNC = "10";
    public static final String MESSAGE_STORY_BULK = "11";

    public static final String DEVICE = "0";
    public static final String ALARM = "1";
    public static final String INSERT = "0";
    public static final String UPDATE = "1";
    public static final String DELETE = "2";

    public static final Integer STORY = 1;
    public static final Integer MEMO = 2;
    public static final Integer PLAN = 3;
    public static final Integer BOOK = 4;
    public static final Integer PAGE = 5;

    /*OS*/
    public static final Long ANDROID = 0L;
    public static final Long IOS = 1L;
}
