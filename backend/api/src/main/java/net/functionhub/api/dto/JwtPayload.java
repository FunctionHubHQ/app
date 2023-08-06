package net.functionhub.api.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Bizuwork Melesse
 * created on 5/11/22
 */
@Getter
@Setter
public class JwtPayload {

    /**
     * Token expiration timestamp
     */
    private Long exp;

    /**
     * Token issuance timestamp
     */
    private Long iat;

    /**
     * JWT unique identifier
     */
    private String juid;

    /**
     * user ID
     */
    private String guid;
}
