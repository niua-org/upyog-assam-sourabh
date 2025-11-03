package org.egov.user.domain.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enum representing different Single Sign-On (SSO) provider types
 * supported by the system for user authentication.
 */
public enum SSOType {
    EPRAMAAN("EPRAMAAN"),
    DIGILOCKER("DIGILOCKER"),
    NONE("NONE");

    private String value;

    SSOType(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
        return String.valueOf(value);
    }

    @JsonCreator
    public static SSOType fromValue(String text) {
        for (SSOType type : SSOType.values()) {
            if (String.valueOf(type.value).equalsIgnoreCase(text)) {
                return type;
            }
        }
        return NONE;
    }
    
    public String getValue() {
        return value;
    }
}

