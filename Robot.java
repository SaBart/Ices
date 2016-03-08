package ices;

import java.util.*;

import org.apache.commons.lang3.ArrayUtils;

import battlecode.common.*;

public abstract class Robot {

	protected RobotController rc;
	Random rand;
	protected static Direction[] directions = { Direction.NORTH, Direction.NORTH_EAST, Direction.EAST,
			Direction.SOUTH_EAST, Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST };

	protected int senseRadius;

	protected RobotInfo[] zombies;
	protected RobotInfo[] enemies;

	MapLocation[] initArchons;

	// TODO: list of known bases - archons + zombie dens
	// some structure with position, type and ID of base
	/*
	 * something like class Base { int x,y; int ID; boolean type; }
	 *
	 * List<Base> Bases;
	 */

	protected Robot(RobotController rc) {
		this.rc = rc;
		rand = new Random(rc.getID());
		senseRadius = rc.getType().sensorRadiusSquared;
	}

	public void run() {
		// initArchons = rc.getInitialArchonLocations(rc.getTeam().opponent());

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

	protected void senseEnemies() throws GameActionException {
		enemies = rc.senseNearbyRobots(senseRadius, rc.getTeam().opponent());
	}

	protected void senseZombies() throws GameActionException {
		zombies = rc.senseNearbyRobots(senseRadius, Team.ZOMBIE);
	}

	protected boolean enemiesNear() {
		return enemies != null && enemies.length > 0;
	}

	protected boolean zombiesNear() {
		return zombies != null && zombies.length > 0;
	}

	protected void processSignals() throws GameActionException {
		Signal[] signals = rc.emptySignalQueue();
		for (Signal sig : signals) {
			if (sig.getTeam() != rc.getTeam())
				continue;
			int[] data = sig.getMessage();
			int y = data[0] % 10000;
			int x = (data[0] - y) / 10000;
			// System.out.println("received x: " + x + " y: " + y);
			knownBase(Math.abs(data[1]), x, y);
		}
	}

	protected boolean knownBase(int ID, int x, int y) throws GameActionException {
		// TODO search list of known bases, if known, dont bother sending
		// message, others should know too
		// if it is not, add it to the list
		// we can sense oponents ID - use it to track archons even if they move
		// and update their location
		return false;
	}

	/* Navigate from my location to x, y - does only one step */
	protected void navigate(int x, int y) throws GameActionException {
		int myX = rc.getLocation().x;
		int myY = rc.getLocation().y;

		int xDir = 0; // -1 = left 0 = same 1 = right
		int yDir = 0; // -1 = down 0 = same 1 = up

		if (x < myX)
			xDir = -1;
		else if (x > myX)
			xDir = 1;

		if (y < myY)
			yDir = 1;
		else if (y > myY)
			yDir = -1;

		if (xDir == 0 && yDir == 1 && rc.canMove(directions[0])) {
			rc.move(directions[0]);
			return;
		}
		if (xDir == 1 && yDir == 1 && rc.canMove(directions[1])) {
			rc.move(directions[1]);
			return;
		}
		if (xDir == 1 && yDir == 0 && rc.canMove(directions[2])) {
			rc.move(directions[2]);
			return;
		}
		if (xDir == 1 && yDir == -1 && rc.canMove(directions[3])) {
			rc.move(directions[3]);
			return;
		}
		if (xDir == 0 && yDir == -1 && rc.canMove(directions[4])) {
			rc.move(directions[4]);
			return;
		}
		if (xDir == -1 && yDir == -1 && rc.canMove(directions[5])) {
			rc.move(directions[5]);
			return;
		}
		if (xDir == -1 && yDir == 0 && rc.canMove(directions[6])) {
			rc.move(directions[6]);
			return;
		}
		if (xDir == -1 && yDir == 1 && rc.canMove(directions[7])) {
			rc.move(directions[7]);
			return;
		}

		// in case we cannot move where we want... go random
		Direction d = directions[rand.nextInt(8)];
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

	/* navigate from my location to specified location */
	protected void navigate(MapLocation dest) throws GameActionException {
		if (!rc.isCoreReady())
			return;
		Direction d = rc.getLocation().directionTo(dest);
		Direction l = d;
		Direction r = d;
		while (!rc.canMove(l) && !rc.canMove(r)) {
			l = l.rotateLeft();
			r = r.rotateRight();
		}

		if (rc.canMove(l))
			rc.move(l);
		else if (rc.canMove(r))
			rc.move(r);

	}

	protected void bail() throws GameActionException {
		if (!rc.isCoreReady())
			return;

		int closest = senseRadius;
		Direction d = directions[rand.nextInt(8)];

		for (RobotInfo ri : zombies)
			if (rc.getLocation().distanceSquaredTo(ri.location) < closest)
				d = ri.location.directionTo(rc.getLocation());

		for (RobotInfo ri : enemies)
			if (rc.getLocation().distanceSquaredTo(ri.location) < closest)
				d = ri.location.directionTo(rc.getLocation());

		Direction turnLeft = d;
		Direction turnRight = d;
		while (!rc.canMove(turnLeft) && !rc.canMove(turnRight)) {
			turnLeft = turnLeft.rotateLeft();
			turnRight = turnRight.rotateRight();
		}

		if (rc.canMove(turnLeft))
			d = turnLeft;
		else
			d = turnRight;

		rc.move(d);
	}
}
