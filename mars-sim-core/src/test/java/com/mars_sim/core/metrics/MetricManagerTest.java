package com.mars_sim.core.metrics;

import static org.junit.jupiter.api.Assertions.*;

import com.mars_sim.core.Entity;
import com.mars_sim.core.metrics.database.DatabaseMetricManager;
import com.mars_sim.core.test.MarsSimUnitTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

/**
 * Unit tests for the MetricManager class.
 */
@DisplayName("MetricManager Tests")
class MetricManagerTest extends MarsSimUnitTest {
    private static final MetricCategory TEMP_CAT = new MetricCategory("Temperature");
    private static final MetricCategory PRES_CAT = new MetricCategory("Pressure");

    private MetricManager manager;

    private String measure1;
    private String measure2;
    
    @BeforeEach
    void setUp() {
        
        // Create an in memory DatabaseMetricManager for testing
        manager = new DatabaseMetricManager(null);
        
        measure1 = "Average";
        measure2 = "Maximum";
    }


    @Test
    @DisplayName("Constructor should create empty MetricManager")
    void testConstructor() {
        
        // Then
        assertNotNull(manager);
        assertTrue(manager.getMetrics().isEmpty());
        assertTrue(manager.getEntities(null).isEmpty());
        assertTrue(manager.getCategories(null).isEmpty());
    }
    
    @Test
    @DisplayName("getMetric should create new metric when it doesn't exist")
    void testGetMetricCreatesNew() {
        
        var s = buildSettlement("Test");
        Metric metric = manager.getMetric(s, TEMP_CAT, measure1);
        
        // Then
        assertNotNull(metric);
        assertEquals(s, metric.getKey().asset());
        assertEquals(TEMP_CAT, metric.getKey().category());
        assertEquals(measure1, metric.getKey().measure());
    }
    
    @Test
    @DisplayName("getMetric should return existing metric when it exists")
    void testGetMetricReturnsExisting() {
        // Given
        var s = buildSettlement("Test");
        Metric metric1 = manager.getMetric(s, TEMP_CAT, measure1);
        
        // When
        Metric metric2 = manager.getMetric(s, TEMP_CAT, measure1);
        
        // Then
        assertEquals(metric1, metric2);
    }
    
    @Test
    @DisplayName("getCategories should return empty list for entity with no metrics")
    void testGetCategoriesEmptyForNewEntity() {
        // Given
        var s = buildSettlement("Test");
        
        // When
        var categories = manager.getCategories(s);
        
        // Then
        assertTrue(categories.isEmpty());
    }
    
    @Test
    @DisplayName("getCategories should return categories for entity")
    void testGetCategoriesForEntity() {
        // Given
        var s = buildSettlement("Test");
        manager.getMetric(s, TEMP_CAT, measure1);
        manager.getMetric(s, PRES_CAT, measure1);
        var s2 = buildSettlement("Test2");
        manager.getMetric(s2, TEMP_CAT, measure1); // Different entity
        
        // When
        var categories = manager.getCategories(s);
        
        // Then
        assertEquals(2, categories.size());
        assertTrue(categories.contains(TEMP_CAT));
        assertTrue(categories.contains(PRES_CAT));
    }
    
    /**
     * Test listener to capture new metric notifications
     */
    private static class TestListener implements MetricManagerListener {
        public Metric notifiedMetric = null;

        @Override
        public void newMetric(Metric m) {
            this.notifiedMetric = m;
        }
    }

    @Test
    @DisplayName("getEntities should return empty list for category with no metrics")
    void testNotification() {
        TestListener listener = new TestListener();

        manager.addListener(listener);
        var s = buildSettlement("Test");
        var added = manager.getMetric(s, TEMP_CAT, measure1);

        assertEquals(added, listener.notifiedMetric);

        // Reset
        listener.notifiedMetric = null;
        manager.removeListener(listener);
        manager.getMetric(s, TEMP_CAT, measure2);
        assertNull(listener.notifiedMetric);
    }

    @Test
    @DisplayName("getEntities should return empty list for category with no metrics")
    void testGetEntitiesEmptyForNewCategory() {
        // When
        var entities = manager.getEntities(TEMP_CAT);
        
        // Then
        assertTrue(entities.isEmpty());
    }
    
