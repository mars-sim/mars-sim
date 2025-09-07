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
import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.BuildingManager;
import com.mars_sim.core.building.BuildingSpec;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.map.location.BoundedObject;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.tool.RandomUtil;

/**
 * This class is responsible for locating the best position for a new Building in a Settlement
 */
public class BuildingPlacement {

    private static final SimLogger logger = SimLogger.getLogger(BuildingPlacement.class.getName());

	private static final double GAP_DISTANCE_HABITABLE = 3D;
	private static final double GAP_DISTANCE_INHABITABLE = 5D;
	private static final double GAP_DISTANCE_FARMING = 2D;
	private static final double MINIMUM_CONNECTOR_LENGTH = 1D;

    private BuildingManager bldMgr;
    private Settlement settlement;
    
    public BuildingPlacement(Settlement associatedSettlement) {
        this.settlement = associatedSettlement;
        this.bldMgr = settlement.getBuildingManager();
    }

    public static BoundedObject placeSite(Settlement base, BuildingSpec spec) {
        var placer = new BuildingPlacement(base);
        return placer.positionSite(spec);
    }

    public BoundedObject positionSite(BuildingSpec spec) {
		
		logger.info(settlement, "Applying building type " + spec.getName());
		
		boolean isBuildingConnector = spec.getFunctionSupported().contains(FunctionType.CONNECTION);
		boolean hasLifeSupport = spec.getFunctionSupported().contains(FunctionType.LIFE_SUPPORT);

		if (isBuildingConnector) {
			// Try to find best location to connect two buildings.
			var connectorPosn = positionNewBuildingConnectorSite(spec);
			if (connectorPosn != null) {
				return connectorPosn;
			}
		} 
		
		else if (hasLifeSupport) {

			var sameBldPosn = positionSameBuildingType(spec, GAP_DISTANCE_FARMING);
			if (sameBldPosn != null) {
				return sameBldPosn;
			}
	
			// Try to put building next to another habitable building.
			List<Building> habitableBuildings = new ArrayList<>(bldMgr.getBuildings(FunctionType.LIFE_SUPPORT));
			Collections.shuffle(habitableBuildings);

			double newFloorArea = spec.getWidth() * spec.getLength();
			for (Building b : habitableBuildings) {
				// Match the floor area (e.g look more organize to put all 7m x 9m next to one
				// another)
				if (b.getFloorArea() == newFloorArea) {
					var foundPosn = positionNextToBuilding(b, spec, GAP_DISTANCE_HABITABLE,
							false);
					if (foundPosn != null) {
						logger.info(settlement, "Case B. Habitable.");
						return foundPosn;
					}
				}
			}
		}
		else {
			// Try to put building next to the same building type.
			logger.info(settlement, "Case C. Inhabitable.");
			var sameBldPosn = positionSameBuildingType(spec, GAP_DISTANCE_INHABITABLE);
			if (sameBldPosn != null) {
				return sameBldPosn;
			}
		}

		// Try to put building next to another building.
		// If not successful, try again 10m from each building and continue out at 10m
		// increments
		// until a location is found.
		if (bldMgr.getNumBuildings() > 0) {
			// Max 10 attempt to place
			for (int x = 1; x < 10; x++) {
				for (Building b : bldMgr.getBuildingSet()) {
					var sameBldPosn = positionNextToBuilding(b, spec, x * 10.0, false);
					if (sameBldPosn != null) {
						logger.info(settlement, "Case 5. Any one of the buildings.");
						return sameBldPosn;
					}
				}
			}

			return null;
		} 
			
		logger.info(settlement, "Case 6. No buildings found.");
		// If no buildings at settlement, position new construction site at (0, 0) with
		// random facing.
		int angle = RandomUtil.getRandomInt(4) * 90;
		return new BoundedObject(LocalPosition.DEFAULT_POSITION, spec.getWidth(), spec.getLength(), angle);
	}

