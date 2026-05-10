/*
 * Mars Simulation Project
 * EntityResolver.java
 * @date 2026-05-10
 * @author Barry Evans
 */
package com.mars_sim.core;

/**
 * Resolves entities within the Mars Simulation Project.
 * This is a static helper class.
 */
public class EntityResolver {

    private EntityResolver() {
        // Private constructor to prevent instantiation
    }

    public static Entity resolve(Simulation simulation, EntityIdentifier identifier) {
        return switch(identifier.type()) {
            // First batch based on Unit type
			case "PERSON", "VEHICLE", "SETTLEMENT", "BUILDING", "EVA_SUIT", "CONTAINER",
                    "ROBOT", "CONSTRUCTION" -> getUnit(simulation, identifier);

            case "AUTHORITY" -> SimulationConfig.instance().getReportingAuthorityFactory().getItem(identifier.id());

            case "SCIENTIFICSTUDY" -> simulation.getScientificStudyManager().getAllStudies().stream()
                    .filter(study -> study.getEntityIdentifier().id().equals(identifier.id()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("No scientific study found with id: " + identifier.id()));
            
            case "MISSION" -> simulation.getMissionManager().getMissions().stream()
                    .filter(mission -> mission.getEntityIdentifier().id().equals(identifier.id()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("No mission found with id: " + identifier.id()));

                case "Transportable", "TRANSPORTABLE" -> simulation.getTransportManager().getTransportItems().stream()
                    .filter(item -> item.getEntityIdentifier().id().equals(identifier.id()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("No transport item found with id: " + identifier.id()));

            default ->
                throw new IllegalArgumentException("Unknown entity type: " + identifier.type());
        };
    }

    private static Entity getUnit(Simulation sim, EntityIdentifier identifier) {
        var numberId = Integer.parseInt(identifier.id());

        UnitType unitType = UnitType.valueOf(identifier.type());
        return sim.getUnitManager().getUnitMap(unitType).get(numberId);
    }
}
