package net.functionhub.api.utils.migration;

import net.functionhub.api.props.FlywayProps;
import net.functionhub.api.props.PostgresDataSourceProps;
import net.functionhub.api.props.SourceProps;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

/**
 * @author Bizuwork Melesse
 * created on 2/13/21
 *
 * <p>
 *     Performs schema migration
 * </p>
 */
@Slf4j
@Transactional
@Component
@RequiredArgsConstructor
public class FlywayMigration {
    private final PostgresDataSourceProps dataSourceProps;
    private final SourceProps sourceProps;
    private final FlywayProps flywayProps;
    private final List<String> devProfiles = Arrays.asList("dev", "test");

    /**
     * Perform schema migration. Drop all schemas if dropSchema is true.
     * Ensure that schemas cannot be dropped by checking that the client
     * calling to drop any schema is an integration test.
     *
     * @param dropSchema
     */
    public void migrate(boolean dropSchema) {
        FluentConfiguration flyway = Flyway.configure()
                .schemas(flywayProps.getSchemas().split(","))
                .cleanDisabled(flywayProps.getCleanDisabled())
                .validateOnMigrate(true)
                .baselineOnMigrate(true)
                .dataSource(dataSourceProps.getUrl(),
                        dataSourceProps.getUsername(),
                        dataSourceProps.getPassword());
        log.info("Performing schema migration");
        flyway.load().clean();
        if (dropSchema) {
            if (!devProfiles.contains(sourceProps.getProfile())) {
                throw new IllegalArgumentException("Cannot drop schema for non-dev profile");
            }

            StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
            boolean isIntegrationTest = false;
            for (StackTraceElement element : stackTraceElements) {
                if (element.getClassName().contains("IntegrationTest")) {
                    isIntegrationTest = true;
                    break;
                }
            }
            if (isIntegrationTest) {
                flyway.load().clean();
            } else {
                throw new IllegalArgumentException("Dropping schema is not allowed outside of integration tests. " +
                        "If this is an integration test, please make sure that the test class name has the suffix " +
                        "'IntegrationTest'");
            }
        }
        flyway.locations(flywayProps.getLocations()).load().migrate();
    }
}