    @Test
    @DisplayName("getEntities should return entities for category")
    void testGetEntitiesForCategory() {
        // Given
        var s = buildSettlement("Test");
        var s2 = buildSettlement("Test2");
        manager.getMetric(s, TEMP_CAT, measure1);
        manager.getMetric(s, TEMP_CAT, measure2); // Same entity, different measure
        manager.getMetric(s2, TEMP_CAT, measure1);
        manager.getMetric(s, PRES_CAT, measure1); // Different category
        
        // When
        List<Entity> entities = manager.getEntities(TEMP_CAT);
        
        // Then
        assertEquals(2, entities.size());
        assertTrue(entities.contains(s));
        assertTrue(entities.contains(s2));
    }
    
    @Test
    @DisplayName("getMeasures should return empty list for entity/category with no metrics")
    void testGetMeasuresEmpty() {
        // Given
        var s = buildSettlement("Test");
        
        // When
        List<String> measures = manager.getMeasures(s, TEMP_CAT);

        // Then
        assertTrue(measures.isEmpty());
    }
    
    @Test
    @DisplayName("getMeasures should return measures for entity and category")
    void testGetMeasuresForEntityAndCategory() {
        // Given
        var s = buildSettlement("Test");
        var s2 = buildSettlement("Test2");
        manager.getMetric(s, TEMP_CAT, measure1);
        manager.getMetric(s, TEMP_CAT, measure2);
        manager.getMetric(s, PRES_CAT, measure1); // Different category
        manager.getMetric(s2, TEMP_CAT, measure1); // Different entity
        
        // When
        List<String> measures = manager.getMeasures(s, TEMP_CAT);
        
        // Then
        assertEquals(2, measures.size());
        assertTrue(measures.contains(measure1));
        assertTrue(measures.contains(measure2));

        assertEquals(2, manager.getCategories(null).size());
        assertEquals(2, manager.getEntities(null).size());

    }
    
    @Test
    @DisplayName("addValue should add to metric")
    void testAddValue() {
        var s = buildSettlement("Test");
        manager.addValue(s, TEMP_CAT, measure1, 42.0);
        Metric metric = manager.getMetric(s, TEMP_CAT, measure1);
        var totalCalculator = new Total();
        metric.apply(totalCalculator);
        assertEquals(42.0, totalCalculator.getSum(), 0.001);
        assertEquals(1, totalCalculator.getCount());
    }
    
    @Test
    @DisplayName("Multiple listeners should receive new metric notifications")
    void testMultipleListeners() {
        // Given
        TestListener listener1 = new TestListener();
        
        // When
        manager.addListener(listener1);
        var s = buildSettlement("Test");
        var createdMetric = manager.getMetric(s, TEMP_CAT, measure1);
        
        // Then
        assertEquals(createdMetric, listener1.notifiedMetric);
        
        // Reset listeners
        listener1.notifiedMetric = null;
        
        // Remove one listener and create another metric
        manager.removeListener(listener1);
        manager.getMetric(s, PRES_CAT, measure1);
        
        // Then
        assertNull(listener1.notifiedMetric); // Should not be notified after removal
    }

    @Test
    @DisplayName("Multiple operations should work together correctly")
    void testIntegrationScenario() {
        // Given - Create several metrics
        var s = buildSettlement("Test");
        var s2 = buildSettlement("Test2");
        manager.getMetric(s, TEMP_CAT, "Max");
        manager.getMetric(s, TEMP_CAT, "Average");
        manager.getMetric(s, PRES_CAT, "Average");
        manager.getMetric(s2, TEMP_CAT, "Average");
        
        var sCategories = manager.getCategories(s);
        assertEquals(2, sCategories.size());
        assertTrue(sCategories.contains(TEMP_CAT));
        assertTrue(sCategories.contains(PRES_CAT));
        
        var tempEntities = manager.getEntities(TEMP_CAT);
        assertEquals(2, tempEntities.size());
        assertTrue(tempEntities.contains(s));
        assertTrue(tempEntities.contains(s2));
        
        var tempMeasures = manager.getMeasures(s, TEMP_CAT);
        assertEquals(2, tempMeasures.size());
        assertTrue(tempMeasures.contains("Average"));
        assertTrue(tempMeasures.contains("Max"));
    }
}