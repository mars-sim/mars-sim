/*
 * Mars Simulation Project
 * BuildingPlacement.java
 * @date 2025-07-31
 * @author Barry Evans
 */
package com.mars_sim.core.building.construction;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.mars_sim.core.LocalAreaUtil;
import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.BuildingConfig;
import com.mars_sim.core.building.BuildingManager;
import com.mars_sim.core.building.BuildingSpec;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.map.location.BoundedObject;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.structure.ObjectiveUtil;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.tool.RandomUtil;

/**
 * This class is responsible for locating teh best position for a new Building in a Settlement
 */
public class BuildingPlacement {

    private static final SimLogger logger = SimLogger.getLogger(BuildingPlacement.class.getName());

	private static final double DEFAULT_HABITABLE_BUILDING_DISTANCE = 5D;
	private static final double DEFAULT_INHABITABLE_BUILDING_DISTANCE = 2D;
	private static final double DEFAULT_FARMING_DISTANCE = 5D;
	private static final double MINIMUM_CONNECTOR_LENGTH = 1D;

    private BuildingManager bldMgr;
    private Settlement settlement;

    private BuildingConfig buildingConfig;
    
    public BuildingPlacement(Settlement associatedSettlement) {
        this.settlement = associatedSettlement;
        this.bldMgr = settlement.getBuildingManager();
		this.buildingConfig = SimulationConfig.instance().getBuildingConfiguration();
    }


    public static void placeSite(ConstructionSite site) {
        var placer = new BuildingPlacement(site.getAssociatedSettlement());
        placer.positionSite(site);
    }

    public void positionSite(ConstructionSite site) {
		boolean goodPosition = false;

		// Use settlement's objective to determine the desired building type
		String buildingType = ObjectiveUtil.getBuildingType(settlement.getObjective());
		
		logger.info(settlement, "Applying building type '" + buildingType + "' as reference for '" + site + "'.");
		
		if (buildingType != null) {
						
			BuildingSpec spec = buildingConfig.getBuildingSpec(buildingType);
			site.setWidth(spec.getWidth());
			site.setLength(spec.getLength());
			boolean isBuildingConnector = spec.getFunctionSupported().contains(FunctionType.CONNECTION);
			boolean hasLifeSupport = spec.getFunctionSupported().contains(FunctionType.LIFE_SUPPORT);

			if (isBuildingConnector) {
				// Try to find best location to connect two buildings.
				goodPosition = positionNewBuildingConnectorSite(site, buildingType);
			} 
			
			else if (hasLifeSupport) {

				goodPosition = positionSameBuildingType(buildingType, DEFAULT_FARMING_DISTANCE, site);
		
				if (!goodPosition) {
					// Try to put building next to another habitable building.
					List<Building> habitableBuildings = site.getSettlement().getBuildingManager()
							.getBuildings(FunctionType.LIFE_SUPPORT);
					Collections.shuffle(habitableBuildings);
					for (Building b : habitableBuildings) {
						// Match the floor area (e.g look more organize to put all 7m x 9m next to one
						// another)
						if (b.getFloorArea() == site.getWidth() * site.getLength()) {
							goodPosition = positionNextToBuilding(site, b, DEFAULT_HABITABLE_BUILDING_DISTANCE,
									false);
							if (goodPosition) {
								logger.info(settlement, "Case 2. Habitable.");
								break;
							}
						}
					}
				}
			}
			else {
				// Try to put building next to the same building type.
				logger.info(settlement, "Case 3. Inhabitable.");
				goodPosition = positionSameBuildingType(buildingType, DEFAULT_INHABITABLE_BUILDING_DISTANCE, site);
			}
		}

		else {
			// Case 4: building type is null
			
			// Try to put building next to another habitable building.
			List<Building> habitableBuildings = bldMgr.getBuildings(FunctionType.LIFE_SUPPORT);
			Collections.shuffle(habitableBuildings);
			for (Building b : habitableBuildings) {
				// Match the floor area (e.g look more organize to put all 7m x 9m next to one
				// another)
				if (b.getFloorArea() == site.getWidth() * site.getLength()) {
					goodPosition = positionNextToBuilding(site, b, DEFAULT_INHABITABLE_BUILDING_DISTANCE, false);
					if (goodPosition) {
						logger.info(settlement, "Case 4. Habitable. Building type not given.");
						break;
					}
				}
			}
		}

		if (!goodPosition) {
			// Try to put building next to another building.
			// If not successful, try again 10m from each building and continue out at 10m
			// increments
			// until a location is found.
			BuildingManager buildingManager = site.getSettlement().getBuildingManager();
			if (buildingManager.getNumBuildings() > 0) {
				for (int x = 10; !goodPosition; x += 10) {
					for (Building b : buildingManager.getBuildingSet()) {
						goodPosition = positionNextToBuilding(site, b, x, false);
						if (goodPosition) {
							logger.info(settlement, "Case 5. Any one of the buildings.");
							break;
						}
					}
				}
			} 
			
			else {
				logger.info(settlement, "Case 6. No buildings found.");
				// If no buildings at settlement, position new construction site at (0, 0) with
				// random facing.
				int angle = RandomUtil.getRandomInt(4) * 90;
				site.setFacing(angle);
				site.setPosition(LocalPosition.DEFAULT_POSITION);
			}
		}
	}

