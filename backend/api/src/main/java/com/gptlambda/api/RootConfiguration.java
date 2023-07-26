package com.gptlambda.api;

import com.gptlambda.api.controller.ControllerConfiguration;
import com.gptlambda.api.data.DataConfiguration;
import com.gptlambda.api.exception.CustomRestExceptionHandler;
import com.gptlambda.api.props.PropConfiguration;
import com.gptlambda.api.service.ServiceConfiguration;
import com.gptlambda.api.utils.migration.FlywayMigration;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author Bizuwork Melesse
 * created on 2/13/21
 */
@Slf4j
@Configuration
@Import({
    ControllerConfiguration.class,
    DataConfiguration.class,
    PropConfiguration.class,
    ServiceConfiguration.class,
    CustomRestExceptionHandler.class,
})
@RequiredArgsConstructor
public class RootConfiguration {
  private final FlywayMigration flywayMigration;

  @PostConstruct
  public void onStart() {
    flywayMigration.migrate(false);
  }
}
