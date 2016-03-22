package ices;

import battlecode.common.*;

public class Archon extends Robot {

	protected int scoutCount;

	public Archon(RobotController rc) {
		super(rc);
	}

	protected void build(RobotType rt) throws GameActionException {
		if (!rc.isCoreReady()) return;
		if (rc.getTeamParts() < rt.partCost) return;

		// Build robot in some random direction

		int j = rand.nextInt(8);

		for (int i = 0; i < 8; i++)
			if (rc.canBuild(directions[(j + i) % 8], rt)) rc.build(directions[(j + i) % 8], rt);
	}

	@Override
	protected void act() throws GameActionException {
		if (zombiesNear() || enemiesNear()) {
			bail();
			return;
		}
		if (rc.getRoundNum() % 10 == 9) {
			for (Target t : targets)
				rc.broadcastMessageSignal(t.where.x * 1000 + t.where.y, t.who, 1000);
			for (MapLocation l : zombieDens)
				rc.broadcastMessageSignal(l.x * 1000 + l.y, -1, 1000);
		}
		processSignals();
		senseZombies();
		senseEnemies();
		formGroup();
		build(RobotType.SCOUT);
	}

	protected void formGroup() throws GameActionException {
		if (!rc.isCoreReady() || rc.getLocation().distanceSquaredTo(ourArchons[0]) < 2) return;

		Direction d = rc.getLocation().directionTo(ourArchons[0]);

		if (rc.canMove(d)) rc.move(d);
		else if (rc.canMove(d.rotateLeft())) rc.move(d.rotateLeft());
		else if (rc.canMove(d.rotateRight())) rc.move(d.rotateRight());
		else rc.clearRubble(d);

	}

}