    /**
	 * Determine the position and length (for variable length sites) for a new
	 * building connector construction site.
	 *
	 * @param site         the construction site.
	 * @param buildingType the new building type.
	 * @return true if position/length of construction site could be found, false if
	 *         not.
	 */
	private boolean positionNewBuildingConnectorSite(ConstructionSite site, String buildingType) {

		boolean result = false;

		List<Building> inhabitableBuildings = bldMgr.getBuildings(FunctionType.LIFE_SUPPORT);
		Collections.shuffle(inhabitableBuildings);

		BuildingSpec spec = buildingConfig.getBuildingSpec(buildingType);

		int baseLevel = spec.getBaseLevel();

		// Try to find a connection between an inhabitable building without access to
		// airlock and
		// another inhabitable building with access to an airlock.
		if (settlement.getAirlockNum() > 0) {

			double leastDistance = Double.MAX_VALUE;

			Iterator<Building> i = inhabitableBuildings.iterator();
			while (i.hasNext()) {
				Building startingBuilding = i.next();
				if (!settlement.hasWalkableAvailableAirlock(startingBuilding)) {

					// Find a different inhabitable building that has walkable access to an airlock.
					Iterator<Building> k = inhabitableBuildings.iterator();
					while (k.hasNext()) {
						Building building = k.next();
						if (!building.equals(startingBuilding)) {

							// Check if connector base level matches either building.
							boolean matchingBaseLevel = (baseLevel == startingBuilding.getBaseLevel())
									|| (baseLevel == building.getBaseLevel());

							if (settlement.hasWalkableAvailableAirlock(building) && matchingBaseLevel) {
								double distance = startingBuilding.getPosition().getDistanceTo(building.getPosition());
								if ((distance < leastDistance) && (distance >= MINIMUM_CONNECTOR_LENGTH)) {

									// Check that new building can be placed between the two buildings.
									if (positionConnectorBetweenTwoBuildings(buildingType, site, startingBuilding,
											building)) {
										leastDistance = distance;
										result = true;
									}
								}
							}
						}
					}
				}
			}
		}

		// Try to find valid connection location between two inhabitable buildings with
		// no joining walking path.
		if (!result) {

			double leastDistance = Double.MAX_VALUE;

			Iterator<Building> j = inhabitableBuildings.iterator();
			while (j.hasNext()) {
				Building startingBuilding = j.next();

				// Find a different inhabitable building.
				Iterator<Building> k = inhabitableBuildings.iterator();
				while (k.hasNext()) {
					Building building = k.next();
					boolean hasWalkingPath = settlement.getBuildingConnectorManager().hasValidPath(startingBuilding,
							building);

					// Check if connector base level matches either building.
					boolean matchingBaseLevel = (baseLevel == startingBuilding.getBaseLevel())
							|| (baseLevel == building.getBaseLevel());

					if (!building.equals(startingBuilding) && !hasWalkingPath && matchingBaseLevel) {

						double distance = startingBuilding.getPosition().getDistanceTo(building.getPosition());
						if ((distance < leastDistance) && (distance >= MINIMUM_CONNECTOR_LENGTH)) {

							// Check that new building can be placed between the two buildings.
							if (positionConnectorBetweenTwoBuildings(buildingType, site, startingBuilding, building)) {
								leastDistance = distance;
								result = true;
							}
						}
					}
				}
			}
		}

		// Try to find valid connection location between two inhabitable buildings that
		// are not directly connected.
		if (!result) {

			double leastDistance = Double.MAX_VALUE;

			Iterator<Building> j = inhabitableBuildings.iterator();
			while (j.hasNext()) {
				Building startingBuilding = j.next();

				// Find a different inhabitable building.
				Iterator<Building> k = inhabitableBuildings.iterator();
				while (k.hasNext()) {
					Building building = k.next();
					boolean directlyConnected = !settlement.getBuildingConnectorManager()
							.getBuildingConnections(startingBuilding, building).isEmpty();

					// Check if connector base level matches either building.
					boolean matchingBaseLevel = (baseLevel == startingBuilding.getBaseLevel())
							|| (baseLevel == building.getBaseLevel());

					if (!building.equals(startingBuilding) && !directlyConnected && matchingBaseLevel) {
						double distance = startingBuilding.getPosition().getDistanceTo(building.getPosition());
						if ((distance < leastDistance) && (distance >= MINIMUM_CONNECTOR_LENGTH)) {

							// Check that new building can be placed between the two buildings.
							if (positionConnectorBetweenTwoBuildings(buildingType, site, startingBuilding, building)) {
								leastDistance = distance;
								result = true;
							}
						}
					}
				}
			}
		}

		// Try to find connection to existing inhabitable building.
		if (!result) {

			// If variable length, set construction site length to default.
			// if (spec.getLength() == -1D) {
			// 	site.setLength(DEFAULT_VARIABLE_BUILDING_LENGTH);
			// }

			Iterator<Building> l = inhabitableBuildings.iterator();
			while (l.hasNext()) {
				Building building = l.next();
				// Make connector building face away from building.
				result = positionNextToBuilding(site, building, 0D, true);

				if (result) {
					break;
				}
			}
		}

		return result;
	}

