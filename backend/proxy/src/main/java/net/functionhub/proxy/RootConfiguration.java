package net.functionhub.proxy;

import java.util.concurrent.Executors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
//import net.functionhub.proxy.data.DataConfiguration;
import net.functionhub.proxy.controller.ControllerConfiguration;
import net.functionhub.proxy.data.DataConfiguration;
import net.functionhub.proxy.props.PropConfiguration;
import net.functionhub.proxy.service.ServiceConfiguration;
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
    DataConfiguration.class,
    PropConfiguration.class,
    ServiceConfiguration.class,
    ControllerConfiguration.class
})
@RequiredArgsConstructor
public class RootConfiguration {

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
