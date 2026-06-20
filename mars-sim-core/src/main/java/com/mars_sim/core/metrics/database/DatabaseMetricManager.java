/*
 * Mars Simulation Project
 * DatabaseMetricManager.java
 * @date 2026-06-19
 * @author Barry Evans
 */
package com.mars_sim.core.metrics.database;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.io.File;
import java.sql.PreparedStatement;
import java.sql.Statement;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;

import com.mars_sim.core.Entity;
import com.mars_sim.core.EntityIdentifier;
import com.mars_sim.core.EntityResolver;
import com.mars_sim.core.Simulation;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.metrics.Metric;
import com.mars_sim.core.metrics.MetricCategory;
import com.mars_sim.core.metrics.MetricKey;
import com.mars_sim.core.metrics.MetricManager;

/**
 * Database-backed metric manager that creates metrics in a H2 database.
 */
public class DatabaseMetricManager extends MetricManager {

    private static SimLogger logger = SimLogger.getLogger(DatabaseMetricManager.class.getName());
    private static final long serialVersionUID = 1L;
    private static final String NULL_PARENT_SENTINEL = "";
      
    private transient JdbcTemplate jdbcTemplate;
    private String dbPath;

    /**
     * Creates a new DatabaseMetricManager with the given database path.
     * If the path is null, an in-memory database is used.
     * @param dbPath Optional path where database is stored.
     */
    public DatabaseMetricManager(String dbPath) {
        super();
        this.dbPath = dbPath;

        reinit();
    }

    /**
     * Reconnect the manager with the underlying database.
     */
    @Override
    public void reinit() {
        if (dbPath == null) {
            logger.info("Open in-memory database");
        } else {
            var path = new File(dbPath);
            if (!path.exists()) {
                logger.info("Creating database directory: " + dbPath);
                path.mkdirs();
            }
            else {
                logger.info("Using existing database directory: " + dbPath);
            }
        }
        logger.info("Open database with dbPath: " + dbPath);
        var config = new DatabaseMetricConfig(dbPath);
        this.jdbcTemplate = config.getJdbcTemplate();
    }

