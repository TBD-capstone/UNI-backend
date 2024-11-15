package uni.backend.enums;

import lombok.Getter;

@Getter
public enum LanguageAbbrev {
    KO("KO"),
    EN("EN-US"),
    ZH("ZH");

    private final String value;

    LanguageAbbrev(String value) {
        this.value = value;
    }
//
//    public static LanguageAbbrev fromValue(String value) {
//        for (LanguageAbbrev abbrev : values()) {
//            if (abbrev.value.equalsIgnoreCase(value)) {
//                return abbrev;
//            }
//        }
//        throw new IllegalArgumentException("Unknown value: " + value);
//    }
}
