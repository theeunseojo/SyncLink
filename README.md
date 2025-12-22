# 🗓️ 언제비어 (SyncLink)

<div align="center">

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.0-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Google Calendar API](https://img.shields.io/badge/Google%20Calendar-API-4285F4?style=for-the-badge&logo=googlecalendar&logoColor=white)

**친구들과 딱 맞는 시간을 3초 만에 찾으세요!**

</div>

---

## 📌 프로젝트 소개

**언제비어**는 Google Calendar API를 활용하여 여러 사람의 일정을 비교하고, 모두가 가능한 시간을 자동으로 추천해주는 웹 서비스입니다.

### ✨ 주요 기능

- 🔐 **Google OAuth2 로그인** - 간편하게 구글 계정으로 로그인
- 📅 **Google Calendar 연동** - 구글 캘린더 일정 자동 동기화
- 🚀 **방 생성 및 공유** - 링크 하나로 친구들과 일정 공유
- ⏰ **실시간 가능 시간 추천** - 모든 참여자가 가능한 시간대 자동 계산
- 🎛️ **유연한 일정 관리** - 특정 일정 무시 기능으로 유연한 조율

---

## 🛠️ 기술 스택

### Backend
| 기술 | 버전 | 설명 |
|------|------|------|
| Spring Boot | 3.4.0 | 웹 애플리케이션 프레임워크 |
| Spring Security OAuth2 | - | Google OAuth2 인증 |
| Spring Data JPA | - | 데이터베이스 ORM |
| H2 Database | - | 인메모리 데이터베이스 |
| Google Calendar API | v3 | 캘린더 데이터 연동 |
| Springdoc OpenAPI | 2.8.9 | API 문서화 (Swagger) |

### Frontend
| 기술 | 설명 |
|------|------|
| HTML5 / CSS3 / JavaScript | 기본 웹 기술 |
| Bootstrap 5 | UI 컴포넌트 프레임워크 |
| FullCalendar 6 | 캘린더 UI 라이브러리 |
| Noto Sans KR | 한국어 폰트 |

---

## 📁 프로젝트 구조

```
SyncLink/
├── src/main/java/com/SyncLink/
│   ├── auth/                    # OAuth2 인증 관련
│   │   ├── OAuth2LoginSecurityConfig.java
│   │   └── OAuth2SuccessHandler.java
│   ├── config/                  # 설정 클래스
│   │   └── SwaggerConfig.java
│   ├── domain/                  # 도메인 엔티티
│   │   ├── Event.java
│   │   ├── IgnoredEvent.java
│   │   ├── Member.java
│   │   └── Room.java
│   ├── error/                   # 에러 처리
│   │   ├── ErrorResponse.java
│   │   └── GlobalExceptionHandler.java
│   ├── infrastructure/          # 리포지토리 계층
│   │   ├── EventRepository.java
│   │   ├── IgnoredEventRepository.java
│   │   ├── MemberRepository.java
│   │   └── RoomRepository.java
│   ├── presentation/            # 컨트롤러 계층
│   │   ├── AuthController.java
│   │   ├── EventController.java
│   │   ├── RoomController.java
│   │   └── ScheduleController.java
│   ├── service/                 # 서비스 계층
│   │   ├── CustomOAuthService.java
│   │   ├── EventService.java
│   │   ├── RoomService.java
│   │   └── ScheduleService.java
│   └── SyncLinkApplication.java
├── src/main/resources/
│   └── static/
│       ├── index.html           # 메인 페이지
│       └── room.html            # 방 페이지
└── pom.xml
```

---

## 🚀 시작하기

### 사전 요구사항

- Java 21 이상
- Maven
- Google Cloud Console에서 OAuth2 클라이언트 ID 발급

### 1. 프로젝트 클론

```bash
git clone https://github.com/your-username/SyncLink.git
cd SyncLink
```

### 2. Google OAuth2 설정

1. [Google Cloud Console](https://console.cloud.google.com/)에서 새 프로젝트 생성
2. **APIs & Services > Credentials**에서 OAuth 2.0 클라이언트 ID 생성
3. 승인된 리디렉션 URI에 `http://localhost:8080/login/oauth2/code/google` 추가
4. Google Calendar API 활성화

### 3. 환경 설정

`src/main/resources/application.properties` 파일에 다음 내용 추가:

```properties
spring.security.oauth2.client.registration.google.client-id=YOUR_CLIENT_ID
spring.security.oauth2.client.registration.google.client-secret=YOUR_CLIENT_SECRET
spring.security.oauth2.client.registration.google.scope=profile,email,https://www.googleapis.com/auth/calendar.readonly
```

### 4. 실행

```bash
cd SyncLink
./mvnw spring-boot:run
```

또는

```bash
mvn spring-boot:run
```

### 5. 접속

브라우저에서 `http://localhost:8080` 접속

---

## 📖 사용 방법

### 1️⃣ 방 생성하기
1. 메인 페이지에서 모임 이름 입력
2. 시작일과 종료일 선택
3. "방 생성하고 링크 복사하기" 클릭

### 2️⃣ 친구 초대하기
- 생성된 방 링크를 친구들에게 공유

### 3️⃣ 일정 확인하기
1. 방에서 "구글 로그인/참여" 클릭
2. Google 계정으로 로그인
3. 캘린더 일정이 자동으로 동기화됨

### 4️⃣ 가능한 시간 확인
- "가능한 시간 보기" 탭에서 모든 참여자가 가능한 시간대 확인
- 달력 뷰 또는 리스트 뷰로 확인 가능

---

## 📚 API 문서

애플리케이션 실행 후 아래 URL에서 Swagger UI를 통해 API 문서를 확인할 수 있습니다:

```
http://localhost:8080/swagger-ui.html
```

---

## 🎨 스크린샷

### 메인 페이지
- 깔끔한 그라데이션 디자인
- 간편한 방 생성 폼
- Google 로그인 버튼

### 방 페이지
- 참여자 목록 표시
- FullCalendar 기반 일정 캘린더
- 가능한 시간 하이라이트

---

## 🤝 기여하기

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## 📄 라이선스

이 프로젝트는 MIT 라이선스 하에 배포됩니다.

---

## 📞 문의

프로젝트에 대한 문의사항이 있으시면 이슈를 등록해 주세요.

---

<div align="center">

Made with ❤️ using Spring Boot

</div>
