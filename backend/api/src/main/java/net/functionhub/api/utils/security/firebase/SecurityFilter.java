package net.functionhub.api.utils.security.firebase;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import net.functionhub.api.data.postgres.entity.UserEntity;
import net.functionhub.api.data.postgres.repo.UserRepo;
import net.functionhub.api.dto.DecodedJwt;
import net.functionhub.api.service.user.UserService;
import net.functionhub.api.service.user.UserService.AuthMode;
import net.functionhub.api.utils.firebase.FirebaseService;
import net.functionhub.api.utils.security.Credentials;
import net.functionhub.api.UserProfile;
import net.functionhub.api.utils.security.UnsecurePaths;
import net.functionhub.api.utils.security.jwt.JwtValidationService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
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
    private final UnsecurePaths unsecurePaths;
    private final JwtValidationService jwtValidationService;
    private final UserRepo userRepo;

  @Override
  protected void doFilterInternal(HttpServletRequest httpServletRequest,
      @NotNull HttpServletResponse httpServletResponse,
      @NotNull FilterChain filterChain) throws IOException {
    // All non-preflight requests must have a valid authorization token
    boolean methodExcluded = Stream.of("options")
      .anyMatch(method -> httpServletRequest.getMethod().toLowerCase().contains(method));
    boolean uriExcluded = unsecurePaths.allow(httpServletRequest.getRequestURI());
    if (!(methodExcluded || uriExcluded ||
        unsecurePaths.allowedOrigin(httpServletRequest.getRemoteHost(),
            httpServletRequest.getRequestURI()))) {
      verifyToken(httpServletRequest);
    }
    try {
      filterChain.doFilter(httpServletRequest, httpServletResponse);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private void verifyToken(HttpServletRequest httpServletRequest) throws IOException {
      String bearerToken = getBearerToken(httpServletRequest);
      UserProfile user = null;
      Credentials credentials = new Credentials();
      if (bearerToken.startsWith(UserService.apiKeyPrefix)) {
        UserEntity userEntity = userRepo.findByApiKey(bearerToken);
        user = new UserProfile();
        user.setEmail(userEntity.getEmail());
        user.setName(userEntity.getFullName());
        user.setUid(userEntity.getUid());
        user.setRoles(new HashMap<>());
        user.setPicture(userEntity.getAvatarUrl());
        user.setApiKey(userEntity.getApiKey());
        user.setAuthMode(AuthMode.AK.name());
      } else {
        try {
          DecodedJwt decodedToken = jwtValidationService.verifyToken(bearerToken);
          user = jwtTokenToUser(decodedToken);
          user.setAuthMode(AuthMode.JWT.name());
          credentials.setDecodedJwtToken(decodedToken);
          credentials.setAuthToken(bearerToken);
        } catch (Exception e) {
          FirebaseToken decodedToken = null;
          try {
            decodedToken = FirebaseAuth.getInstance().verifyIdToken(bearerToken);
          } catch (FirebaseAuthException ex) {
            throw new RuntimeException(ex);
          }
          user = firebaseTokenToUser(decodedToken);
          user.setAuthMode(AuthMode.FB.name());
          credentials.setAuthToken(bearerToken);
          credentials.setDecodedFirebaseToken(decodedToken);
          UserEntity userEntity = userRepo.findByUid(user.getUid());
          if (userEntity != null) {
            // userEntity could be null if this is the registration flow
            user.setApiKey(userEntity.getApiKey());
          }
        }
      }
    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(user, credentials,
              getAuthorities(user.getRoles()));
      authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(httpServletRequest));
      SecurityContextHolder.getContext().setAuthentication(authentication);
    }

  private UserProfile jwtTokenToUser(DecodedJwt decodedToken) {
    return new UserProfile()
        .uid(decodedToken.getUserId())
        .roles(new HashMap<>());
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

  private String getBearerToken(@NotNull HttpServletRequest httpServletRequest) throws IOException {
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
      } else if (httpServletRequest.getRequestURI().startsWith("/spec")) {
        String[] tokens = httpServletRequest.getRequestURI().split("/");
        bearerToken = tokens[tokens.length - 2];
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
