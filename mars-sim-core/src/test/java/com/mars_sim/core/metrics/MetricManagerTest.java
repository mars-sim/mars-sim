package com.mars_sim.core.metrics;

import static org.junit.jupiter.api.Assertions.*;

import com.mars_sim.core.Entity;
import com.mars_sim.core.MockEntity;
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
    private Entity entity1;
    private Entity entity2;
    private String measure1;
    private String measure2;
    
    @BeforeEach
    void setUp() {
        manager = new MetricManager();
        
        entity1 = new MockEntity("E1");
        entity2 = new MockEntity("E2");
        
        measure1 = "Average";
        measure2 = "Maximum";
    }
    
    @Test
    @DisplayName("Constructor should create empty MetricManager")
    void testConstructor() {
        // Given
        MetricManager newManager = new MetricManager();
        
        // Then
        assertNotNull(newManager);
        assertTrue(newManager.getCategories(null).isEmpty());
        assertTrue(newManager.getEntities(null).isEmpty());
    }
    
    @Test
    @DisplayName("getMetric should create new metric when it doesn't exist")
    void testGetMetricCreatesNew() {
        // When
        Metric metric = manager.getMetric(entity1, TEMP_CAT, measure1);
        
        // Then
        assertNotNull(metric);
        assertEquals(entity1, metric.getKey().asset());
        assertEquals(TEMP_CAT, metric.getKey().category());
        assertEquals(measure1, metric.getKey().measure());
    }
    
    @Test
    @DisplayName("getMetric should return existing metric when it exists")
    void testGetMetricReturnsExisting() {
        // Given
        Metric metric1 = manager.getMetric(entity1, TEMP_CAT, measure1);
        
        // When
        Metric metric2 = manager.getMetric(entity1, TEMP_CAT, measure1);
        
        // Then
        assertSame(metric1, metric2);
    }
    
    @Test
    @DisplayName("getCategories should return empty list for entity with no metrics")
    void testGetCategoriesEmptyForNewEntity() {
        // When
        var categories = manager.getCategories(entity1);
        
        // Then
        assertTrue(categories.isEmpty());
    }
    
    @Test
    @DisplayName("getCategories should return categories for entity")
    void testGetCategoriesForEntity() {
        // Given
        manager.getMetric(entity1, TEMP_CAT, measure1);
        manager.getMetric(entity1, PRES_CAT, measure1);
        manager.getMetric(entity2, TEMP_CAT, measure1); // Different entity
        
        // When
        var categories = manager.getCategories(entity1);
        
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
        var added = manager.getMetric(entity1, TEMP_CAT, measure1);

        assertEquals(added, listener.notifiedMetric);

        // Reset
        listener.notifiedMetric = null;
        manager.removeListener(listener);
        manager.getMetric(entity1, TEMP_CAT, measure2);
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
        manager.getMetric(entity1, TEMP_CAT, measure1);
        manager.getMetric(entity1, TEMP_CAT, measure2); // Same entity, different measure
        manager.getMetric(entity2, TEMP_CAT, measure1);
        manager.getMetric(entity1, PRES_CAT, measure1); // Different category
        
        // When
        List<Entity> entities = manager.getEntities(TEMP_CAT);
        
        // Then
        assertEquals(2, entities.size());
        assertTrue(entities.contains(entity1));
        assertTrue(entities.contains(entity2));
    }
    
    @Test
    @DisplayName("getMeasures should return empty list for entity/category with no metrics")
    void testGetMeasuresEmpty() {
        // When
        List<String> measures = manager.getMeasures(entity1, TEMP_CAT);
        
        // Then
        assertTrue(measures.isEmpty());
    }
    
    @Test
    @DisplayName("getMeasures should return measures for entity and category")
    void testGetMeasuresForEntityAndCategory() {
        // Given
        manager.getMetric(entity1, TEMP_CAT, measure1);
        manager.getMetric(entity1, TEMP_CAT, measure2);
        manager.getMetric(entity1, PRES_CAT, measure1); // Different category
        manager.getMetric(entity2, TEMP_CAT, measure1); // Different entity
        
        // When
        List<String> measures = manager.getMeasures(entity1, TEMP_CAT);
        
        // Then
        assertEquals(2, measures.size());
        assertTrue(measures.contains(measure1));
        assertTrue(measures.contains(measure2));
    }
    
    @Test
    @DisplayName("addValue should add to metric")
    void testAddValue() {
        manager.addValue(entity1, TEMP_CAT, measure1, 42.0);
        Metric metric = manager.getMetric(entity1, TEMP_CAT, measure1);
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
        
        var createdMetric = manager.getMetric(entity1, TEMP_CAT, measure1);
        
        // Then
        assertEquals(createdMetric, listener1.notifiedMetric);
        
        // Reset listeners
        listener1.notifiedMetric = null;
        
        // Remove one listener and create another metric
        manager.removeListener(listener1);
        manager.getMetric(entity1, PRES_CAT, measure1);
        
        // Then
        assertNull(listener1.notifiedMetric); // Should not be notified after removal
    }

    @Test
    @DisplayName("Multiple operations should work together correctly")
    void testIntegrationScenario() {
        // Given - Create several metrics
        manager.getMetric(entity1, TEMP_CAT, "Max");
        manager.getMetric(entity1, TEMP_CAT, "Average");
        manager.getMetric(entity1, PRES_CAT, "Average");
        manager.getMetric(entity2, TEMP_CAT, "Average");
        
        var entity1Categories = manager.getCategories(entity1);
        assertEquals(2, entity1Categories.size());
        assertTrue(entity1Categories.contains(TEMP_CAT));
        assertTrue(entity1Categories.contains(PRES_CAT));
        
        var tempEntities = manager.getEntities(TEMP_CAT);
        assertEquals(2, tempEntities.size());
        assertTrue(tempEntities.contains(entity1));
        assertTrue(tempEntities.contains(entity2));
        
        var tempMeasures = manager.getMeasures(entity1, TEMP_CAT);
        assertEquals(2, tempMeasures.size());
        assertTrue(tempMeasures.contains("Average"));
        assertTrue(tempMeasures.contains("Max"));
    }
}