package net.functionhub.api.controller;

import net.functionhub.api.RootConfiguration;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;


/**
 * @author Bizuwork Melesse
 * created on 07/30/23
 */
@EnableAutoConfiguration(exclude = {
        FlywayAutoConfiguration.class
})
@SpringBootConfiguration
@Configuration
@Import({
       RootConfiguration.class
})
@RequiredArgsConstructor
public class ControllerTestConfiguration {
}
