package uni.backend.util;

import java.util.List;
import java.util.Map;

public class MainCategoryMap {

    //검색용
    public static final Map<String, String> HASHTAG_TRANSLATION_MAP = Map.ofEntries(
        // 여행
        Map.entry("trip", "여행"),
        Map.entry("旅行", "여행"),
        Map.entry("travel", "여행"),
        Map.entry("journey", "여행"),
        Map.entry("旅游", "여행"), // 중국어 동의어
        Map.entry("游览", "여행"), // 중국어 동의어

        // 행정
        Map.entry("administration", "행정"),
        Map.entry("行政", "행정"),
        Map.entry("govt", "행정"),
        Map.entry("government work", "행정"),
        Map.entry("管理", "행정"), // 중국어 동의어
        Map.entry("政府工作", "행정"), // 중국어 동의어

        // 부동산
        Map.entry("realty", "부동산"),
        Map.entry("房地产", "부동산"),
        Map.entry("real estate", "부동산"),
        Map.entry("property", "부동산"),
        Map.entry("地产", "부동산"), // 중국어 동의어
        Map.entry("房产", "부동산"), // 중국어 동의어

        // 은행
        Map.entry("banking", "은행"),
        Map.entry("银行", "은행"),
        Map.entry("finance", "은행"),
        Map.entry("money management", "은행"),
        Map.entry("金融", "은행"), // 중국어 동의어
        Map.entry("储蓄", "은행"), // 중국어 동의어

        // 휴대폰
        Map.entry("mobile", "휴대폰"),
        Map.entry("通讯", "휴대폰"),
        Map.entry("cell phone", "휴대폰"),
        Map.entry("smartphone", "휴대폰"),
        Map.entry("移动电话", "휴대폰"), // 중국어 동의어
        Map.entry("智能手机", "휴대폰"), // 중국어 동의어

        // 언어교환
        Map.entry("language exchange", "언어교환"),
        Map.entry("语言交换", "언어교환"),
        Map.entry("language swap", "언어교환"),
        Map.entry("language practice", "언어교환"),
        Map.entry("语言互换", "언어교환"), // 중국어 동의어
        Map.entry("语言练习", "언어교환"), // 중국어 동의어

        // 대학 생활
        Map.entry("college life", "대학 생활"),
        Map.entry("大学生活", "대학 생활"),
        Map.entry("university life", "대학 생활"),
        Map.entry("campus life", "대학 생활"),
        Map.entry("高校生活", "대학 생활"), // 중국어 동의어
        Map.entry("校园生活", "대학 생활"), // 중국어 동의어

        // 맛집
        Map.entry("gastroventure", "맛집"),
        Map.entry("美食游", "맛집"),
        Map.entry("food tour", "맛집"),
        Map.entry("restaurant exploration", "맛집"),
        Map.entry("美食探店", "맛집"), // 중국어 동의어
        Map.entry("美味之旅", "맛집"), // 중국어 동의어

        // 게임
        Map.entry("game", "게임"),
        Map.entry("游戏", "게임"),
        Map.entry("gaming", "게임"),
        Map.entry("video game", "게임"),
        Map.entry("电子竞技", "게임"), // 중국어 동의어
        Map.entry("电玩", "게임"), // 중국어 동의어

        // 쇼핑
        Map.entry("shopping", "쇼핑"),
        Map.entry("购物", "쇼핑"),
        Map.entry("mall", "쇼핑"),
        Map.entry("retail", "쇼핑"),
        Map.entry("采购", "쇼핑"), // 중국어 동의어
        Map.entry("买东西", "쇼핑") // 중국어 동의어
    );


    // 번역용
    public static final Map<String, Map<String, String>> KOREAN_HASHTAG_MAP = Map.of(
        "여행", Map.of("en", "Trip", "zh", "旅行"),
        "행정", Map.of("en", "Administration", "zh", "行政"),
        "부동산", Map.of("en", "Realty", "zh", "房地产"),
        "은행", Map.of("en", "Banking", "zh", "银行"),
        "휴대폰", Map.of("en", "Mobile", "zh", "通讯"),
        "언어교환", Map.of("en", "Language Exchange", "zh", "语言交换"),
        "대학 생활", Map.of("en", "College Life", "zh", "大学生活"),
        "맛집", Map.of("en", "Gastroventure", "zh", "美食游"),
        "게임", Map.of("en", "Game", "zh", "游戏"),
        "쇼핑", Map.of("en", "Shopping", "zh", "购物")
    );
}
