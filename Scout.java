package ices;

import battlecode.common.*;

public class Scout extends Robot {

	public Scout(RobotController rc) {
		super(rc);
	}

	@Override
	protected void act() throws GameActionException {
		// sense();
		// bail();
		explore();
	}

	protected void explore() throws GameActionException {
		if (!rc.isCoreReady())
			return;
		Direction d = directions[rand.nextInt(8)];
		if (rc.canMove(d))
			rc.move(d);
	}

}
