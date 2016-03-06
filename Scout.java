package ices;

import battlecode.common.*;

public class Scout extends Robot {

	protected int timesMoved;
	protected boolean sensedBase;
	protected boolean baseType;
	protected int xCoord;
	protected int yCoord;
	protected int baseID;

	protected Direction direction;
	protected MapLocation destination;
	protected boolean suicideMode;

	public Scout(RobotController rc) {
		super(rc);
		timesMoved = 0;

		sensedBase = false;
		baseType = true; // true for player; false for zombie den
		xCoord = 0;
		yCoord = 0;
		baseID = 0;
		direction = directions[rand.nextInt(8)];
	}

	@Override
	protected void act() throws GameActionException {
		// sendMissedMessages(); // In case we found something, but were not
		// able
		// to send message in previous turn

		if (destination != null && zombiesNear())
			suicideMode = true;

		processSignals();
		if (!suicideMode) {
			senseEnemies();
			senseEnemyBase();
			senseZombies();
			explore();
		} else
			homeInOn();
		// lureZombies();

	}

	@Override
	protected void processSignals() throws GameActionException {
		Signal[] signals = rc.emptySignalQueue();
		for (Signal sig : signals) {
			if (sig.getTeam() != rc.getTeam())
				continue;
			int[] data = sig.getMessage();
			int x = data[0];
			int y = data[1];
			MapLocation archon = new MapLocation(x, y);
			destination = archon;
		}
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
			direction = turnLeft;
		else
			direction = turnRight;

		rc.move(direction);
		timesMoved++;

		/*
		 * sense what is around me once in a while, if its important, tell my
		 * friends
		 */
	}

	protected void homeInOn() throws GameActionException {
		if (!rc.isCoreReady())
			return;

		if (rc.getLocation().distanceSquaredTo(destination) < 10) {
			senseEnemies();
			senseEnemyBase();
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

	protected void sendMissedMessages() throws GameActionException {
		if (sensedBase) {
			// code x, y, ID and type into 2 ints
			int ID = baseID;
			if (baseType)
				ID = baseID * -1;
			// System.out.println("send x: " + xCoord + " y: " + yCoord);
			rc.broadcastMessageSignal(10000 * xCoord + yCoord, ID, 100); // TODO
																			// radius
																			// change
			sensedBase = false;
		}
		return;
	}

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

		// for (RobotInfo enemy : enemies)
		// if (enemy.type == RobotType.ARCHON)
		// if (knownBase(enemy.ID, enemy.location.x, enemy.location.y))
		// continue;
		// else {
		// sensedBase = true;
		// baseType = true;
		// xCoord = enemy.location.x;
		// yCoord = enemy.location.y;
		// baseID = enemy.ID;
		// sendMissedMessages();
		// continue;
		// }

		for (RobotInfo enemy : enemies)
			if (enemy.type == RobotType.ARCHON) {
				rc.broadcastMessageSignal(enemy.location.x, enemy.location.y, 1000);
				destination = enemy.location;
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
