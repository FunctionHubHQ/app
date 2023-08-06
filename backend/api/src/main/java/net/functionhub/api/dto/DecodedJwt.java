package net.functionhub.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Bizuwork Melesse
 * created on 5/11/22
 */
@Getter
@Setter
public class DecodedJwt {
    /**
     * Token expiration timestamp
     */
    private Long exp;

    /**
     * Token issuance timestamp
     */
    private Long iat;

    private String authToken;

    /**
     * JWT unique identifier
     */
    private String juid;

    @JsonProperty("guid")
    private String userId;
}