    /**
	 * Positions a new construction site near an existing building.
	 *
	 * @param site               the new construction site.
	 * @param building           the existing building.
	 * @param separationDistance the separation distance (meters) from the building.
	 * @param faceAway           true if new building should face away from other
	 *                           building.
	 * @return true if construction site could be positioned, false if not.
	 */
	private boolean positionNextToBuilding(ConstructionSite site, Building building, double separationDistance,
			boolean faceAway) {

		boolean goodPosition = false;

		final int front = 0;
		final int back = 1;
		final int right = 2;
		final int left = 3;

		List<Integer> directions = new ArrayList<>(4);
		directions.add(front);
		directions.add(back);
		directions.add(right);
		directions.add(left);
		Collections.shuffle(directions);

		double direction = 0D;
		double structureDistance = 0D;
		double rectRotation = building.getFacing();

		for (int x = 0; x < directions.size(); x++) {
			switch (directions.get(x)) {
			case front:
				direction = building.getFacing();
				structureDistance = (building.getLength() / 2D) + (site.getLength() / 2D);
				break;
			case back:
				direction = building.getFacing() + 180D;
				structureDistance = (building.getLength() / 2D) + (site.getLength() / 2D);
				if (faceAway) {
					rectRotation = building.getFacing() + 180D;
				}
				break;
			case right:
				direction = building.getFacing() + 90D;
				structureDistance = (building.getWidth() / 2D) + (site.getWidth() / 2D);
				if (faceAway) {
					structureDistance = (building.getWidth() / 2D) + (site.getLength() / 2D);
					rectRotation = building.getFacing() + 90D;
				}
				break;
			case left:
				direction = building.getFacing() + 270D;
				structureDistance = (building.getWidth() / 2D) + (site.getWidth() / 2D);
				if (faceAway) {
					structureDistance = (building.getWidth() / 2D) + (site.getLength() / 2D);
					rectRotation = building.getFacing() + 270D;
				}
			}

			if (rectRotation > 360D) {
				rectRotation -= 360D;
			}

			double distance = structureDistance + separationDistance;
			double radianDirection = Math.PI * direction / 180D;
			LocalPosition rectCenter = building.getPosition().getPosition(distance, radianDirection);

			// Check to see if proposed new site position intersects with any existing
			// buildings
			// or construction sites.
			BoundedObject sitePosition = new BoundedObject(rectCenter, site.getWidth(), site.getLength(), rectRotation);
			if (bldMgr.isBuildingLocationOpen(sitePosition, site)) {
				// Set the new site here.
				site.setPosition(rectCenter);
				site.setFacing(rectRotation);
				goodPosition = true;
				break;
			}
		}

		return goodPosition;
	}
    /**
	 * Determine the position and length (for variable length) for a connector
	 * building between two existing buildings.
	 *
	 * @param buildingType   the new connector building type.
	 * @param site           the construction site.
	 * @param firstBuilding  the first of the two existing buildings.
	 * @param secondBuilding the second of the two existing buildings.
	 * @return true if position/length of construction site could be found, false if
	 *         not.
	 */
	private boolean positionConnectorBetweenTwoBuildings(String buildingType, ConstructionSite site,
			Building firstBuilding, Building secondBuilding) {

		boolean result = false;

		// Determine valid placement lines for connector building.
		List<Line2D> validLines = new ArrayList<>();

		// Check each building side for the two buildings for a valid line unblocked by
		// obstacles.
		BuildingSpec spec = buildingConfig.getBuildingSpec(buildingType);
		double width = spec.getWidth();
		List<Point2D> firstBuildingPositions = getFourPositionsSurroundingBuilding(firstBuilding, .1D);
		List<Point2D> secondBuildingPositions = getFourPositionsSurroundingBuilding(secondBuilding, .1D);
		
		for (int x = 0; x < firstBuildingPositions.size(); x++) {
			for (int y = 0; y < secondBuildingPositions.size(); y++) {

				Point2D firstBuildingPos = firstBuildingPositions.get(x);
				Point2D secondBuildingPos = secondBuildingPositions.get(y);

				double distance = Point2D.distance(firstBuildingPos.getX(), firstBuildingPos.getY(),
						secondBuildingPos.getX(), secondBuildingPos.getY());

				if (distance >= MINIMUM_CONNECTOR_LENGTH) {
					// Check line rect between positions for obstacle collision.
					Line2D line = new Line2D.Double(firstBuildingPos.getX(), firstBuildingPos.getY(),
							secondBuildingPos.getX(), secondBuildingPos.getY());
					boolean clearPath = LocalAreaUtil.isLinePathCollisionFree(line, site.getSettlement().getCoordinates(), false);
					if (clearPath) {
						validLines.add(new Line2D.Double(firstBuildingPos, secondBuildingPos));
					}
				}
			}
		}

		if (!validLines.isEmpty()) {
			result = isLineValid(validLines, site, firstBuilding, secondBuilding, width);
		}

		return result;
	}
	
