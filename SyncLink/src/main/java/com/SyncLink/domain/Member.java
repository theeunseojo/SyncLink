package com.SyncLink.domain;

import com.SyncLink.enums.ServiceType;
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

    @Enumerated(EnumType.STRING)
    private ServiceType serviceType;

    // 생성자
    @Builder
    public Member(String email, String name, String token, ServiceType serviceType) {
        this.email = email;
        this.name = name;
        this.token = token;
        this.serviceType = serviceType;
    }

    // 토큰 갱신
    public Member updateToken(String accessToken) {
        this.token = accessToken;
        return this;
    }

}
