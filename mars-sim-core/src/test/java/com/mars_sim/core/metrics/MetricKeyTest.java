package com.mars_sim.core.metrics;

import static org.junit.jupiter.api.Assertions.*;

import com.mars_sim.core.Entity;
import com.mars_sim.core.MockEntity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

/**
 * Unit tests for the MetricKey class.
 */
@DisplayName("MetricKey Tests")
class MetricKeyTest {
    
    private static final MetricCategory TEMP_CAT = new MetricCategory("Temperature");
    private static final MetricCategory PRES_CAT = new MetricCategory("Pressure");

    private Entity mockEntity1;
    private Entity mockEntity2;
    private String measure1;
    private String measure2;
    
    @BeforeEach
    void setUp() {
        mockEntity1 = new MockEntity("E1");
        mockEntity2 = new MockEntity("E2");

        measure1 = "Average";
        measure2 = "Maximum";
    }
    
    @Test
    @DisplayName("Constructor should create MetricKey with valid parameters")
    void testConstructorWithValidParameters() {
        // When
        MetricKey key = new MetricKey(mockEntity1, TEMP_CAT, measure1);
        
        // Then
        assertNotNull(key);
        assertEquals(mockEntity1, key.asset());
        assertEquals(TEMP_CAT, key.category());
        assertEquals(measure1, key.measure());
    }
    
    @Test
    @DisplayName("Two MetricKeys with same values should be equal")
    void testEqualityWithSameValues() {
        // Given
        MetricKey key1 = new MetricKey(mockEntity1, TEMP_CAT, measure1);
        MetricKey key2 = new MetricKey(mockEntity1, TEMP_CAT, measure1);
        
        // When & Then
        assertEquals(key1, key2);
        assertEquals(key1.hashCode(), key2.hashCode());
    }
    
    @Test
    @DisplayName("Two MetricKeys with different assets should not be equal")
    void testInequalityWithDifferentAssets() {
        // Given
        MetricKey key1 = new MetricKey(mockEntity1, TEMP_CAT, measure1);
        MetricKey key2 = new MetricKey(mockEntity2, TEMP_CAT, measure1);
        
        // When & Then
        assertNotEquals(key1, key2);
    }
    
    @Test
    @DisplayName("Two MetricKeys with different categories should not be equal")
    void testInequalityWithDifferentCategories() {
        // Given
        MetricKey key1 = new MetricKey(mockEntity1, TEMP_CAT, measure1);
        MetricKey key2 = new MetricKey(mockEntity1, PRES_CAT, measure1);
        
        // When & Then
        assertNotEquals(key1, key2);
    }
    
    @Test
    @DisplayName("Two MetricKeys with different measures should not be equal")
    void testInequalityWithDifferentMeasures() {
        // Given
        MetricKey key1 = new MetricKey(mockEntity1, TEMP_CAT, measure1);
        MetricKey key2 = new MetricKey(mockEntity1, TEMP_CAT, measure2);
        
        // When & Then
        assertNotEquals(key1, key2);
    }
}