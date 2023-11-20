package com.doldolmeet.domain.commons;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Role {
    FAN("FAN"),
    ADMIN("ADMIN"),
    IDOL("IDOL");

    private final String key;
}
