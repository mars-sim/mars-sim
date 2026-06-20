/*
 * Mars Simulation Project
 * DatabaseMetricConfig.java
 * @date 2026-06-18
 * @author Barry Evans
 */
package com.mars_sim.core.metrics.database;

import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

/**
 * Code-based configuration for an in-memory H2 database used to persist metrics.
 * Creates the {@link JdbcTemplate} and initialises the schema without requiring any external
 * configuration files.
 * <p>
 * Each instance owns its own uniquely-named H2 in-memory database ({@code DB_CLOSE_DELAY=-1})
 * so that multiple instances created within the same JVM do not clash.
 * </p>
 * <p>
 * H2 registers its JDBC driver automatically via {@link java.util.ServiceLoader}, so no
 * explicit driver class name is needed.
 * </p>
 */
public class DatabaseMetricConfig {

    private static final String CREATE_METRIC_TABLE =
        "CREATE TABLE METRIC ("
        + "  id                    BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,"
        + "  entity_type           VARCHAR(100)  NOT NULL,"
        + "  entity_id             VARCHAR(255)  NOT NULL,"
        + "  entity_parent_id      VARCHAR(255)  NOT NULL,"   // empty string used for NULL
        + "  category_name         VARCHAR(100)  NOT NULL,"
        + "  category_replace_exist BOOLEAN      NOT NULL,"
        + "  measure               VARCHAR(255)  NOT NULL,"
        + "  CONSTRAINT uq_metric UNIQUE (entity_type, entity_id, entity_parent_id, category_name, measure)"
        + ")";

    private static final String CREATE_DATA_POINT_TABLE =
        "CREATE TABLE DATA_POINT ("
        + "  id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,"
        + "  metric_id        BIGINT  NOT NULL,"
        + "  sol              INT     NOT NULL,"
        + "  total_millisols  DOUBLE  NOT NULL,"
        + "  metric_value     DOUBLE  NOT NULL,"
        + "  CONSTRAINT fk_dp_metric FOREIGN KEY (metric_id) REFERENCES METRIC (id)"
        + ")";

    private static final String CREATE_INDEX =
        "CREATE INDEX idx_dp_metric_sol ON DATA_POINT (metric_id, sol)";

    private static final String CLEAR_DATABASE = "DROP INDEX IF EXISTS idx_dp_metric_sol; DROP TABLE IF EXISTS DATA_POINT; DROP TABLE IF EXISTS METRIC";    
    private final JdbcTemplate jdbcTemplate;

    /**
    * Creates a new configuration, building an in-memory H2 database and initialising
     * the METRIC and DATA_POINT tables.
     */
    public DatabaseMetricConfig(String path) {
        // DB_CLOSE_DELAY=-1 keeps the named in-memory database alive until close() is called.
        DriverManagerDataSource ds = new DriverManagerDataSource(
            "jdbc:h2:" +
            (path != null ? path + "/marssim-metrics" : "mem:marssim") + ";DB_CLOSE_DELAY=-1");
        this.jdbcTemplate = new JdbcTemplate(ds);
        initializeSchema();
    }

    private void initializeSchema() {
        jdbcTemplate.execute(CLEAR_DATABASE);
        jdbcTemplate.execute(CREATE_METRIC_TABLE);
        jdbcTemplate.execute(CREATE_DATA_POINT_TABLE);
        jdbcTemplate.execute(CREATE_INDEX);
    }

    /**
    * Returns the {@link JdbcTemplate} backed by the embedded H2 database.
     *
     * @return the JdbcTemplate
     */
    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    /**
    * Shuts down the in-memory H2 database and releases all resources.
    * After calling this method the configuration and its JdbcTemplate must not be used.
     */
    public void close() {
        jdbcTemplate.execute("SHUTDOWN");
    }
}