	private boolean isLineValid(List<Line2D> validLines, ConstructionSite site,
			Building firstBuilding, Building secondBuilding, double width) {
		
		// Find shortest valid line.
		double shortestLineLength = Double.MAX_VALUE;
		Line2D shortestLine = null;
		Iterator<Line2D> i = validLines.iterator();
		while (i.hasNext()) {
			Line2D line = i.next();
			double length = Point2D.distance(line.getX1(), line.getY1(), line.getX2(), line.getY2());
			if (length < shortestLineLength) {
				shortestLine = line;
				shortestLineLength = length;
			}
		}

		if (shortestLine == null)
			shortestLine = validLines.get(0);
	
		// Create building template with position, facing, width and length for the
		// connector building.
		double shortestLineFacingDegrees = LocalAreaUtil.getDirection(shortestLine.getP1(), shortestLine.getP2());
		Point2D p1 = adjustConnectorEndPoint(shortestLine.getP1(), shortestLineFacingDegrees, firstBuilding, width);
		Point2D p2 = adjustConnectorEndPoint(shortestLine.getP2(), shortestLineFacingDegrees, secondBuilding,
				width);
		double centerX = (p1.getX() + p2.getX()) / 2D;
		double centerY = (p1.getY() + p2.getY()) / 2D;
		double newLength = p1.distance(p2);
		double facingDegrees = LocalAreaUtil.getDirection(p1, p2);

		// Provide the site the position, facing and length
		site.setPosition(new LocalPosition(centerX, centerY));
		site.setFacing(facingDegrees);
		site.setLength(newLength);
		
		return true;
	}


