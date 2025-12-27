package com.SyncLink.presentation;

import com.SyncLink.enums.memberType;

public record memberDto(
        Long id,
        String name,
        memberType type
) {
}