    /**
     * Retrieves all metrics from the database.
     * @return A set of MetricKey objects representing the metrics.
     */
    @Override
    public Set<MetricKey> getMetrics() {
        List<MetricKey> rows = jdbcTemplate.query(
            "SELECT entity_type, entity_id, entity_parent_id, category_name, category_replace_exist, measure "
            + "FROM METRIC ORDER BY entity_type, entity_id, entity_parent_id, category_name, measure",
            (rs, rowNum) -> {
                EntityIdentifier identifier = new EntityIdentifier(
                    rs.getString("entity_type"),
                    rs.getString("entity_id"),
                    normalizeParentId(rs.getString("entity_parent_id")));
                try {
                    Entity entity = EntityResolver.resolve(Simulation.instance(), identifier);
                    MetricCategory category = new MetricCategory(
                        rs.getString("category_name"),
                        rs.getBoolean("category_replace_exist"));
                    return new MetricKey(entity, category, rs.getString("measure"));
                }
                catch (RuntimeException _) {
                    logger.warning("Failed to resolve metric entity '" + identifier + "'");
                    return null;
                }
            });

        Set<MetricKey> metrics = new LinkedHashSet<>();
        for (MetricKey key : rows) {
            if (key != null) {
                metrics.add(key);
            }
        }
        return metrics;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public List<MetricCategory> getCategories(Entity asset) {
        RowMapper<MetricCategory> rowMapper = (rs, rowNum) -> new MetricCategory(
            rs.getString("category_name"),
            rs.getBoolean("category_replace_exist"));

        List<MetricCategory> rows = null;
        if (asset != null) {
            var assetId = asset.getEntityIdentifier();
            rows = jdbcTemplate.query(
                "SELECT DISTINCT category_name, category_replace_exist FROM METRIC WHERE entity_type = ? AND entity_id = ? AND COALESCE(entity_parent_id, '') = ? ORDER BY category_name",
                rowMapper,
                assetId.type(),
                assetId.id(),
                normalizeParentId(assetId.parentId()));
        }
        else {
            rows = jdbcTemplate.query(
                "SELECT DISTINCT category_name, category_replace_exist FROM METRIC ORDER BY category_name",
                rowMapper);
        }

        Map<String, MetricCategory> categories = new LinkedHashMap<>();
        for (MetricCategory category : rows) {
            categories.putIfAbsent(category.getName(), category);
        }

        return new ArrayList<>(categories.values());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Entity> getEntities(MetricCategory cat) {
        RowMapper<Entity> rowMapper = (rs, rowNum) -> {
            EntityIdentifier identifier = new EntityIdentifier(
                rs.getString("entity_type"),
                rs.getString("entity_id"),
                normalizeParentId(rs.getString("entity_parent_id")));
            try {
                return EntityResolver.resolve(Simulation.instance(), identifier);
            }
            catch (RuntimeException _) {
                // Skip entities that are no longer resolvable in the current simulation.
                logger.warning("Failed to resolve entity '" + identifier);
                return null;
            }
        };

        List<Entity> rows = null;
        if (cat != null) {
            rows = jdbcTemplate.query(
                "SELECT DISTINCT entity_type, entity_id, entity_parent_id FROM METRIC WHERE category_name = ? ORDER BY entity_type, entity_id, entity_parent_id",
                rowMapper,
                cat.getName());
        }
        else {
            rows = jdbcTemplate.query(
                "SELECT DISTINCT entity_type, entity_id, entity_parent_id FROM METRIC ORDER BY entity_type, entity_id, entity_parent_id",
                rowMapper);
        }

        return rows.stream()
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getMeasures(Entity asset, MetricCategory cat) {
        if (asset == null) {
            throw new IllegalArgumentException("Asset cannot be null when retrieving measures.");
        }
        if (cat == null) {
            throw new IllegalArgumentException("Category cannot be null when retrieving measures.");
        }
        
        var assetId = asset.getEntityIdentifier();
        return jdbcTemplate.query(
            "SELECT DISTINCT measure FROM METRIC WHERE entity_type = ? AND entity_id = ? AND COALESCE(entity_parent_id, '') = ? AND category_name = ? ORDER BY measure",
            (rs, rowNum) -> rs.getString("measure"),
            assetId.type(),
            assetId.id(),
            normalizeParentId(assetId.parentId()),
            cat.getName());
    }

    /**
     * Searches the database for a matching metric based on the provided key.
     * @param key Key identifying the metric to find
     * @return The metric if found, or {@code null} if no matching metric exists    
     */
    private Metric findMetric(MetricKey key) {
        EntityIdentifier eid = key.asset().getEntityIdentifier();
        String entityType     = eid.type();
        String entityId       = eid.id();
        String entityParentId = normalizeParentId(eid.parentId());
        String categoryName   = key.category().getName();
        String measure        = key.measure();

        List<Long> ids = jdbcTemplate.query(
            "SELECT id FROM METRIC "
            + "WHERE entity_type = ? AND entity_id = ? AND COALESCE(entity_parent_id, '') = ? "
            + "  AND category_name = ? AND measure = ?",
            (rs, rowNum) -> rs.getLong("id"),
            entityType, entityId, entityParentId, categoryName, measure);

        if (ids.isEmpty()) {
            return null;
        } else {
            Long metricId = ids.get(0);
            return new DatabaseMetric(key, metricId, this);
        }
    }

    /**
     * Find the specifc Metric from the database by it's key.
     */
    @Override
    public Metric getMetric(MetricKey key) {
        Metric m = findMetric(key);
        if (m == null) {
            m = insertMetric(key, false);
            notifyListeners(m);
        }
        return m;
    }

    /**
     * Inserts a new row into the {@code METRIC} table and returns the generated primary key.
     */
    private Metric insertMetric(MetricKey key, boolean replaceExist) {
        var keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                "INSERT INTO METRIC "
                + "(entity_type, entity_id, entity_parent_id, category_name, category_replace_exist, measure) "
                + "VALUES (?, ?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS);
            
            var assetId = key.asset().getEntityIdentifier();
            ps.setString(1, assetId.type());
            ps.setString(2, assetId.id());
            ps.setString(3, normalizeParentId(assetId.parentId()));
            ps.setString(4, key.category().getName());
            ps.setBoolean(5, replaceExist);
            ps.setString(6, key.measure());
            return ps;
        }, keyHolder);

        var dbKey = keyHolder.getKey();
        if (dbKey == null) {
            throw new IllegalStateException("Failed to insert metric into database.");
        }
        return new DatabaseMetric(key, dbKey.longValue(), this);
    }

    private String normalizeParentId(String parentId) {
        return parentId != null ? parentId : NULL_PARENT_SENTINEL;
    }

    JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }
}
