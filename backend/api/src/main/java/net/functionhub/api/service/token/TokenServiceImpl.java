package net.functionhub.api.service.token;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.functionhub.api.UserProfile;
import net.functionhub.api.dto.JwtPayload;
import net.functionhub.api.props.JwtProps;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * @author Bizuwork Melesse
 * created on 5/11/22
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {
    private final JwtProps jwtProps;
    private final ObjectMapper objectMapper;

    @Override
    public String generateJwtToken() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("payload", getPayload());

        Map<String, Object> headers = new HashMap<>();
        headers.put("alg", SignatureAlgorithm.HS512);
        headers.put("typ", Header.JWT_TYPE);

        return Jwts.builder()
                    .setClaims(claims)
                    .setHeader(headers)
                    .signWith(
                        SignatureAlgorithm.HS512,
                        jwtProps.getSecret()
                            .getBytes(StandardCharsets.UTF_8))
                    .compact();
    }

    @SneakyThrows
    private String getPayload() {
        UserProfile user = (UserProfile) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        JwtPayload payload = new JwtPayload();
        payload.setJuid(UUID.randomUUID().toString());
        payload.setGuid(user.getUid());
        payload.setIat(
                OffsetDateTime.now()
                        .atZoneSameInstant(ZoneOffset.UTC).toEpochSecond());


       // Default expiration is the TTL specified in the configuration
        payload.setExp(OffsetDateTime.now()
                .atZoneSameInstant(ZoneOffset.UTC)
            .plusDays(jwtProps.getTtlDays())
            .toEpochSecond());
        return objectMapper.writeValueAsString(payload);
    }
}
