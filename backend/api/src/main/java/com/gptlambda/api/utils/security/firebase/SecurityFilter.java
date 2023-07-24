package com.gptlambda.api.utils.security.firebase;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.gptlambda.api.utils.firebase.FirebaseService;
import com.gptlambda.api.utils.security.Credentials;
import com.gptlambda.api.UserProfile;
import com.gptlambda.api.utils.security.UnsecurePaths;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.util.*;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author Bizuwork Melesse
 * created on 02/13/22
 *
 * Implements a custom user authentication filter using Firebase.
 * If Firebase fails to decode the Bearer token for any reason, the request
 * will fail and a 401 will be returned to the user.
 *
 * In addition to authentication, we also populate the user principal. The user
 * principal is a custom User object that stores all the attributes of the user
 * from Firebase. The most important of these attributes is the user claims.
 * User claims are stored as ROLES and are used to authorize the user to specific
 * services throughout the application.
 *
 * Authorization is a post-filter actions. Overall, there are three levels of security.
 * The first is url pattern- and http method-based authorization of the request. And then
 * the request is filtered down for authentication against Firebase. Finally,
 * authenticated users are authorized to access specific services based on the assigned
 * roles.
 *
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class SecurityFilter extends OncePerRequestFilter {
    private final FirebaseService firebaseService;

  @Override
  protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                                  FilterChain filterChain){
    // All non-preflight requests must have a valid authorization token
    boolean methodExcluded = Stream.of("options")
      .anyMatch(method -> httpServletRequest.getMethod().toLowerCase().contains(method));
    boolean uriExcluded = UnsecurePaths.paths()
      .anyMatch(uri -> httpServletRequest.getRequestURI().toLowerCase().contains(uri));
    if (!(methodExcluded || uriExcluded)) {
      verifyToken(httpServletRequest);
    }
    try {
      filterChain.doFilter(httpServletRequest, httpServletResponse);
    } catch (IOException | ServletException e) {
      throw new RuntimeException(e);
    }
  }

    private void verifyToken(HttpServletRequest httpServletRequest) {
        try {
            String bearerToken = getBearerToken(httpServletRequest);
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(bearerToken);
            UserProfile user = firebaseTokenToUser(decodedToken);
            Credentials credentials = new Credentials();
            credentials.setDecodedToken(decodedToken);
            credentials.setAuthToken(bearerToken);
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(user, credentials,
                    getAuthorities(user.getRoles()));
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(httpServletRequest));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (IllegalArgumentException | FirebaseAuthException | IOException e) {
            log.error("SecurityFilter.verifyToken Authentication Error: {}", e.getLocalizedMessage());
        }
    }

    private UserProfile firebaseTokenToUser(FirebaseToken decodedToken) {
      UserProfile user = new UserProfile();
        if (decodedToken != null) {
            user.setUid(decodedToken.getUid());
            user.setName(decodedToken.getName());
            user.setEmail(decodedToken.getEmail());
            user.setPicture(decodedToken.getPicture());
            Map<String, Boolean> parsedClaims = new HashMap<>();
            final Map<String, Object> claimsToParse = decodedToken.getClaims();
            for (Map.Entry<String, Object> entry : claimsToParse.entrySet()) {
                if (entry.getKey().startsWith("ROLE_")) {
                    parsedClaims.put(entry.getKey(), (Boolean) entry.getValue());
                }
            }
            user.setRoles(parsedClaims);
        }
        return user;
    }

    private String getBearerToken(HttpServletRequest httpServletRequest) throws IOException {
        String bearerToken = "";
        String authorization = httpServletRequest.getHeader("Authorization");
        if (StringUtils.hasText(authorization)) {
            if (authorization.startsWith("Bearer ")) {
                bearerToken = authorization.substring(7);
            } else if (authorization.startsWith("Token ")) {
                bearerToken = authorization.substring(6);
            } else if (authorization.startsWith("Basic ")) {
                String credentials = new String(Base64.getDecoder().decode(authorization.substring(6)), UTF_8);
                String email = credentials.split(":")[0];
                String password = credentials.split(":")[1];
                bearerToken = firebaseService.login(email, password);
            } else {
                bearerToken = authorization;
            }
        }
        return bearerToken;
    }

    private Collection<GrantedAuthority> getAuthorities(Map<String, Boolean> claims) {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        for (Map.Entry<String, Boolean> claim: claims.entrySet()) {
            if (claim.getKey().startsWith("ROLE_") && claim.getValue()) {
                authorities.add(new SimpleGrantedAuthority(claim.getKey()));
            }
        }
        return authorities;
    }

    public UserProfile getUser() {
        UserProfile userProfile = null;
        SecurityContext securityContext = SecurityContextHolder.getContext();
        Object principal = securityContext.getAuthentication().getPrincipal();
        if (principal instanceof UserProfile) {
          userProfile = (UserProfile) principal;
        }
        return userProfile;
    }

    public Credentials getCredentials() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        return (Credentials) securityContext.getAuthentication().getCredentials();
    }
}
