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
	protected ArrayList<Target> targets;
	protected ArrayList<MapLocation> zombieDens;
	protected MapLocation[] initArchons;

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
		targets = new ArrayList<>();
		zombieDens = new ArrayList<>();
		initArchons = rc.getInitialArchonLocations(rc.getTeam().opponent());
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

	protected void processSignals() throws GameActionException {
		Signal[] signals = rc.emptySignalQueue();
		for (Signal sig : signals) {
			if (sig.getTeam() != rc.getTeam())
				continue;
			int[] data = sig.getMessage();
			int x = data[0] / 10000;
			int y = data[0] % 10000;
			int id = data[1];
			MapLocation l = new MapLocation(x, y);

			if (id > 0) {
				for (Target t : targets)
					if (t.who == id) {
						t.where = l;
						return;
					}
				targets.add(new Target(id, l));
			} else {
				for (MapLocation z : zombieDens)
					if (z.x == l.x && z.y == l.y)
						return;
				zombieDens.add(l);
			}
		}
		rc.setIndicatorString(0, Integer.toString(targets.size()) + "+" + Integer.toString(zombieDens.size()));
	}

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

	protected boolean knownBase(int ID, int x, int y) throws GameActionException {
		// TODO search list of known bases, if known, dont bother sending
		// message, others should know too
		// if it is not, add it to the list
		// we can sense oponents ID - use it to track archons even if they move
		// and update their location
		return false;
	}

	/* move from my location to specified location */

	protected void bail() throws GameActionException {
		if (!rc.isCoreReady()) {
			senseZombies();
			senseEnemies();
			return;
		}

		int closest = senseRadius;
		Direction d = directions[rand.nextInt(8)];
		MapLocation l = rc.getLocation();

		for (RobotInfo ri : zombies) {
			int current = l.distanceSquaredTo(ri.location);
			if (current < closest) {
				d = ri.location.directionTo(l);
				closest = current;
			}
		}
		for (RobotInfo ri : enemies) {
			int current = l.distanceSquaredTo(ri.location);
			if (current < closest) {
				d = ri.location.directionTo(l);
				closest = current;
			}
		}
		if (rc.canMove(d))
			rc.move(d);
		else if (rc.canMove(d.rotateLeft()))
			rc.move(d.rotateLeft());
		else if (rc.canMove(d.rotateRight()))
			rc.move(d.rotateRight());
		else if (rc.onTheMap(l.add(d)))
			rc.clearRubble(d);
	}
}
