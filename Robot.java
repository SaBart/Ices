package ices;

import java.util.*;

import org.apache.commons.lang3.ArrayUtils;

import battlecode.common.*;

public abstract class Robot {

	protected RobotController rc;
	Random rand;
	protected Direction[] directions = { Direction.NORTH, Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST,
			Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST };

	protected int senseRadius;
	protected boolean bitten;

	protected RobotInfo[] zombies;
	protected RobotInfo[] enemies;

	protected Robot(RobotController rc) {
		this.rc = rc;
		rand = new Random(rc.getID());
		senseRadius = rc.getType().sensorRadiusSquared;
	}

	public void run() {
		while (true)
			try {
				act();
				Clock.yield();
			} catch (GameActionException e) {
				System.out.println(e.getMessage());
				e.printStackTrace();
			}
	}

	protected abstract void act() throws GameActionException;

	protected void sense() throws GameActionException {
		if (!rc.isCoreReady())
			return;
		zombies = rc.senseNearbyRobots(senseRadius, Team.ZOMBIE);
		enemies = rc.senseNearbyRobots(senseRadius, rc.getTeam().opponent());
	}

	protected void bail() throws GameActionException {
		if (!rc.isCoreReady())
			return;
		if (zombies.length == 0)
			return;
		if (enemies.length == 0)
			return;

		// Direction d = Arrays.stream(ArrayUtils.addAll(zombies, enemies))
		// .min(Comparator.comparing(ri ->
		// ri.location.distanceSquaredTo(rc.getLocation()))).get().location
		// .directionTo(rc.getLocation());

		Direction d = directions[rand.nextInt(8)];

		if (rc.canMove(d))
			rc.move(d);
		else {
			int base = Arrays.asList(directions).indexOf(d);
			for (int shift = 1; shift <= 4; shift++) {
				d = directions[(base + shift) % directions.length];
				if (rc.canMove(d)) {
					rc.move(d);
					break;
				}
				d = directions[(base - shift) % directions.length];
				if (rc.canMove(d)) {
					rc.move(d);
					break;
				}

			}
		}
	}
}
