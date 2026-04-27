# com.first-ticket.common-jpa

JPA 기본 엔티티 및 설정을 제공하는 공통 모듈입니다.

---

## 📝 버전

| 버전 | 변경 내용 |
|------|------|
| `0.0.1-SNAPSHOT` | • JPA 기본 엔티티 및 설정 추가 (`BaseEntity`, `BaseUserEntity`, `CommonJpaAutoConfiguration`)<br>• QueryDSL 설정 추가 (`JPAQueryFactory` 빈 등록) <br> • `SecurityAuditorAware` 추가 - X-User-Id 헤더 기반 JPA Auditing 자동 주입 (`createdBy`/`updatedBy`)|

---

## 📦 의존성 추가

> 배포 방법 및 의존성 추가는 [common README](https://github.com/first-ticket/common)를 참고해주세요.

```groovy
implementation 'com.first-ticket:common-jpa:0.0.1-SNAPSHOT'
```

---

## 🗂️ 패키지 구조

```
com.firstticket.common.persistence
├── CommonJpaAutoConfiguration.java  ← EntityScan, JPAQueryFactory 설정, SecurityAuditorAware 등록
├── BaseEntity.java                  ← 생성/수정/삭제 시간
├── BaseUserEntity.java              ← BaseEntity + 생성/수정/삭제 유저
└── SecurityAuditorAware.java        ← X-User-Id 헤더 기반 JPA Auditor
```

---

## 🗄️ CommonJpaAutoConfiguration

`com.firstticket` 하위 패키지를 자동으로 스캔하여 JPA 엔티티와 Repository를 등록합니다.
`JPAQueryFactory` 빈을 자동으로 등록하여 QueryDSL을 바로 사용할 수 있습니다.

> ⚠️ 모든 서비스의 베이스 패키지가 `com.firstticket`으로 시작해야 합니다.

```java
// 서비스 엔티티 자동 스캔됨
package com.firstticket.sampleservice.domain;

@Entity
public class Sample extends BaseEntity { ... }
```

QueryDSL 사용 시 `JPAQueryFactory`를 주입받아 바로 사용합니다.

```java
@Repository
@RequiredArgsConstructor
public class SampleQueryRepositoryImpl implements SampleQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Sample> findBySpec(SampleSearchSpec spec) {
        return queryFactory
            .selectFrom(sample)
            .where(...)
            .fetch();
    }
}
```

---

## 🏗️ BaseEntity / BaseUserEntity

### BaseEntity

생성 시간, 수정 시간, 삭제 시간을 자동으로 관리합니다.

| 필드 | 타입 | 설명 |
|------|------|------|
| `createdAt` | `LocalDateTime` | 생성 시간 (자동) |
| `updatedAt` | `LocalDateTime` | 수정 시간 (자동, insert 시 null) |
| `deletedAt` | `LocalDateTime` | 삭제 시간 |

```java
@Entity
public class Sample extends BaseEntity {
    // createdAt, updatedAt, deletedAt 자동 포함
}
```

소프트 삭제가 필요한 경우 도메인 엔티티에서 `delete()`를 호출하는 메서드를 정의합니다.

```java
// 도메인 엔티티
public void softDelete() {
    // 삭제 권한 검증
    delete();  // deletedAt 자동 설정
}
```

### BaseUserEntity

`BaseEntity`를 상속받아 생성/수정/삭제 유저 UUID를 추가로 관리합니다.

| 필드 | 타입 | 설명 |
|------|------|------|
| `createdBy` | `UUID` | 생성 유저 (자동) |
| `updatedBy` | `UUID` | 수정 유저 (자동, insert 시 null) |
| `deletedBy` | `UUID` | 삭제 유저 |

```java
@Entity
public class Sample extends BaseUserEntity {
    // createdAt, updatedAt, deletedAt, createdBy, updatedBy, deletedBy 자동 포함
}
```

소프트 삭제가 필요한 경우 도메인 엔티티에서 `delete(UUID userId)`를 호출하는 메서드를 정의합니다.

```java
// 도메인 엔티티
public void softDelete(UUID userId) {
    // 삭제 권한 검증
    delete(userId);  // deletedAt, deletedBy 자동 설정
}
```

### SecurityAuditorAware

`createdBy` / `updatedBy`는 `SecurityAuditorAware`가 자동으로 채워줍니다.

| 동작 조건 | 결과 |
  |---|---|
| API Gateway가 `X-User-Id` 헤더 주입 | `createdBy` / `updatedBy`에 해당 UUID 자동 기록 |
| 헤더 없음 (내부 호출, Flyway 등) | `null` 유지 (예외 발생하지 않음) |
| 헤더 값이 UUID 형식이 아닌 경우 | `null` 유지 + warn 로그 출력 |
