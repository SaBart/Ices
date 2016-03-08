package ices;

import java.util.*;

import battlecode.common.*;

public class Scout extends Robot {

	protected Direction direction;
	protected MapLocation destination;
	protected ArrayList<Target> targets;
	protected ArrayList<MapLocation> zombieDens;
	protected int infection;

	public Scout(RobotController rc) {
		super(rc);
		direction = rc.getLocation().directionTo(rc.getInitialArchonLocations(rc.getTeam().opponent())[rand
				.nextInt(rc.getInitialArchonLocations(rc.getTeam().opponent()).length)]);

		if (rand.nextDouble() < 0.8)
			direction = directions[rand.nextInt(directions.length)];

		targets = new ArrayList<>();
		zombieDens = new ArrayList<>();

	}

	@Override
	protected void act() throws GameActionException {

		if (rc.getRoundNum() % 5 == 4)
			processSignals();
		if (targets.isEmpty() && zombieDens.isEmpty() && rc.getRoundNum() % 5 == 2) {
			senseEnemies();
			senseEnemyBase();
			senseZombies();
			senseZombieDen();
		}
		if (!targets.isEmpty() && rc.isInfected()) {
			destination = targets.get(rand.nextInt(targets.size())).where;
			homeInOn();
		}
		if (!rc.isInfected() && !zombieDens.isEmpty()) {
			destination = zombieDens.get(rand.nextInt(zombieDens.size()));
			getInfected();
		}
		explore();
	}

	@Override
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
			}
		}
		rc.setIndicatorString(0, Integer.toString(targets.size()));
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

		/*
		 * sense what is around me once in a while, if its important, tell my
		 * friends
		 */
	}

	protected void homeInOn() throws GameActionException {
		if (!rc.isCoreReady())
			return;

		rc.setIndicatorString(0, "suicide run");

		if (rc.getInfectedTurns() == 1)
			rc.disintegrate();

		if (rc.getLocation().distanceSquaredTo(destination) <= 8) {
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
				destination = zombie.location;
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
