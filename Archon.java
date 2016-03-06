package ices;

import battlecode.common.*;

public class Archon extends Robot {

	protected int scoutCount;

	public Archon(RobotController rc) {
		super(rc);
	}

	protected void build(RobotType rt) throws GameActionException {
		if (!rc.isCoreReady())
			return;
		if (rc.getTeamParts() < rt.partCost)
			return;

		// Build robot in some random direction

		int j = rand.nextInt(8);

		for (int i = 0; i < 8; i++)
			if (rc.canBuild(directions[(j + i) % 8], rt))
				rc.build(directions[(j + i) % 8], rt);
	}

	@Override
	protected void act() throws GameActionException {
		processSignals();
		senseZombies();
		senseEnemies();
		double fate = 1;

		if (zombies.length != 0 || enemies.length != 0)
			fate = rand.nextDouble();

		/* TODO: maybe change the p of moving */
		if (fate > 0.8) {
			build(RobotType.SCOUT);
			inform();
		} else
			bail();
	}

	protected void inform() throws GameActionException {
		// TODO: tell the new scout about known bases (or at least one)
	}

}
