package com.gptlambda.api.utils.security.firebase;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gptlambda.api.props.CorsProps;
import com.gptlambda.api.utils.security.firewall.FirewallConfiguration;
import com.gptlambda.api.utils.security.UnsecurePaths;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.autoconfigure.security.reactive.ReactiveManagementWebSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * @author Bizuwork Melesse
 * created on 8/21/21
 *
 * Configures general application authentication and specifies which HTTP methods and
 * URIs should be excluded. We exclude the pre-flight OPTIONS method and /health
 * endpoint from this check. All the requests passing this initial check are passed
 * down the security filter chain, where we authenticate the user per request.
 *
 * Include {@link FirebaseSecurityConfiguration} in the target application's main
 * application configuration to enable Firebase Authentication. To enable basic
 * username+password authentication, include BasicSecurityConfiguration instead.
 */
@EnableWebSecurity
@EnableGlobalMethodSecurity(
        securedEnabled = true,
        jsr250Enabled = true,
        prePostEnabled = true)
@EnableAutoConfiguration(exclude = {
        ReactiveManagementWebSecurityAutoConfiguration.class,
        ReactiveSecurityAutoConfiguration.class
})
@Import(FirewallConfiguration.class)
@ComponentScan
@RequiredArgsConstructor
public class FirebaseSecurityConfiguration {

    private final SecurityFilter securityFilter;
    private final ObjectMapper objectMapper;
    private final CorsProps corsProps;
    private final UnsecurePaths unsecurePaths;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
        AuthenticationEntryPoint authenticationEntryPoint) throws Exception {
        http.cors(AbstractHttpConfigurer::disable)
            .csrf(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .exceptionHandling(ex -> ex.authenticationEntryPoint(authenticationEntryPoint))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers(unsecurePaths.wildcardPaths()).permitAll()
                .anyRequest().authenticated())
            .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class)
            .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        return http.build();
    }


    @Bean
    public FilterRegistrationBean<CorsFilter> corsConfigurationSource() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowedHeaders(List.of("*"));
        corsConfiguration.setAllowedOriginPatterns(corsProps.getAllowedOriginPatterns()
            .stream()
            .toList());
        corsConfiguration.setAllowedMethods(List.of("*"));
        corsConfiguration.setExposedHeaders(List.of("*"));
        corsConfiguration.setAllowCredentials(true);
        corsConfiguration.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);
        FilterRegistrationBean<CorsFilter> filter = new FilterRegistrationBean<>(new CorsFilter(source));
        filter.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return filter;
    }

    @Bean
    public AuthenticationEntryPoint restAuthenticationEntryPoint() {
        return (httpServletRequest, httpServletResponse, e) -> {
            Map<String, Object> errorObject = new HashMap<>();
            int errorCode = 401;
            errorObject.put("message","Unauthorized access of protected resource");
            errorObject.put("error", HttpStatus.UNAUTHORIZED);
            errorObject.put("code", errorCode);
            errorObject.put("timestamp", OffsetDateTime.now());
            httpServletResponse.setContentType("application/json;charset=UTF-8");
            httpServletResponse.setStatus(errorCode);
            httpServletResponse.getWriter().write(objectMapper.writeValueAsString(errorObject));
        };
    }
}
