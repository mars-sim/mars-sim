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

    /**
     * Resolve an identifer to an entity within the simulation. The identifier should be globally unique across the system and should be immutable.
     * @param simulation the simulation instance
     * @param identifier the entity identifier
     * @return the resolved entity
     */
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
            
            case "MISSION" -> {
                if (identifier.parentId() == null) {
                    throw new IllegalArgumentException("Mission identifier must have a parent settlement id");
                }
                var parentSettement = simulation.getUnitManager().getSettlementByID(Integer.parseInt(identifier.parentId()));
                if (parentSettement == null) {
                    throw new IllegalArgumentException("No settlement found with id: " + identifier.parentId());
                }
                yield parentSettement.getMissionControl().getAllMissions().stream()
                    .filter(mission -> mission.getEntityIdentifier().id().equals(identifier.id()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("No mission found with id: " + identifier.id()));
                }

            case "TRANSPORTABLE" -> simulation.getTransportManager().getTransportItems().stream()
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

    /**
     * Parse a string representation of an EntityIdentifier. The string should have been constructed via {@link #toString(EntityIdentifier)}
     * @param stringId the string representation of the EntityIdentifier
     */
    public static EntityIdentifier fromString(String stringId) {
        var parts = stringId.split(":");
        if (parts.length == 2) {
            return new EntityIdentifier(parts[0], parts[1]);
        }
        else if (parts.length == 3) {
            return new EntityIdentifier(parts[0], parts[1], parts[2]); 
        } else {
            throw new IllegalArgumentException("Invalid EntityIdentifier string: " + stringId);
        }
    }

    /**
     * Convert a EntityIdentifier to a string representation. This should be reversible via {@link #fromString(String)}.
     * @param id Identfier to convert
     * @return
     */
    public static String toString(EntityIdentifier id) {
        if (id.parentId() != null) {
            return id.type() + ":" + id.id() + ":" + id.parentId();
        }
        return id.type() + ":" + id.id();
    }
}
