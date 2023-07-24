package com.gptlambda.api.utils.firebase;

import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * @author Bizuwork Melesse
 * created on 2/13/21
 */
public interface FirebaseService {

    UserRecord createUser(String email, String password, String phoneNumber, String displayName, String photoUrl)
        throws FirebaseAuthException;

    UserRecord getUserByEmail(String email);

    String getIdToken(String uid) throws FirebaseAuthException;

    void deleteUser(String uid) throws FirebaseAuthException;

    void createCustomClaims(String uid, List<String> claims);

    void updateCustomClaims(String uid, List<String> claims);

    String login(String email, String password) throws IOException;

    UserRecord getUserByPhoneNumber(String phone);

}
