package antworld.wizard;

import antworld.common.AntData;
import antworld.common.Direction;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * A Squad is a subdivision of all the ants under our control
 * Squads recieve group instructions (eg: travel, explore region, regroup, attack)
 * Squads can be subdivided into smaller squads
 */
public class Squad {
    ArrayList<AntData> squadAnts = new ArrayList<AntData>();
    HashSet<Integer> squadAntIDs = new HashSet<Integer>();
    ArrayList<Squad> subSquadrons = new ArrayList<Squad>();
    int squadCenterX;
    int squadCenterY;
    Direction exploreDir = Direction.EAST; // default value east...


    public Squad()
    {

    }

    void assignAntToSquad(AntData ant)
    {
        squadAntIDs.add(ant.id);
        squadAnts.add(ant);
    }

    void setExploreDir(Direction dir) { exploreDir = dir; }
}
