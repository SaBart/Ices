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

		// --Build robot in some random direction
		for (int i = 0; i < 8; i++)
			if (rc.canBuild(directions[i], rt))
				rc.build(directions[i], rt);
	}

	@Override
	protected void act() throws GameActionException {
		sense();
		bail();
		//build(RobotType.SCOUT);
	}

}