    /**
	 * Determine the position and length (for variable length sites) for a new
	 * building connector construction site.
	 *
	 * @param spec         the buil,ding type
	 * @return calculated position
	 */
	private BoundedObject positionNewBuildingConnectorSite(BuildingSpec spec) {

		List<Building> inhabitableBuildings = new ArrayList<>(bldMgr.getBuildings(FunctionType.LIFE_SUPPORT));
		Collections.shuffle(inhabitableBuildings);

		int baseLevel = spec.getBaseLevel();

		BoundedObject result = null;
		// Try to find a connection between an inhabitable building without access to
		// airlock and
		// another inhabitable building with access to an airlock.
		if (settlement.getAirlockNum() > 0) {

			double leastDistance = Double.MAX_VALUE;

			for(Building startingBuilding : inhabitableBuildings) {
				if (!settlement.hasWalkableAvailableAirlock(startingBuilding)) {
					// Find a different inhabitable building that has walkable access to an airlock.
					for(Building building : inhabitableBuildings) {
						if (!building.equals(startingBuilding)) {

							// Check if connector base level matches either building.
							boolean matchingBaseLevel = (baseLevel == startingBuilding.getBaseLevel())
									|| (baseLevel == building.getBaseLevel());

							if (settlement.hasWalkableAvailableAirlock(building) && matchingBaseLevel) {
								double distance = startingBuilding.getPosition().getDistanceTo(building.getPosition());
								if ((distance < leastDistance) && (distance >= MINIMUM_CONNECTOR_LENGTH)) {

									// Check that new building can be placed between the two buildings.
									var betweenPosn = positionConnectorBetweenTwoBuildings(spec, startingBuilding,
											building);
									if (betweenPosn != null) {
										leastDistance = distance;
										result = betweenPosn;
									}
								}
							}
						}
					}
				}
			}
		}
		if (result != null) {
			return result;
		}
		
		// Try to find valid connection location between two inhabitable buildings with
		// no joining walking path.
		double leastDistance = Double.MAX_VALUE;
		for(Building startingBuilding : inhabitableBuildings) {

			// Find a different inhabitable building.
			for(Building building : inhabitableBuildings) {
				boolean hasWalkingPath = settlement.getBuildingConnectorManager().hasValidPath(startingBuilding,
						building);

				// Check if connector base level matches either building.
				boolean matchingBaseLevel = (baseLevel == startingBuilding.getBaseLevel())
						|| (baseLevel == building.getBaseLevel());

				if (!building.equals(startingBuilding) && !hasWalkingPath && matchingBaseLevel) {

					double distance = startingBuilding.getPosition().getDistanceTo(building.getPosition());
					if ((distance < leastDistance) && (distance >= MINIMUM_CONNECTOR_LENGTH)) {

						// Check that new building can be placed between the two buildings.
						var posn = positionConnectorBetweenTwoBuildings(spec, startingBuilding, building);
						if (posn != null) {
							leastDistance = distance;
							result = posn;
						}
					}
				}
			}
		}
		if (result != null) {
			return result;
		}

		// Try to find valid connection location between two inhabitable buildings that
		// are not directly connected.
		leastDistance = Double.MAX_VALUE;
		for(Building startingBuilding : inhabitableBuildings) {

			// Find a different inhabitable building.
			for(Building building : inhabitableBuildings) {
				boolean directlyConnected = !settlement.getBuildingConnectorManager()
						.getBuildingConnections(startingBuilding, building).isEmpty();

				// Check if connector base level matches either building.
				boolean matchingBaseLevel = (baseLevel == startingBuilding.getBaseLevel())
						|| (baseLevel == building.getBaseLevel());

				if (!building.equals(startingBuilding) && !directlyConnected && matchingBaseLevel) {
					double distance = startingBuilding.getPosition().getDistanceTo(building.getPosition());
					if ((distance < leastDistance) && (distance >= MINIMUM_CONNECTOR_LENGTH)) {

						// Check that new building can be placed between the two buildings.
						var posn = positionConnectorBetweenTwoBuildings(spec, startingBuilding, building);
						if (posn != null) {
							leastDistance = distance;
							result = posn;
						}
					}
				}
			}
		}
		if (result != null) {
			return result;
		}

		// Try to find connection to existing inhabitable building.
		for(Building building : inhabitableBuildings) {
			// Make connector building face away from building.
			var nextPosn = positionNextToBuilding(building, spec, 0D, true);

			if (nextPosn != null) {
				return nextPosn;
			}
		}

		return null;
	}

