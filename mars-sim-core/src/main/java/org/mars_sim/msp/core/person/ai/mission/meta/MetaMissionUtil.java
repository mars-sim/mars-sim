/**
 * Mars Simulation Project
 * MetaMissionUtil.java
 * @version 3.07 2015-03-02
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.mission.meta;

import java.util.ArrayList;
import java.util.List;

/**
 * A utility mission for getting the list of meta missions.
 */
public class MetaMissionUtil {

    // Static values.
    private static List<MetaMission> metaMissions = null;
    private static List<MetaMission> robotMetaMissions = null;
    /**
     * Private constructor for utility class.
     */
    private MetaMissionUtil() {};

    /**
     * Lazy initialization of metaMissions list.
     */
    private static void initializeMetaMissions() {

        metaMissions = new ArrayList<MetaMission>(12);

        // Populate metaMissions list with all meta missions.
        metaMissions.add(new AreologyStudyFieldMissionMeta());
        metaMissions.add(new BiologyStudyFieldMissionMeta());
        metaMissions.add(new BuildingConstructionMissionMeta());
        metaMissions.add(new BuildingSalvageMissionMeta());
        metaMissions.add(new CollectIceMeta());
        metaMissions.add(new CollectRegolithMeta());
        metaMissions.add(new EmergencySupplyMissionMeta());
        metaMissions.add(new ExplorationMeta());
        metaMissions.add(new MiningMeta());
        metaMissions.add(new RescueSalvageVehicleMeta());
        metaMissions.add(new TradeMeta());
        metaMissions.add(new TravelToSettlementMeta());
    }
    private static void initializeRobotMetaMissions() {

        robotMetaMissions = new ArrayList<MetaMission>();

        // Populate robotMetaMissions list with all meta missions.
        //robotMetaMissions.add(new TradeMeta());
        //robotMetaMissions.add(new TravelToSettlementMeta());
        //robotMetaMissions.add(new BuildingConstructionMissionMeta());
        //robotMetaMissions.add(new BuildingSalvageMissionMeta());
    }

    /**
     * Gets a list of all meta missions.
     * @return list of meta missions.
     */
    public static List<MetaMission> getMetaMissions() {

        // Lazy initialize meta missions list if necessary.
        if (metaMissions == null) {
            initializeMetaMissions();
        }

        // Return copy of meta mission list.
        return new ArrayList<MetaMission>(metaMissions);
    }
    public static List<MetaMission> getRobotMetaMissions() {

        // Lazy initialize meta missions list if necessary.
        if (robotMetaMissions == null) {
            initializeRobotMetaMissions();
        }

        // Return copy of meta mission list.
        return new ArrayList<MetaMission>(robotMetaMissions);
    }
}