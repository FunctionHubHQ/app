package net.functionhub.api.utils.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.functionhub.api.dto.DecodedJwt;
import net.functionhub.api.props.JwtProps;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

/**
 * @author Bizuwork Melesse
 * created on 5/11/22
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtValidationServiceImpl implements JwtValidationService {
    private final ObjectMapper objectMapper;
    private final JwtProps jwtProps;

    @Override
    public DecodedJwt verifyToken(String jwtToken) throws Exception {
        Claims claims = Jwts.parser().setSigningKey(jwtProps.getSecret().getBytes(StandardCharsets.UTF_8))
            .parseClaimsJws(jwtToken).getBody();
        String payload = claims.get("payload").toString();
        DecodedJwt decodedJwt = objectMapper.readValue(payload, DecodedJwt.class);
        decodedJwt.setAuthToken(jwtToken);
        assert !ObjectUtils.isEmpty(decodedJwt.getJuid());
        assert !ObjectUtils.isEmpty(decodedJwt.getUserId());
        assert decodedJwt.getIat() <=  OffsetDateTime.now()
                .atZoneSameInstant(ZoneOffset.UTC).toEpochSecond();
        assert decodedJwt.getExp() >  OffsetDateTime.now()
                .atZoneSameInstant(ZoneOffset.UTC).toEpochSecond();
        return decodedJwt;
    }
}
