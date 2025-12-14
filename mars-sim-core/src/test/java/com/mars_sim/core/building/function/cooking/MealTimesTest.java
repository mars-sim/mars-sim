package com.mars_sim.core.building.function.cooking;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class MealTimesTest {
    // Default meal times
	private static final int BREAKFAST_START = 250; // at 6am
	private static final int LUNCH_START = 500; // at 12 am
	private static final int DINNER_START = 750; // at 6 pm
	private static final int MIDNIGHT_SNACK_START = 5; 
    private static final int LONG_MEAL = 80; 
	private static final int SHORT_MEAL = 40;

    @Test
    void testZero() {
        assertMeals(0);
    }
    
    @Test
    void test500() {
        assertMeals(500);
    }

    @Test
    void testOvernight() {
        assertMeals(990 - BREAKFAST_START); // Force breakfast to cross mighnight
    }

    private void assertMeals(int offset) {
        var status = new MealSchedule(offset);

        assertMeal("Breakfast", status, offset, BREAKFAST_START, SHORT_MEAL);
        assertMeal("Dinner", status, offset, DINNER_START, LONG_MEAL);
        assertMeal("Lunch", status, offset, LUNCH_START, LONG_MEAL);
        assertMeal("Midnight", status, offset, MIDNIGHT_SNACK_START, SHORT_MEAL);
    }

    private void assertMeal(String name, MealSchedule status, int offset, int start, int duration) {
        int adjustedStart = (start + offset);
        int adjustedEnd = (adjustedStart + duration) % 1000;

        assertTrue(status.isMealTime((adjustedStart+1) % 1000), name + " active");
        assertFalse(status.isMealTime((adjustedStart-1) % 1000), name + " not active");

        assertTrue(status.isMealTime((adjustedEnd-1) % 1000), name + " still active");
        assertFalse(status.isMealTime((adjustedEnd+1) % 1000), name + " still not active");
    }
}
