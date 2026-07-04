package com.mars_sim.core.person.ai.mission.meta;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.test.MarsSimUnitTest;

class FieldStudyMetaTest extends MarsSimUnitTest {
    
    static Stream<FieldStudyMeta> fieldStudyMetaSubclasses() {
        return Stream.of(
            new AreologyFieldStudyMeta(),
            new BiologyFieldStudyMeta(),
            new MeteorologyFieldStudyMeta()
        );
    }
    
    @ParameterizedTest(name= "Test science of worker in {0}")
    @MethodSource("fieldStudyMetaSubclasses")
    void testGetWorkerSuitability(FieldStudyMeta meta) {
        var s = buildSettlement("test");
        var b = buildAccommodation(s.getBuildingManager(), LocalPosition.DEFAULT_POSITION, 0);
        var n = buildPerson("normal", s, JobType.CHEF, b, null);
        var w = buildPerson("worker", s, JobType.CHEF, b, null);

        w.getResearchStudy().addScientificAchievement(200, meta.getScienceType());

        var nScore = meta.getWorkerSuitability(n);
        var wScore = meta.getWorkerSuitability(w);

        assertTrue(wScore > nScore, "Worker suitability score for worker should be higher than normal");
    }
}
