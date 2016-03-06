package ices;

import battlecode.common.*;

public class Scout extends Robot {

	protected int timesMoved;

	protected boolean sensedBase;
	protected boolean baseType;
	protected int xCoord;
	protected int yCoord;
	protected int baseID;

	public Scout(RobotController rc) {
		super(rc);
		timesMoved = 0;

		sensedBase = false;
		baseType = true; // true for player; false for zombie den
		xCoord = 0;
		yCoord = 0;
		baseID = 0;
	}

	@Override
	protected void act() throws GameActionException {
		sendMissedMessages(); // In case we found something, but were not able
								// to send message in previous turn
		processSignals();
		if (lureZombies())
			return;
		explore();
	}

	protected void explore() throws GameActionException {
		if (!rc.isCoreReady())
			return;
		Direction d = directions[rand.nextInt(8)];
		if (rc.canMove(d))
			rc.move(d);

		timesMoved++;

		/*
		 * sense what is around me once in a while, if its important, tell my
		 * friends
		 */
		if (timesMoved % 4 == 0) {
			timesMoved = 0;
			senseEnemyBase();
		}

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

	protected boolean lureZombies() throws GameActionException {
		if (!senseZombies())
			return false;

		/* zombies are around, take advantage of them */
		// TODO
		// check known bases, if empty use initArchons
		// navigate towards closest enemy archon (use function navigate(x,y))
		// maybe use it more times? while (rc.isCoreReady())
		// dont forget to be bitten first :D

		// while (rc.isCoreReady())
		// navigate(310, 420);

		return true;
	}

	protected void senseEnemyBase() throws GameActionException {
		if (!rc.isCoreReady())
			return;
		enemies = rc.senseNearbyRobots(senseRadius, rc.getTeam().opponent());

		if (enemies.length == 0)
			return;

		for (RobotInfo enemy : enemies)
			if (enemy.type == RobotType.ARCHON)
				if (knownBase(enemy.ID, enemy.location.x, enemy.location.y))
					continue;
				else {
					sensedBase = true;
					baseType = true;
					xCoord = enemy.location.x;
					yCoord = enemy.location.y;
					baseID = enemy.ID;
					sendMissedMessages();
					continue;
				}
	}

	protected boolean senseZombies() throws GameActionException {
		if (!rc.isCoreReady())
			return false;
		zombies = rc.senseNearbyRobots(senseRadius, Team.ZOMBIE);

		if (zombies.length == 0)
			return false;

		boolean nonBaseZombies = false;

		for (RobotInfo zombie : zombies) {
			if (zombie.type == RobotType.ZOMBIEDEN)
				if (knownBase(zombie.ID, zombie.location.x, zombie.location.y))
					continue;
				else {
					sensedBase = true;
					baseType = false;
					xCoord = zombie.location.x;
					yCoord = zombie.location.y;
					baseID = zombie.ID;
					sendMissedMessages();
					continue;
				}
			nonBaseZombies = true;
		}

		return nonBaseZombies;
	}
}
