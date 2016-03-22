package ices;

import battlecode.common.*;

public class Scout extends Robot {

	protected MapLocation destination;
	protected Direction direction;

	public Scout(RobotController rc) {
		super(rc);
	}

	@Override
	protected void act() throws GameActionException {
		if (rc.getRoundNum() % 5 == 4) processSignals();
		if (rc.getRoundNum() % 5 == 3) {
			senseEnemies();
			senseEnemyBase();
			senseZombies();
			senseZombieDen();
		}
		pickTarget();
		if (destination != null) rc.setIndicatorString(0, "destination=[" + destination.x + "," + destination.y + "]");
		suicideCheck();
		navigate();
		explore();
	}

	protected void pickTarget() throws GameActionException {
		if (!rc.isInfected()) return;
		// no targets, pick the closest initial archon position
		if (targets.isEmpty()) {
			destination = initArchons[rand.nextInt(initArchons.length)];
			for (MapLocation archon : initArchons)
				if (rc.getLocation().distanceSquaredTo(archon) < rc.getLocation().distanceSquaredTo(destination))
					destination = archon;
			return;
		}
		// closest enemy archon
		destination = targets.get(0).where;
		for (Target t : targets)
			if (rc.getLocation().distanceSquaredTo(t.where) < rc.getLocation().distanceSquaredTo(destination))
				destination = t.where;
	}

	// protected void explore() throws GameActionException {
	// if (rc.isInfected() || destination != null)
	// return;
	// if (zombieDens.isEmpty()) {
	// ;
	// return;
	// }
	// destination = zombieDens.get(0);
	// for (MapLocation l : zombieDens)
	// if (rc.getLocation().distanceSquaredTo(l) <
	// rc.getLocation().distanceSquaredTo(destination))
	// destination = l;
	// }

	protected void suicideCheck() throws GameActionException {
		if (!rc.isInfected()) {
			if (destination != null) direction = destination.directionTo(rc.getLocation());
			destination = null;
			return;
		}
		if (rc.getLocation().distanceSquaredTo(destination) <= 20) {
			senseEnemies();
			senseEnemyBase();
			rc.disintegrate();
		}
	}

	protected void explore() throws GameActionException {
		if (!rc.isCoreReady() || destination != null) return;
		if (direction == null) direction = directions[rand.nextInt(directions.length)];
		if (rc.canMove(direction)) rc.move(direction);
		else if (rc.canMove(direction.rotateLeft())) rc.move(direction.rotateLeft());
		else if (rc.canMove(direction.rotateRight())) rc.move(direction.rotateRight());
		else direction = rand.nextDouble() < 0.5 ? direction.rotateLeft() : direction.rotateRight();
	}

	protected void navigate() throws GameActionException {
		if (!rc.isCoreReady() || destination == null) return;

		MapLocation l = rc.getLocation();

		if (l.distanceSquaredTo(destination) == 0) {
			destination = null;
			return;
		}
		Direction d = l.directionTo(destination);

		if (rc.canMove(d) && rc.senseRubble(l.add(d)) < 100) rc.move(d);
		else if (rc.canMove(d.rotateLeft()) && rc.senseRubble(l.add(d.rotateLeft())) < 100) rc.move(d.rotateLeft());
		else if (rc.canMove(d.rotateRight()) && rc.senseRubble(l.add(d.rotateRight())) < 100) rc.move(d.rotateRight());
		else if (rc.senseRubble(l.add(d.opposite())) > 100) rc.clearRubble(d.opposite());
	}

	protected void senseEnemyBase() throws GameActionException {
		if (!enemiesNear()) return;
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
		if (!zombiesNear()) return;
		rc.setIndicatorString(0, "scanning for dens");
		for (RobotInfo zombie : zombies)
			if (zombie.type == RobotType.ZOMBIEDEN) {
				rc.broadcastMessageSignal(zombie.location.x * 10000 + zombie.location.y, -zombie.ID, 1000);
				for (MapLocation l : zombieDens)
					if (l.x == zombie.location.x && l.y == zombie.location.y) return;
				zombieDens.add(zombie.location);
			}
	}
}