    /**
	 * Positions a new construction site near an existing building.
	 *
	 * @param building           the existing building.
	 * @param gapDistance the separation distance (meters) from the wall of one building to the wall of another building.
	 * @param faceAway           true if new building should face away from other
	 *                           building.
	 * @return true if construction site could be positioned, false if not.
	 */
	private BoundedObject positionNextToBuilding(Building building, BuildingSpec spec, double gapDistance,
			boolean faceAway) {

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
				structureDistance = (building.getLength() / 2D) + (spec.getLength() / 2D);
				break;
			case back:
				direction = building.getFacing() + 180D;
				structureDistance = (building.getLength() / 2D) + (spec.getLength() / 2D);
				if (faceAway) {
					rectRotation = building.getFacing() + 180D;
				}
				break;
			case right:
				direction = building.getFacing() + 90D;
				structureDistance = (building.getWidth() / 2D) + (spec.getWidth() / 2D);
				if (faceAway) {
					structureDistance = (building.getWidth() / 2D) + (spec.getLength() / 2D);
					rectRotation = building.getFacing() + 90D;
				}
				break;
			case left:
				direction = building.getFacing() + 270D;
				structureDistance = (building.getWidth() / 2D) + (spec.getWidth() / 2D);
				if (faceAway) {
					structureDistance = (building.getWidth() / 2D) + (spec.getLength() / 2D);
					rectRotation = building.getFacing() + 270D;
				}
			}

			if (rectRotation > 360D) {
				rectRotation -= 360D;
			}

			double distance = structureDistance + gapDistance;
			double radianDirection = Math.PI * direction / 180D;
			LocalPosition rectCenter = building.getPosition().getPosition(distance, radianDirection);

			// Check to see if proposed new site position intersects with any existing
			// buildings
			// or construction sites.
			BoundedObject sitePosition = new BoundedObject(rectCenter, spec.getWidth(), spec.getLength(), rectRotation);
			if (bldMgr.isBuildingLocationOpen(sitePosition, null)) {
				return sitePosition;
			}
		}

		return null;
	}
    /**
	 * Determine the position and length (for variable length) for a connector
	 * building between two existing buildings.
	 *
	 * @param spec   the new connector building type.
	 * @param firstBuilding  the first of the two existing buildings.
	 * @param secondBuilding the second of the two existing buildings.
	 * @return true if position/length of construction site could be found, false if
	 *         not.
	 */
	private BoundedObject positionConnectorBetweenTwoBuildings(BuildingSpec spec,
			Building firstBuilding, Building secondBuilding) {

		// Determine valid placement lines for connector building.
		List<Line2D> validLines = new ArrayList<>();

		// Check each building side for the two buildings for a valid line unblocked by
		// obstacles.
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
					boolean clearPath = LocalAreaUtil.isLinePathCollisionFree(line, settlement.getCoordinates(), false);
					if (clearPath) {
						validLines.add(new Line2D.Double(firstBuildingPos, secondBuildingPos));
					}
				}
			}
		}

		if (!validLines.isEmpty()) {
			return isLineValid(validLines, firstBuilding, secondBuilding, spec.getWidth());
		}

		return null;
	}
	
	private BoundedObject isLineValid(List<Line2D> validLines,
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
		return new BoundedObject(new LocalPosition(centerX, centerY), width, newLength, facingDegrees);
	}


	private BoundedObject positionSameBuildingType(BuildingSpec spec, double dist) {
		// Try to put building next to the same building type.
		List<Building> sameBuildings = new ArrayList<>(bldMgr.getBuildingsOfSameType(spec.getName()));
		
		Collections.shuffle(sameBuildings);

		for(Building building : sameBuildings) {
			var goodPosition = positionNextToBuilding(building, spec, Math.round(dist), false);
			if (goodPosition != null) {
				logger.info(settlement, "Case 1. The building type '" 
						+ spec.getName() + "' has life support.");
				return goodPosition;
			}
		}
		return null;
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
