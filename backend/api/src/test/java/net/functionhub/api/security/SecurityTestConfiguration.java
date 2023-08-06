package net.functionhub.api.security;

import net.functionhub.api.props.PropConfiguration;
import net.functionhub.api.utils.TestUtilConfiguration;
import net.functionhub.api.utils.firebase.FirebaseSDKConfig;
import net.functionhub.api.UserProfile;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import java.util.UUID;

/**
 * @author Bizuwork Melesse
 * created on 1/29/22
 */
@SpringBootConfiguration
@EnableAutoConfiguration(exclude = {
        UserDetailsServiceAutoConfiguration.class
})
@Import({
        FirebaseSDKConfig.class,
        PropConfiguration.class,
        TestUtilConfiguration.class
})
public class SecurityTestConfiguration {

    @Bean
    public UserProfile getUserPrincipal() {
      UserProfile userProfile = new UserProfile();
      userProfile.setUid(UUID.randomUUID().toString().replace("-", ""));
      userProfile.setName("Sideshow Bob");
      userProfile.setEmail("bobby@gmail.com");
      userProfile.setPicture("http://lorem.picsum/200");
        return userProfile;
    }
}
