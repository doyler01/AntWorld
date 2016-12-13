package antworld.wizard;

import antworld.common.*;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Helper values associated with my ants
 */
public class MoreAntData {

    volatile AntData myAntData;
    int myAntID;

    Squad mySquad;

    volatile int targetX;
    volatile int targetY;

    volatile boolean inDanger;
    volatile boolean farFromSquad;
    volatile int distFromSquad;
    volatile boolean sendHome;
    volatile boolean hasTarget;

    volatile FoodData closestFood;
    volatile int foodProximity;
    volatile AntData closestEnemy;
    volatile int enemyProximity;

    volatile ArrayList<Direction> discoveredPath = new ArrayList<Direction>();

    volatile AntAction.AntActionType lastSubmittedAction;


    public MoreAntData(AntData ant, Squad sqa)
    {
        myAntData = ant;
        myAntID = ant.id;
        mySquad = sqa;
    }

    public boolean calcDistFromSquad()
    {
        distFromSquad = Util.manhattanDistance(myAntData.gridX, myAntData.gridY, mySquad.squadCenterX, mySquad.squadCenterY);
        if(distFromSquad > LocalVars.MAX_DIST_FROM_SQUAD) farFromSquad = true;
        else farFromSquad = false;
        return farFromSquad;
    }

    public AntData closestEnemy(AntData ant, HashSet<AntData> enemySet)
    {
        AntData enemyAnt = null;
        int closestDistance = LocalVars.X_PIXELS;
        for(AntData enemy: enemySet)
        {
            int curDist = Util.manhattanDistance(ant.gridX, ant.gridY, enemy.gridX, enemy.gridY);
            //System.out.println("my ant @ [" + myAntData.gridX + ", " + myAntData.gridY + "] and enemy @ [" + enemy.gridX + ", " + enemy.gridY + "] -- dist: "+ curDist);
            if(curDist < closestDistance)
            {
                enemyAnt = enemy;
                closestDistance = curDist;
            }
        }
        enemyProximity = closestDistance;
        closestEnemy = enemyAnt;
        //if(closestEnemy != null) System.out.println("Found Enemy -- dist: " + enemyProximity);
        return enemyAnt;
    }

    public FoodData closestFood(AntData ant, HashSet<FoodData> foodSet)
    {
        FoodData closeFood = null;
        int closestDistance = LocalVars.X_PIXELS;
        for(FoodData food: foodSet)
        {
            int curDist = Util.manhattanDistance(ant.gridX, ant.gridY, food.gridX, food.gridY);
            if(curDist < closestDistance)
            {
                closeFood = food;
                closestDistance = curDist;
            }
        }
        foodProximity = closestDistance;
        closestFood = closeFood;
        //if(closestFood != null) System.out.println("Found Food -- dist: " + foodProximity);
        return closeFood;
    }

    public boolean related(AntData ant)
    {
        if(ant.id == myAntID) return true;
        return false;
    }

    public boolean completedLastAction(AntData ant)
    {
        if(ant.myAction.type == lastSubmittedAction) return true;
        return false;
    }




}
