package com.mars_sim.core.metrics;

import static org.junit.jupiter.api.Assertions.*;

import com.mars_sim.core.time.MarsTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

/**
 * Unit tests for the DataPoint class.
 */
@DisplayName("DataPoint Tests")
class DataPointTest {
    
    private MarsTime mockTime1;
    private MarsTime mockTime2;
    private double value1;
    private double value2;
    
    @BeforeEach
    void setUp() {
        mockTime1 = new MarsTime(1, 12, 0, 0, 0);
        mockTime2 = mockTime1.addTime(500);

        value1 = 25.5;
        value2 = 30.7;
    }
    
    @Test
    @DisplayName("Constructor should create DataPoint with valid parameters")
    void testConstructorWithValidParameters() {
        // When
        DataPoint dataPoint = new DataPoint(mockTime1, value1);
        
        // Then
        assertNotNull(dataPoint);
        assertEquals(mockTime1, dataPoint.getWhen());
        assertEquals(value1, dataPoint.getValue(), 0.001);
    }
    
    @Test
    @DisplayName("Constructor should throw exception when timestamp is null")
    void testConstructorWithNullTimestamp() {
        // When & Then
        assertThrows(NullPointerException.class, () -> 
            new DataPoint(null, value1));
    }
    
    @Test
    @DisplayName("Constructor should accept zero value")
    void testConstructorWithZeroValue() {
        // When
        DataPoint dataPoint = new DataPoint(mockTime1, 0.0);
        
        // Then
        assertNotNull(dataPoint);
        assertEquals(0.0, dataPoint.getValue(), 0.001);
    }
    
    @Test
    @DisplayName("Constructor should accept negative value")
    void testConstructorWithNegativeValue() {
        // Given
        double negativeValue = -15.5;
        
        // When
        DataPoint dataPoint = new DataPoint(mockTime1, negativeValue);
        
        // Then
        assertNotNull(dataPoint);
        assertEquals(negativeValue, dataPoint.getValue(), 0.001);
    }
    
    @Test
    @DisplayName("Constructor should accept extreme values")
    void testConstructorWithExtremeValues() {
        // When & Then
        assertDoesNotThrow(() -> new DataPoint(mockTime1, Double.MAX_VALUE));
        assertDoesNotThrow(() -> new DataPoint(mockTime1, Double.MIN_VALUE));
        assertDoesNotThrow(() -> new DataPoint(mockTime1, Double.POSITIVE_INFINITY));
        assertDoesNotThrow(() -> new DataPoint(mockTime1, Double.NEGATIVE_INFINITY));
        assertDoesNotThrow(() -> new DataPoint(mockTime1, Double.NaN));
    }
    
    @Test
    @DisplayName("Two DataPoints with same values should be equal")
    void testEqualityWithSameValues() {
        // Given
        DataPoint point1 = new DataPoint(mockTime1, value1);
        DataPoint point2 = new DataPoint(mockTime1, value1);
        
        // When & Then
        assertEquals(point1, point2);
        assertEquals(point1.hashCode(), point2.hashCode());
    }
    
    @Test
    @DisplayName("Two DataPoints with different timestamps should not be equal")
    void testInequalityWithDifferentTimestamps() {
        // Given
        DataPoint point1 = new DataPoint(mockTime1, value1);
        DataPoint point2 = new DataPoint(mockTime2, value1);
        
        // When & Then
        assertNotEquals(point1, point2);
    }
    
    @Test
    @DisplayName("Two DataPoints with different values should not be equal")
    void testInequalityWithDifferentValues() {
        // Given
        DataPoint point1 = new DataPoint(mockTime1, value1);
        DataPoint point2 = new DataPoint(mockTime1, value2);
        
        // When & Then
        assertEquals(point1, point2);
    }
    
    @Test
    @DisplayName("DataPoint should not equal null")
    void testNotEqualToNull() {
        // Given
        DataPoint point = new DataPoint(mockTime1, value1);
        
        // When & Then
        assertNotEquals(null, point);
    }
    
    @Test
    @DisplayName("DataPoint should not equal object of different type")
    void testNotEqualToDifferentType() {
        // Given
        DataPoint point = new DataPoint(mockTime1, value1);
        String otherObject = "some string";
        
        // When & Then
        assertNotEquals(otherObject, point);
    }
    
    @Test
    @DisplayName("DataPoint should equal itself")
    void testEqualToSelf() {
        // Given
        DataPoint point = new DataPoint(mockTime1, value1);
        
        // When & Then
        assertEquals(point, point);
    }
}