package com.mars_sim.core.metrics;

import static org.junit.jupiter.api.Assertions.*;

import com.mars_sim.core.Entity;
import com.mars_sim.core.MockEntity;
import com.mars_sim.core.test.MarsSimUnitTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;
import java.util.Map;

/**
 * Unit tests for the MetricManager class.
 */
@DisplayName("MetricManager Tests")
class MetricManagerTest extends MarsSimUnitTest {
    
    private MetricManager manager;
    private Entity entity1;
    private Entity entity2;
    private String category1;
    private String category2;
    private String measure1;
    private String measure2;
    
    @BeforeEach
    void setUp() {
        manager = new MetricManager();
        
        entity1 = new MockEntity("E1");
        entity2 = new MockEntity("E2");
        
        category1 = "Temperature";
        category2 = "Pressure";
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
        assertTrue(newManager.getAllMetrics().isEmpty());
    }
    
    @Test
    @DisplayName("getMetric should create new metric when it doesn't exist")
    void testGetMetricCreatesNew() {
        // When
        Metric metric = manager.getMetric(entity1, category1, measure1);
        
        // Then
        assertNotNull(metric);
        assertEquals(entity1, metric.getKey().asset());
        assertEquals(category1, metric.getKey().category());
        assertEquals(measure1, metric.getKey().measure());
        assertEquals(1, manager.getAllMetrics().size());
    }
    
    @Test
    @DisplayName("getMetric should return existing metric when it exists")
    void testGetMetricReturnsExisting() {
        // Given
        Metric metric1 = manager.getMetric(entity1, category1, measure1);
        
        // When
        Metric metric2 = manager.getMetric(entity1, category1, measure1);
        
        // Then
        assertSame(metric1, metric2);
        assertEquals(1, manager.getAllMetrics().size());
    }
    
    @Test
    @DisplayName("getCategories should return empty list for entity with no metrics")
    void testGetCategoriesEmptyForNewEntity() {
        // When
        List<String> categories = manager.getCategories(entity1);
        
        // Then
        assertTrue(categories.isEmpty());
    }
    
    @Test
    @DisplayName("getCategories should return categories for entity")
    void testGetCategoriesForEntity() {
        // Given
        manager.getMetric(entity1, category1, measure1);
        manager.getMetric(entity1, category2, measure1);
        manager.getMetric(entity2, category1, measure1); // Different entity
        
        // When
        List<String> categories = manager.getCategories(entity1);
        
        // Then
        assertEquals(2, categories.size());
        assertTrue(categories.contains(category1));
        assertTrue(categories.contains(category2));
    }
    
    @Test
    @DisplayName("getEntities should return empty list for category with no metrics")
    void testGetEntitiesEmptyForNewCategory() {
        // When
        List<Entity> entities = manager.getEntities(category1);
        
        // Then
        assertTrue(entities.isEmpty());
    }
    
    @Test
    @DisplayName("getEntities should return entities for category")
    void testGetEntitiesForCategory() {
        // Given
        manager.getMetric(entity1, category1, measure1);
        manager.getMetric(entity1, category1, measure2); // Same entity, different measure
        manager.getMetric(entity2, category1, measure1);
        manager.getMetric(entity1, category2, measure1); // Different category
        
        // When
        List<Entity> entities = manager.getEntities(category1);
        
        // Then
        assertEquals(2, entities.size());
        assertTrue(entities.contains(entity1));
        assertTrue(entities.contains(entity2));
    }
    
    @Test
    @DisplayName("getMeasures should return empty list for entity/category with no metrics")
    void testGetMeasuresEmpty() {
        // When
        List<String> measures = manager.getMeasures(entity1, category1);
        
        // Then
        assertTrue(measures.isEmpty());
    }
    
    @Test
    @DisplayName("getMeasures should return measures for entity and category")
    void testGetMeasuresForEntityAndCategory() {
        // Given
        manager.getMetric(entity1, category1, measure1);
        manager.getMetric(entity1, category1, measure2);
        manager.getMetric(entity1, category2, measure1); // Different category
        manager.getMetric(entity2, category1, measure1); // Different entity
        
        // When
        List<String> measures = manager.getMeasures(entity1, category1);
        
        // Then
        assertEquals(2, measures.size());
        assertTrue(measures.contains(measure1));
        assertTrue(measures.contains(measure2));
    }
    
    @Test
    @DisplayName("getAllMetrics should return copy of metrics map")
    void testGetAllMetrics() {
        // Given
        manager.getMetric(entity1, category1, measure1);
        manager.getMetric(entity2, category2, measure2);
        
        // When
        Map<MetricKey, Metric> allMetrics = manager.getAllMetrics();
        
        // Then
        assertEquals(2, allMetrics.size());
    }
    
    @Test
    @DisplayName("addValue should add to metric")
    void testAddValue() {
        manager.addValue(entity1, category1, measure1, 42.0);
        Metric metric = manager.getMetric(entity1, category1, measure1);
        var totalCalculator = new Total();
        metric.apply(totalCalculator);
        assertEquals(42.0, totalCalculator.getSum(), 0.001);
        assertEquals(1, totalCalculator.getCount());
    }
    
    @Test
    @DisplayName("Multiple operations should work together correctly")
    void testIntegrationScenario() {
        // Given - Create several metrics
        manager.getMetric(entity1, "Temperature", "Average");
        manager.getMetric(entity1, "Temperature", "Max");
        manager.getMetric(entity1, "Pressure", "Average");
        manager.getMetric(entity2, "Temperature", "Average");
        
        // When & Then - Verify various queries
        assertEquals(4, manager.getAllMetrics().size());
        
        List<String> entity1Categories = manager.getCategories(entity1);
        assertEquals(2, entity1Categories.size());
        assertTrue(entity1Categories.contains("Temperature"));
        assertTrue(entity1Categories.contains("Pressure"));
        
        List<Entity> tempEntities = manager.getEntities("Temperature");
        assertEquals(2, tempEntities.size());
        assertTrue(tempEntities.contains(entity1));
        assertTrue(tempEntities.contains(entity2));
        
        List<String> tempMeasures = manager.getMeasures(entity1, "Temperature");
        assertEquals(2, tempMeasures.size());
        assertTrue(tempMeasures.contains("Average"));
        assertTrue(tempMeasures.contains("Max"));
        
        
        List<String> updatedCategories = manager.getCategories(entity1);
        assertEquals(2, updatedCategories.size());
        assertTrue(updatedCategories.contains("Temperature"));
        assertTrue(updatedCategories.contains("Pressure"));
    }
}