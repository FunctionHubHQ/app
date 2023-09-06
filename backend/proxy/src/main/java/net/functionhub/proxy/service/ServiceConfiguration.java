package net.functionhub.proxy.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.functionhub.proxy.data.DataConfiguration;
import net.functionhub.proxy.props.PropConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * @author Bizuwork Melesse
 * created on 2/13/21
 *
 */
@Slf4j
@Configuration
@ComponentScan
@RequiredArgsConstructor
@EnableAsync
@Import({
    DataConfiguration.class,
    PropConfiguration.class})
public class ServiceConfiguration {
}