	private boolean positionSameBuildingType(String buildingType, double dist, ConstructionSite site) {
		boolean goodPosition = false;
		// Try to put building next to the same building type.
		List<Building> sameBuildings = bldMgr.getBuildingsOfSameType(buildingType);
		
		Collections.shuffle(sameBuildings);
		Iterator<Building> j = sameBuildings.iterator();
		while (j.hasNext()) {
			Building building = j.next();
			goodPosition = positionNextToBuilding(site, building, Math.round(dist), false);
			if (goodPosition) {
				logger.info(settlement, "Case 1. The building type '" 
						+ buildingType + "' has life support.");
				break;
			}
		}
		return goodPosition;
	} 
	
	/**
	 * Adjust the connector end point based on relative angle of the connection.
	 *
	 * @param point          the initial connector location.
	 * @param lineFacing     the facing of the connector line (degrees).
	 * @param building       the existing building being connected to.
	 * @param connectorWidth the width of the new connector.
	 * @return point adjusted location for connector end point.
	 */
	private static Point2D adjustConnectorEndPoint(Point2D point, double lineFacing, Building building,
			double connectorWidth) {

		double lineFacingRad = Math.toRadians(lineFacing);
		double angleFromBuildingCenterDegrees = LocalAreaUtil
				.getDirection(new Point2D.Double(building.getXLocation(), building.getYLocation()), point);
		double angleFromBuildingCenterRad = Math.toRadians(angleFromBuildingCenterDegrees);
		double offsetAngle = angleFromBuildingCenterRad - lineFacingRad;
		double offsetDistance = Math.abs(Math.sin(offsetAngle)) * (connectorWidth / 2D);

		double newXLoc = (-1D * Math.sin(angleFromBuildingCenterRad) * offsetDistance) + point.getX();
		double newYLoc = (Math.cos(angleFromBuildingCenterRad) * offsetDistance) + point.getY();

		return new Point2D.Double(newXLoc, newYLoc);
	}

	/**
	 * Gets four positions surrounding a building with a given distance from its
	 * edge.
	 *
	 * @param building         the building.
	 * @param distanceFromSide distance (distance) for positions from the edge of
	 *                         the building.
	 * @return list of four positions.
	 */
	private static List<Point2D> getFourPositionsSurroundingBuilding(Building building, double distanceFromSide) {

		List<Point2D> result = new ArrayList<>(4);

		final int front = 0;
		final int back = 1;
		final int right = 2;
		final int left = 3;

		for (int x = 0; x < 4; x++) {
			double xPos = 0D;
			double yPos = 0D;

			switch (x) {
			case front:
				xPos = 0D;
				yPos = (building.getLength() / 2D) + distanceFromSide;
				break;
			case back:
				xPos = 0D;
				yPos = 0D - (building.getLength() / 2D) - distanceFromSide;
				break;
			case right:
				xPos = 0D - (building.getWidth() / 2D) - distanceFromSide;
				yPos = 0D;
				break;
			case left:
				xPos = (building.getWidth() / 2D) + distanceFromSide;
				yPos = 0D;
				break;
			}

			Point2D position = LocalAreaUtil.convert2SettlementPos(xPos, yPos, building);
			result.add(position);
		}

		return result;
	}
}
