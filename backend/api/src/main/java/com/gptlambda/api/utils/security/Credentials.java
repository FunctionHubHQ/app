package com.gptlambda.api.utils.security;

import com.google.firebase.auth.FirebaseToken;
import com.gptlambda.api.dto.DecodedJwt;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Bizuwork Melesse
 * created on 2/13/22
 */
@Getter @Setter
public class Credentials {
    private String authToken;
    private FirebaseToken decodedFirebaseToken;
    private DecodedJwt decodedJwtToken;
}
