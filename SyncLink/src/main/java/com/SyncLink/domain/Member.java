package com.SyncLink.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private String email;
    private String name;
    @Lob
    private String token;

    // 생성자
    @Builder
    public Member(String email, String name, String token) {
        this.email = email;
        this.name = name;
        this.token = token;
    }

    // 토큰 갱신
    public Member updateToken(String accessToken) {
        this.token = accessToken;
        return this;
    }

}
