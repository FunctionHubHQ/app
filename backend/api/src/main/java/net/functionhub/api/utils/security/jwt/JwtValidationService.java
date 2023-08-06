package net.functionhub.api.utils.security.jwt;


import net.functionhub.api.dto.DecodedJwt;

/**
 * @author Bizuwork Melesse
 * created on 5/11/22
 */
public interface JwtValidationService {

    /**
     * Validate a JWT token. Do not handle any exceptions
     * so that errors are propagated up the filter as 401.
     *
     * The JWT token is invalid if the signature verification or payload
     * decryption fails. In addition to these validations, assert that
     * the token has not yet expired.
     *
     * @param jwtToken
     * @return
     */
    DecodedJwt verifyToken(String jwtToken) throws Exception;
}