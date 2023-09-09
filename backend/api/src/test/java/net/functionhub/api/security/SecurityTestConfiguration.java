package net.functionhub.api.security;

import net.functionhub.api.data.DataConfiguration;
import net.functionhub.api.props.PropConfiguration;
import net.functionhub.api.service.utils.FHUtils;
import net.functionhub.api.utils.TestUtilConfiguration;
import net.functionhub.api.utils.firebase.FirebaseSDKConfig;
import net.functionhub.api.UserProfile;
import net.functionhub.api.utils.migration.FlywayMigrationConfiguration;
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
    TestUtilConfiguration.class,
    DataConfiguration.class,
    FlywayMigrationConfiguration.class,
})
public class SecurityTestConfiguration {

    @Bean
    public UserProfile getUserPrincipal() {
      UserProfile userProfile = new UserProfile();
      userProfile.userId(FHUtils.generateEntityId("up"));
      userProfile.setUsername(UUID.randomUUID().toString().replace("-", ""));
      userProfile.setName("Sideshow Bob");
      userProfile.setEmail("bobby@gmail.com");
      userProfile.setPicture("http://lorem.picsum/200");
        return userProfile;
    }
}
