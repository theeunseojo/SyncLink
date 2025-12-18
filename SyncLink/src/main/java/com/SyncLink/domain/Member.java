package com.SyncLink.domain;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {
    @Id@GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private  String email;
    private  String name;
    @Lob
    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private Room room;

    // 생성자
    @Builder
    public Member(String email, String name , String token, Room room){
        this.email = email;
        this.name = name;
        this.token = token;
        this.room = room;
    }

    // 토큰 갱신
    public Member updateToken(String accessToken){
        this.token = accessToken;
        return this;
    }

    public void setRoom(Room room){
        this.room = room;
    }
}

