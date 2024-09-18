package com.mars_sim.core.person.ai;

import com.mars_sim.core.data.Rating;
import com.mars_sim.core.data.RatingScore;
import com.mars_sim.core.time.MarsTime;
import com.mars_sim.core.tool.RandomUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

class CacheCreatorTest {

    @Mock private MarsTime marsTime;
    @Mock private Rating rating1;
    @Mock private Rating rating2;
    @Mock private RatingScore score1;
    @Mock private RatingScore score2;
    @InjectMocks private CacheCreator<Rating> cacheCreator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(score1.getScore()).thenReturn(5.0);
        when(score2.getScore()).thenReturn(3.0);
        when(rating1.getScore()).thenReturn(score1);
        when(rating2.getScore()).thenReturn(score2);

    }

    @Test
    public void testPut() {
        cacheCreator.put(rating1);
        Assertions.assertEquals(5.0, cacheCreator.getTotalProbability());
        Assertions.assertEquals(1.0, cacheCreator.getCache().size());
    }

    @Test
    public void testAdd() {
        List<Rating> ratings = new ArrayList<>();
        ratings.add(rating1);
        ratings.add(rating2);

        cacheCreator.add(ratings);

        Assertions.assertEquals(8.0, cacheCreator.getTotalProbability());
        Assertions.assertEquals(2.0, cacheCreator.getCache().size());
    }

    @Test
    public void testGetCreatedTime() {
        Assertions.assertEquals(marsTime, cacheCreator.getCreatedTime());
    }

    @Test
    public void testGetLastSelected() {
        cacheCreator.put(rating1);
        cacheCreator.getRandomSelection();
        Assertions.assertEquals(rating1, cacheCreator.getLastSelected());
    }

    @Test
    public void testGetRandomSelection() {
        cacheCreator.put(rating1);
        cacheCreator.put(rating2);

        try (MockedStatic<RandomUtil> randomUtilStatic =
                     mockStatic(RandomUtil.class)) {
            randomUtilStatic.when(() -> RandomUtil
                    .getRandomDouble(8.0)).thenReturn(4.0);

            Rating selectedRating = cacheCreator.getRandomSelection();
            Assertions.assertEquals(rating1, selectedRating);
            Assertions.assertEquals(3.0, cacheCreator.getTotalProbability());
            Assertions.assertEquals(1.0, cacheCreator.getCache().size());
        }
    }
}