package ices;

import battlecode.common.*;

public class Target {

	public int who;
	public MapLocation where;

	public Target(int id, MapLocation l) {
		who = id;
		where = l;
	}
}
