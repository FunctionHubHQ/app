package net.functionhub.api.utils.security.firebase;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import jakarta.servlet.ServletException;
import net.functionhub.api.data.postgres.entity.ApiKeyEntity;
import net.functionhub.api.data.postgres.entity.UserEntity;
import net.functionhub.api.data.postgres.projection.UserProjection;
import net.functionhub.api.data.postgres.repo.ApiKeyRepo;
import net.functionhub.api.data.postgres.repo.EntitlementRepo;
import net.functionhub.api.data.postgres.repo.UserRepo;
import net.functionhub.api.dto.DecodedJwt;
import net.functionhub.api.dto.SessionUser;
import net.functionhub.api.service.user.UserService;
import net.functionhub.api.service.user.UserService.AuthMode;
import net.functionhub.api.service.utils.FHUtils;
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
import org.springframework.core.annotation.Order;
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
@Slf4j
@Component
@RequiredArgsConstructor
public class SecurityFilter extends OncePerRequestFilter {
    private final FirebaseService firebaseService;
    private final UnsecurePaths unsecurePaths;
    private final JwtValidationService jwtValidationService;
    private final UserRepo userRepo;
    private final ApiKeyRepo apiKeyRepo;
    private final EntitlementRepo entitlementRepo;


  @Override
  protected void doFilterInternal(HttpServletRequest httpServletRequest,
      @NotNull HttpServletResponse httpServletResponse,
      @NotNull FilterChain filterChain) throws IOException, ServletException {
    // All non-preflight requests must have a valid authorization token
    boolean methodExcluded = Stream.of("options")
      .anyMatch(method -> httpServletRequest.getMethod().toLowerCase().contains(method));
    if (httpServletRequest.getRequestURI().toLowerCase().contains("/favicon.ico")) {
      return;
    }
    boolean uriExcluded = unsecurePaths.allow(httpServletRequest.getRequestURI());
    if (!(methodExcluded || uriExcluded ||
        unsecurePaths.allowedOrigin(httpServletRequest.getRemoteHost(),
            httpServletRequest.getRequestURI()))) {
      verifyToken(httpServletRequest);
    }
    filterChain.doFilter(httpServletRequest, httpServletResponse);
  }

  private void verifyToken(HttpServletRequest httpServletRequest) throws IOException {
      String bearerToken = getBearerToken(httpServletRequest);
      SessionUser user = null;
      Credentials credentials = new Credentials();
      if (bearerToken.startsWith(UserService.apiKeyPrefix)) {
        user = new SessionUser();
        if (bearerToken.contains("internal")) {
          assert httpServletRequest.getRequestURI().equals("/log") &&
              httpServletRequest.getRemoteHost().equals("127.0.0.1");
          user.setApiKey(bearerToken);
          user.setName("Internal");
        } else {
          FHUtils.populateSessionUser(userRepo.findByApiKey(bearerToken), user);
          user.setAuthMode(AuthMode.AK);
        }
      } else {
        try {
          DecodedJwt decodedToken = jwtValidationService.verifyToken(bearerToken);
          user = jwtTokenToUser(decodedToken);
          user.setAuthMode(AuthMode.JWT);
          credentials.setDecodedJwtToken(decodedToken);
          credentials.setAuthToken(bearerToken);
        } catch (Exception e) {
          FirebaseToken decodedToken = null;
          try {
            decodedToken = FirebaseAuth.getInstance().verifyIdToken(bearerToken);
          } catch (FirebaseAuthException ex) {
            throw new RuntimeException(ex.getMessage());
          }
          user = firebaseTokenToUser(decodedToken);
          FHUtils.populateSessionUser(userRepo.findProjectionByUid(user.getUid()), user);
          user.setAuthMode(AuthMode.FB);
          // TODO 2 db calls with the one above so not very efficient for production use. This is why
          //    prod should use api key instead of firebase tokens

          // Arbitrarily set an api key since we're using a non-api key auth method
          ApiKeyEntity apiKeyEntity = apiKeyRepo.findOldestApiKey(user.getUid());
          if (apiKeyEntity != null) {
            // userEntity could be null if this is the registration flow
            user.setApiKey(apiKeyEntity.getApiKey());
          }

          credentials.setAuthToken(bearerToken);
          credentials.setDecodedFirebaseToken(decodedToken);
        }
      }
    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(user, credentials,
              getAuthorities(user.getRoles()));
      authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(httpServletRequest));
      SecurityContextHolder.getContext().setAuthentication(authentication);
    }


  private SessionUser jwtTokenToUser(DecodedJwt decodedToken) {
    SessionUser user = new SessionUser();
    user.setUid(decodedToken.getUserId());
    return user;
  }

  private SessionUser firebaseTokenToUser(FirebaseToken decodedToken) {
    SessionUser user = new SessionUser();
      if (decodedToken != null) {
          user.setUid(decodedToken.getUid());
          user.setName(decodedToken.getName());
          user.setEmail(decodedToken.getEmail());
          user.setAvatar(decodedToken.getPicture());
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
      } else if (httpServletRequest.getRequestURI().startsWith("/npm")) {
        String[] tokens = httpServletRequest.getRequestURI().split("apiKey=");
        bearerToken = tokens[tokens.length - 1];
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
}
