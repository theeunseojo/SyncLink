package com.SyncLink.presentation;

import com.SyncLink.enums.MemberType;

/**
 * 멤버 정보를 담는 DTO.
 */
public record MemberDto(
        Long id,
        String name,
        MemberType type) {
}
