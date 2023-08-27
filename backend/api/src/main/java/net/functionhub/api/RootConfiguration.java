package net.functionhub.api;

import net.functionhub.api.controller.ControllerConfiguration;
import net.functionhub.api.data.DataConfiguration;
import net.functionhub.api.props.PropConfiguration;
import net.functionhub.api.props.SourceProps;
import net.functionhub.api.service.ServiceConfiguration;
import net.functionhub.api.service.utils.SeedData;
import net.functionhub.api.utils.migration.FlywayMigration;
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
    ServiceConfiguration.class
})
@RequiredArgsConstructor
public class RootConfiguration {
  private final FlywayMigration flywayMigration;
  private final SeedData seedData;
  private final SourceProps sourceProps;

  @PostConstruct
  public void onStart() {
    flywayMigration.migrate(false);
    if (!sourceProps.getProfile().equals("prod")) {
      seedData.generateSeedData();
    }
  }
}
