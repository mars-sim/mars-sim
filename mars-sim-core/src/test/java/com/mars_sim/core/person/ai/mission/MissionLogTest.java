package com.mars_sim.core.person.ai.mission;

import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.Test;

import com.mars_sim.core.test.MarsSimUnitTest;

class MissionLogTest extends MarsSimUnitTest{
    @Test
    void testAddEntryBasic() {
        MissionLog missionLog = new MissionLog();
        int count = 4;
        for (int i = 0; i < count; i++) {
            missionLog.addEntry("Test entry" + i, "UnitTest");
        }

        var enntries = missionLog.getEntries();
        assertEquals(count, enntries.size());

        var entriesText = enntries.stream().map(e -> e.getEntry()).toList();
        for (int i = 0; i < count; i++) {
            assertEquals(entriesText.get(i), "Test entry" + i);
        }
    }

    @Test
    void testAddEntryOverTime() {
        var master = getContext().getSim().getMasterClock();
        var startTime = master.getMarsTime();
        var entryTime = startTime;

        MissionLog missionLog = new MissionLog();
        int count = 4;
        for (int i = 0; i < count; i++) {
            entryTime = entryTime.addTime(10);
            master.setMarsTime(entryTime);

            missionLog.addEntry("Test entry" + i, "UnitTest");

            var lastEntry = missionLog.getLastEntry();
            assertEquals(entryTime, lastEntry.getTime());
        }
    }

    @Test
    void testAddDuplicate() {
        MissionLog missionLog = new MissionLog();

        missionLog.addEntry("Test entry", "UnitTest");
        missionLog.addEntry("Test entry", "UnitTest");

        var enntries = missionLog.getEntries();
        assertEquals(1, enntries.size());
    }

    
    @Test
    void testAddDuplicateHistory() {
        MissionLog missionLog = new MissionLog();

        for (int i = 0; i < MissionLog.MAX_COMPARE; i++) {
            missionLog.addEntry("Test entry" + i, "UnitTest");
        }

        // Add entry #0
        missionLog.addEntry("Test entry 0", "UnitTest");


        var entries = missionLog.getEntries();
        assertEquals(MissionLog.MAX_COMPARE+1, entries.size());
    }

    @Test
    void testAddDuplicateEntryValid() {
        MissionLog missionLog = new MissionLog();

        missionLog.addEntry("Test entry dup", "UnitTest");
        missionLog.addEntry("Test entry unique", "UnitTest");

        missionLog.addEntry("Test entry dup", "UnitTest");

        var entries = missionLog.getEntries();
        assertEquals(2, entries.size());
    }

    @Test
    void testAddDuplicateEntryUniqueEnterByValid() {
        MissionLog missionLog = new MissionLog();

        missionLog.addEntry("Test entry dup", "UnitTest0");
        missionLog.addEntry("Test entry unique", "UnitTest");

        missionLog.addEntry("Test entry dup", "UnitTest1");

        var entries = missionLog.getEntries();
        assertEquals(3, entries.size());
    }
    
    @Test
    void testGetLastEntry() {
        MissionLog missionLog = new MissionLog();
        int count = 10;
        for (int i = 0; i < count; i++) {
            missionLog.addEntry("Test entry" + i, "UnitTest");
        }

        var lastEntry = missionLog.getLastEntry();
        assertEquals(lastEntry.getEntry(), "Test entry" + (count - 1));
    }
}
