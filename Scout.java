package ices;

import java.util.*;

import battlecode.common.*;

public class Scout extends Robot {

	protected Direction direction;
	protected MapLocation destination;

	public Scout(RobotController rc) {
		super(rc);
		direction = rc.getLocation().directionTo(rc.getInitialArchonLocations(rc.getTeam().opponent())[rand
				.nextInt(rc.getInitialArchonLocations(rc.getTeam().opponent()).length)]);
	}

	@Override
	protected void act() throws GameActionException {

		if (rc.getInfectedTurns() == 1)
			rc.disintegrate();

		if (destination == null && !targets.isEmpty() && rc.isInfected()) {
			// closest enemy archon
			destination = targets.get(0).where;
			for (Target t : targets)
				if (rc.getLocation().distanceSquaredTo(t.where) < rc.getLocation().distanceSquaredTo(destination))
					destination = t.where;
		}

		if (destination == null && !rc.isInfected() && !zombieDens.isEmpty() && !targets.isEmpty()) {
			// closest enemy archon
			MapLocation archon = targets.get(0).where;
			for (Target t : targets)
				if (rc.getLocation().distanceSquaredTo(t.where) < rc.getLocation().distanceSquaredTo(archon))
					archon = t.where;
			// a den closest to the archon
			destination = zombieDens.get(0);
			for (MapLocation l : zombieDens)
				if (archon.distanceSquaredTo(l) < archon.distanceSquaredTo(destination))
					destination = l;
		}
		if (destination != null && rc.isInfected()) {
			homeInOn();
			return;
		}
		if (destination != null && !rc.isInfected()) {
			getInfected();
			return;
		}
		if (rc.getRoundNum() % 5 == 4)
			processSignals();
		if (rc.getRoundNum() % 5 == 3) {
			senseEnemies();
			senseEnemyBase();
			senseZombies();
			senseZombieDen();
		}

		explore();
	}

	protected void explore() throws GameActionException {
		if (!rc.isCoreReady())
			return;
		Direction turnLeft = direction;
		Direction turnRight = direction;
		while (!rc.canMove(turnLeft) && !rc.canMove(turnRight)) {
			turnLeft = turnLeft.rotateLeft();
			turnRight = turnRight.rotateRight();
		}

		if (rc.canMove(turnLeft))
			rc.move(turnLeft);
		else
			rc.move(turnRight);

		/*
		 * sense what is around me once in a while, if its important, tell my
		 * friends
		 */
	}

	protected void homeInOn() throws GameActionException {
		if (!rc.isCoreReady())
			return;
		if (!rc.isInfected()) {
			destination = null;
			return;
		}

		rc.setIndicatorString(0, "suicide run");

		if (rc.getLocation().distanceSquaredTo(destination) <= 20) {
			senseEnemies();
			senseEnemyBase();
			if (rc.isInfected())
				rc.disintegrate();
		}

		Direction l = rc.getLocation().directionTo(destination);
		Direction r = rc.getLocation().directionTo(destination);
		while (!rc.canMove(l) && !rc.canMove(r)) {
			l = l.rotateLeft();
			r = r.rotateRight();
		}

		if (rc.canMove(l))
			rc.move(l);
		else if (rc.canMove(r))
			rc.move(r);
	}

	protected void getInfected() throws GameActionException {
		if (!rc.isCoreReady())
			return;
		if (rc.isInfected()) {
			destination = null;
			return;
		}

		rc.setIndicatorString(0, "getting infected");

		Direction l = rc.getLocation().directionTo(destination);
		Direction r = rc.getLocation().directionTo(destination);
		while (!rc.canMove(l) && !rc.canMove(r)) {
			l = l.rotateLeft();
			r = r.rotateRight();
		}

		if (rc.canMove(l))
			rc.move(l);
		else if (rc.canMove(r))
			rc.move(r);
	}

	// protected void sendMissedMessages() throws GameActionException {
	// if (sensedBase) {
	// // code x, y, ID and type into 2 ints
	// int ID = baseID;
	// if (baseType)
	// ID = baseID * -1;
	// // System.out.println("send x: " + xCoord + " y: " + yCoord);
	// rc.broadcastMessageSignal(10000 * xCoord + yCoord, ID, 100); // TODO
	// // radius
	// // change
	// sensedBase = false;
	// }
	// return;
	// }

	// protected void lureZombies() throws GameActionException {
	// if (!rc.isCoreReady())
	// return;
	// if (!zombiesNear())
	// return;
	// if (Archons.isEmpty())
	// return;
	// Direction d = rc.getLocation().directionTo((MapLocation)
	// Archons.values().toArray()[0]);
	// if (rc.canMove(d))
	// rc.move(d);
	//
	// return;
	// }

	protected void senseEnemyBase() throws GameActionException {
		if (!enemiesNear())
			return;
		rc.setIndicatorString(0, "scanning for bases");
		for (RobotInfo enemy : enemies)
			if (enemy.type == RobotType.ARCHON) {
				rc.broadcastMessageSignal(enemy.location.x * 10000 + enemy.location.y, enemy.ID, 1000);
				for (Target t : targets)
					if (t.who == enemy.ID) {
						t.where = enemy.location;
						return;
					}
				targets.add(new Target(enemy.ID, enemy.location));
			}
	}

	protected void senseZombieDen() throws GameActionException {
		if (!zombiesNear())
			return;
		rc.setIndicatorString(0, "scanning for dens");
		for (RobotInfo zombie : zombies)
			if (zombie.type == RobotType.ZOMBIEDEN) {
				rc.broadcastMessageSignal(zombie.location.x * 10000 + zombie.location.y, -zombie.ID, 1000);
				for (MapLocation l : zombieDens)
					if (l.x == zombie.location.x && l.y == zombie.location.y)
						return;
				zombieDens.add(zombie.location);
			}
	}

	// @Override
	// protected boolean senseZombies() throws GameActionException {
	// if (!rc.isCoreReady())
	// return false;
	// zombies = rc.senseNearbyRobots(senseRadius, Team.ZOMBIE);
	//
	// if (zombies.length == 0)
	// return false;
	//
	// boolean nonBaseZombies = false;
	//
	// for (RobotInfo zombie : zombies) {
	// if (zombie.type == RobotType.ZOMBIEDEN)
	// if (knownBase(zombie.ID, zombie.location.x, zombie.location.y))
	// continue;
	// else {
	// sensedBase = true;
	// baseType = false;
	// xCoord = zombie.location.x;
	// yCoord = zombie.location.y;
	// baseID = zombie.ID;
	// sendMissedMessages();
	// continue;
	// }
	// nonBaseZombies = true;
	// }
	//
	// return nonBaseZombies;
	// }
}
