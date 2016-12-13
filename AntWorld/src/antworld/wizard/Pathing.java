package antworld.wizard;

import antworld.common.AntData;
import antworld.common.Constants;
import antworld.common.Direction;
import antworld.common.NestData;

import java.util.ArrayList;

/**
 * WANT: A 'low cost' pathing algo that doesnt take much time
 * Temp: takes most 'direct' path (disregarding obstacles)
 */
public class Pathing {

    int nestX, nestY;

    public Pathing(int x, int y)
    {
        nestX = x;
        nestY = y;
    }

    public Direction toHome(AntData ant)
    {
        int dx = 0; int dy = 0;
        if(ant.gridX > nestX) dx = -1;
        else if(ant.gridX < nestX) dx = 1;
        if(ant.gridY > nestY) dy = -1;
        else if(ant.gridY < nestY) dy = 1;
        // anti-obstacle
        if(LocalVars.waterMap[ant.gridX + dx][ant.gridY + dy]) return null;
        return enumFromDxDy(dx, dy);
    }

    public Direction toTarget(AntData ant, int x, int y)
    {
        int dx = 0; int dy = 0;
        if(ant.gridX > x) dx = -1;
        else if(ant.gridX < x) dx = 1;
        if(ant.gridY > y) dy = -1;
        else if(ant.gridY < y) dy = 1;
        // anti-obstacle
        if(LocalVars.waterMap[ant.gridX + dx][ant.gridY + dy]) return null;
        return enumFromDxDy(dx, dy);
    }

    private Direction enumFromDxDy(int dx, int dy)
    {
        if(dx == -1 && dy == -1) return Direction.NORTHWEST;
        if(dx == -1 && dy ==  0) return Direction.WEST;
        if(dx == -1 && dy ==  1) return Direction.SOUTHWEST;
        if(dx ==  0 && dy == -1) return Direction.NORTH;
        if(dx ==  0 && dy ==  1) return Direction.SOUTH;
        if(dx ==  1 && dy == -1) return Direction.NORTHEAST;
        if(dx ==  1 && dy ==  0) return Direction.EAST;
        if(dx ==  1 && dy ==  1) return Direction.SOUTHEAST;
        return null;
    }

    // simple bfs
    public ArrayList<Direction> pathSearch(AntData ant, int x, int y)
    {
        ArrayList<Direction> path = new ArrayList<Direction>();


        return path;
    }

    public boolean nextStepToWater(AntData ant, Direction dir)
    {
        if(LocalVars.waterMap[ant.gridX + dir.deltaX()][ant.gridY + dir.deltaY()]) return true;
        return false;
    }

    public ArrayList<Direction> leagalDirs(AntData ant)
    {
        ArrayList<Direction> legalDirs = new ArrayList<Direction>();
        if(!nextStepToWater(ant, Direction.NORTH)) legalDirs.add(Direction.NORTH);
        if(!nextStepToWater(ant, Direction.NORTHEAST)) legalDirs.add(Direction.NORTHEAST);
        if(!nextStepToWater(ant, Direction.EAST)) legalDirs.add(Direction.EAST);
        if(!nextStepToWater(ant, Direction.SOUTHEAST)) legalDirs.add(Direction.SOUTHEAST);
        if(!nextStepToWater(ant, Direction.SOUTH)) legalDirs.add(Direction.SOUTH);
        if(!nextStepToWater(ant, Direction.SOUTHWEST)) legalDirs.add(Direction.SOUTHWEST);
        if(!nextStepToWater(ant, Direction.WEST)) legalDirs.add(Direction.WEST);
        if(!nextStepToWater(ant, Direction.NORTHWEST)) legalDirs.add(Direction.NORTHWEST);
        return legalDirs;
    }

    public void chooseRandomUnexploredArea(MoreAntData data)
    {
        int attempts = 0;
        while(! data.hasTarget)
        {
            int randomXCoor = Constants.random.nextInt(LocalVars.X_PIXELS);
            int randomYCoor = Constants.random.nextInt(LocalVars.Y_PIXELS);
            if(!LocalVars.exploredMap[randomXCoor][randomYCoor])
            {
                data.hasTarget = true;
                data.targetX = randomXCoor;
                data.targetY = randomYCoor;
            }
            attempts++;
            if(attempts > 100) return;
        }
    }
}
