package ices;

import battlecode.common.*;

public class RobotPlayer {

	/**
	 * run() is the method that is called when a robot is instantiated in the
	 * Battlecode world. If this method returns, the robot dies!
	 **/
	@SuppressWarnings("unused")
	public static void run(RobotController rc) {

		try {

			switch (rc.getType()) {
			case ARCHON:
				new Archon(rc).run();
				break;

			case GUARD:
				new Guard(rc).run();
				break;

			case SCOUT:
				new Scout(rc).run();
				break;

			case SOLDIER:
				new Soldier(rc).run();
				break;

			case TURRET:
				new Turret(rc).run();
				break;

			case VIPER:
				new Viper(rc).run();
				break;
			default:
				break;
			}

			while (true);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}
}
