package net.functionhub.api;

import java.util.concurrent.Executors;
import net.functionhub.api.controller.ControllerConfiguration;
import net.functionhub.api.data.DataConfiguration;
import net.functionhub.api.props.PropConfiguration;
import net.functionhub.api.props.SourceProps;
import net.functionhub.api.service.ServiceConfiguration;
import net.functionhub.api.service.user.UserService;
import net.functionhub.api.service.utils.SeedData;
import net.functionhub.api.utils.migration.FlywayPostgresMigration;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.boot.web.embedded.tomcat.TomcatProtocolHandlerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.support.TaskExecutorAdapter;

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
  private final FlywayPostgresMigration flywayPostgresMigration;
  private final SeedData seedData;
  private final SourceProps sourceProps;
  private final UserService userService;

  @PostConstruct
  public void onStart() {
    flywayPostgresMigration.migrate(false);
    userService.createAnonymousUser();
    if (!sourceProps.getProfile().equals("prod")) {
//      seedData.generateSeedData();
    }
  }

  @Bean(TaskExecutionAutoConfiguration.APPLICATION_TASK_EXECUTOR_BEAN_NAME)
  public AsyncTaskExecutor asyncTaskExecutor() {
    return new TaskExecutorAdapter(Executors.newVirtualThreadPerTaskExecutor());
  }

  @Bean
  public TomcatProtocolHandlerCustomizer<?> protocolHandlerVirtualThreadExecutorCustomizer() {
    return protocolHandler -> {
      protocolHandler.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
    };
  }
}
