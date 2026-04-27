package com.firstticket.common.persistence;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;
import java.util.UUID;

/**
 * JPA Auditing의 createdBy / updatedBy를 자동으로 채워주는 공통 구현체입니다.
 *
 * API Gateway에서 Keycloak JWT 검증 후 X-User-Id 헤더에 UUID를 주입하며,
 * 각 서비스는 해당 헤더를 통해 요청자를 식별합니다.
 */

@Slf4j
@Component
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(name = {
    "org.springframework.web.context.request.RequestContextHolder",
    "jakarta.servlet.http.HttpServletRequest"
})
public class SecurityAuditorAware implements AuditorAware<UUID> {

    // API Gateway가 JWT 검증 후 각 서비스 요청 헤더에 삽입하는 사용자 ID
    private static final String USER_ID_HEADER = "X-User-Id";

    /**
     * 현재 HTTP 요청의 X-User-Id 헤더에서 UUID를 추출하여 반환합니다.
     *
     * Optional.empty()를 반환하는 케이스:
     * - HTTP 요청 컨텍스트가 없는 경우
     * - X-User-Id 헤더가 없거나 비어있는 경우
     * - UUID 형식이 아닌 값이 헤더에 들어온 경우
     */
    @Override
    public Optional<UUID> getCurrentAuditor() {
        try {
            RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

            // 서블릿 환경이 아닌 경우(WebFlux, 비동기, Flyway 등) instanceof 체크로 안전하게 처리
            if (!(requestAttributes instanceof ServletRequestAttributes attributes)) {
                return Optional.empty();
            }

            String userId = attributes.getRequest().getHeader(USER_ID_HEADER);

            if (userId == null || userId.isBlank()) {
                return Optional.empty();
            }

            return Optional.of(UUID.fromString(userId));

        } catch (IllegalArgumentException e) {
            // UUID 형식이 아닌 경우 - Gateway 설정 오류
            log.warn("X-User-Id 헤더가 유효한 UUID 형식이 아닙니다.");
            return Optional.empty();
        }
    }
}
