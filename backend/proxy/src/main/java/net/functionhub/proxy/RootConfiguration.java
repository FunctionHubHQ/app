package net.functionhub.proxy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
//import net.functionhub.proxy.data.DataConfiguration;
import net.functionhub.proxy.props.PropConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author Bizuwork Melesse
 * created on 2/13/21
 */
@Slf4j
@Configuration
@Import({
//    DataConfiguration.class,
    PropConfiguration.class,
})
@RequiredArgsConstructor
public class RootConfiguration {
}
