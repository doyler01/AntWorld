package antworld.wizard;

import antworld.common.*;

import java.util.ArrayList;
import java.util.Random;

/**
 * Colony is the totality of our ants and highest level managment
 * responsible for: creating factions and evaluating if birthing is possible
 * (as well as triggering the map read-in)
 */
public class Colony {
    int totalAnts;
    volatile CommData data;
    ArrayList<AntData> antList;
    ArrayList<Squad> factions = new ArrayList<Squad>();
    int factionCount = 5; // one to head in each cardinal direction + freedom
    int leaveAtHome = 0;  // want to implement some home defense, but not in place yet
    Squad homeDefense = new Squad();

    ArrayList<MoreAntData> moreAntData = new ArrayList<MoreAntData>();

    private int centerX, centerY;

    private static Random random = Constants.random;

    public Colony(CommData data)
    {
        this.data = data;
        antList = data.myAntList;
        totalAnts = antList.size();
        createFactions();
        try{
            ParseMapImg readIn = new ParseMapImg();
        }
        catch(Exception e)
        {}

    }

    private void createFactions()
    {
        for(int i = 0; i < factionCount; i++)
        {
            factions.add(new Squad());
        }
        for(int i = 0; i < leaveAtHome; i++)
        {
            homeDefense.assignAntToSquad(antList.get(i));
            moreAntData.add(new MoreAntData(antList.get(i), homeDefense));

        }
        for(int i = leaveAtHome; i < totalAnts; i++)
        {
            factions.get(i % factionCount).assignAntToSquad(antList.get(i));
            moreAntData.add(new MoreAntData(antList.get(i), factions.get(i % factionCount)));
        }

        factions.get(0).setExploreDir(Direction.NORTH);
        factions.get(1).setExploreDir(Direction.SOUTH);
        factions.get(2).setExploreDir(Direction.EAST);
        factions.get(3).setExploreDir(Direction.WEST);
        factions.get(4).setExploreDir(null);
    }

    //For my strategy, this is the approximate breakdown of how I value the different types of ants (tiered below)
    AntType canBirthAnt(CommData commData)
    {
        if (canBirthType(AntType.SPEED, commData)) return AntType.SPEED;
        if (canBirthType(AntType.VISION, commData)) return AntType.VISION;
        if (canBirthType(AntType.MEDIC, commData)) return AntType.MEDIC;
        if (canBirthType(AntType.DEFENCE, commData)) return AntType.DEFENCE;
        if (canBirthType(AntType.WORKER, commData)) return AntType.WORKER;
        if (canBirthType(AntType.ATTACK, commData)) return AntType.ATTACK;
        return null;
    }

    private boolean canBirthType(AntType type, CommData commData)
    {
        FoodType[] foodForDesiredAnt = type.getBirthFood();
        for(int i = 0; i < foodForDesiredAnt.length; i++)
        {
            int neededFood = type.getFoodUnitsToSpawn(foodForDesiredAnt[i]);
            if(! (commData.foodStockPile[foodForDesiredAnt[i].ordinal()] >= neededFood)) return false;
        }
        //System.out.println("Can birth a(n) " + type + " ant");
        return true;
    }
}
