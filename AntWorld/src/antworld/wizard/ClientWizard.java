package antworld.wizard;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Random;

import antworld.common.*;
import antworld.common.AntAction.AntActionType;

/**
 * ClientWizard is the main() for my client
 * It is a copy paste of the original ClientRandomWalk
 * But with new AI/Logic in place
 */
public class ClientWizard
{
    private static final boolean DEBUG = true;
    private final TeamNameEnum myTeam;
    private static final long password = 962740848319L;//Each team has been assigned a random password.
    private ObjectInputStream inputStream = null;
    private ObjectOutputStream outputStream = null;
    private boolean isConnected = false;
    private NestNameEnum myNestName = null;
    private int centerX, centerY;

    private Colony myColony;
    private Pathing paths;
    private double dangerHealth = 0.5;
    private int exploreTracker = 0;

    private static double JOSTLE_CHANCE = 0.02;


    private Socket clientSocket;


    //A random number generator is created in Constants. Use it.
    //Do not create a new generator every time you want a random number nor
    //  even in every class were you want a generator.
    private static Random random = Constants.random;


    public ClientWizard(String host, int portNumber, TeamNameEnum team)
    {
        myTeam = team;
        System.out.println("Starting " + team +" on " + host + ":" + portNumber + " at "
                + System.currentTimeMillis());

        isConnected = openConnection(host, portNumber);
        if (!isConnected) System.exit(0);
        CommData data = obtainNest();
        mainGameLoop(data);
        closeAll();
    }

    private boolean openConnection(String host, int portNumber)
    {
        try
        {
            clientSocket = new Socket(host, portNumber);
        }
        catch (UnknownHostException e)
        {
            System.err.println("ClientWizard Error: Unknown Host " + host);
            e.printStackTrace();
            return false;
        }
        catch (IOException e)
        {
            System.err.println("ClientWizard Error: Could not open connection to " + host + " on port " + portNumber);
            e.printStackTrace();
            return false;
        }

        try
        {
            outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
            inputStream = new ObjectInputStream(clientSocket.getInputStream());

        }
        catch (IOException e)
        {
            System.err.println("ClientWizard Error: Could not open i/o streams");
            e.printStackTrace();
            return false;
        }

        return true;

    }

    public void closeAll()
    {
        System.out.println("ClientWizard.closeAll()");
        {
            try
            {
                if (outputStream != null) outputStream.close();
                if (inputStream != null) inputStream.close();
                clientSocket.close();
            }
            catch (IOException e)
            {
                System.err.println("ClientWizard Error: Could not close");
                e.printStackTrace();
            }
        }
    }

    /**
     * This method is called ONCE after the socket has been opened.
     * The server assigns a nest to this client with an initial ant population.
     * @return a reusable CommData structure populated by the server.
     */
    public CommData obtainNest()
    {
        CommData data = new CommData(myTeam);
        data.password = password;

        if( sendCommData(data) )
        {
            try
            {
                if (DEBUG) System.out.println("ClientWizard: listening to socket....");
                data = (CommData) inputStream.readObject();
                if (DEBUG) System.out.println("ClientWizard: received <<<<<<<<<"+inputStream.available()+"<...\n" + data);

                if (data.errorMsg != null)
                {
                    System.err.println("ClientWizard***ERROR***: " + data.errorMsg);
                    System.exit(0);
                }
            }
            catch (IOException e)
            {
                System.err.println("ClientWizard***ERROR***: client read failed");
                e.printStackTrace();
                System.exit(0);
            }
            catch (ClassNotFoundException e)
            {
                System.err.println("ClientWizard***ERROR***: client sent incorrect common format");
            }
        }
        if (data.myTeam != myTeam)
        {
            System.err.println("ClientWizard***ERROR***: Server returned wrong team name: "+data.myTeam);
            System.exit(0);
        }
        if (data.myNest == null)
        {
            System.err.println("ClientWizard***ERROR***: Server returned NULL nest");
            System.exit(0);
        }

        myNestName = data.myNest;
        centerX = data.nestData[myNestName.ordinal()].centerX;
        centerY = data.nestData[myNestName.ordinal()].centerY;
        LocalVars.nestCenterX = centerX;
        LocalVars.nestCenterY = centerY;
        System.out.println("ClientWizard: ==== Nest Assigned ===>: " + myNestName);
        myColony = new Colony(data);
        paths = new Pathing(centerX, centerY);
        return data;
    }

    public void mainGameLoop(CommData data)
    {
        while (true)
        {
            try
            {

                //if (DEBUG) System.out.println("ClientWizard: chooseActions: " + myNestName);

                chooseActionsOfAllAnts(data);

                birthNewAnt(data);

                CommData sendData = data.packageForSendToServer();

                //System.out.println("ClientWizard: Sending>>>>>>>: " + sendData);
                outputStream.writeObject(sendData);
                outputStream.flush();
                outputStream.reset();


                //if (DEBUG) System.out.println("ClientWizard: listening to socket....");
                CommData receivedData = (CommData) inputStream.readObject();
                //if (DEBUG) System.out.println("ClientWizard: received <<<<<<<<<"+inputStream.available()+"<...\n" + receivedData);
                data = receivedData;



                if ((myNestName == null) || (data.myTeam != myTeam))
                {
                    System.err.println("ClientWizard: !!!!ERROR!!!! " + myNestName);
                }
            }
            catch (IOException e)
            {
                System.err.println("ClientWizard***ERROR***: client read failed");
                e.printStackTrace();
                System.exit(0);

            }
            catch (ClassNotFoundException e)
            {
                System.err.println("ServerToClientConnection***ERROR***: client sent incorrect common format");
                e.printStackTrace();
                System.exit(0);
            }

        }
    }


    private boolean sendCommData(CommData data)
    {

        CommData sendData = data.packageForSendToServer();
        try
        {
            if (DEBUG) System.out.println("ClientWizard.sendCommData(" + sendData +")");
            outputStream.writeObject(sendData);
            outputStream.flush();
            outputStream.reset();
        }
        catch (IOException e)
        {
            System.err.println("ClientWizard***ERROR***: client read failed");
            e.printStackTrace();
            System.exit(0);
        }

        return true;

    }

    private void updateExploredMap(ArrayList<AntData> antList)
    {
        // only do this every 10 turns?
        // for each ant, set all tiles in vision range to true
        if(exploreTracker++ > 10)
        {
            exploreTracker = 0;
            for(AntData ant: antList)
            {
                int visionRange = ant.antType.getVisionRadius() / 2; //not as complete, but much faster
                for(int i = -visionRange; i <= visionRange; i++)
                {
                    for(int j = -visionRange; j <= visionRange; j++)
                    {
                        LocalVars.exploredMap[ant.gridX + i][ant.gridY + j] = true;
                    }
                }
            }
        }
    }

    private void birthNewAnt(CommData commData)
    {
        if(myColony.canBirthAnt(commData) != null)
        {
            //System.out.println("NEW ANT");
            AntData birthedAnt = new AntData(Constants.UNKNOWN_ANT_ID, myColony.canBirthAnt(commData), myNestName, myTeam );
            commData.myAntList.add( birthedAnt );
            birthedAnt.myAction.type = AntActionType.BIRTH;

        }
    }


    private void chooseActionsOfAllAnts(CommData commData)
    {
        updateExploredMap(commData.myAntList);

        for (AntData ant : commData.myAntList)
        {
            AntAction action = chooseAction(commData, ant);
            ant.myAction = action;
        }
//        myColony.setActionsForAnts(commData);
    }





    private MoreAntData updateMoreData(AntData ant, CommData data)
    {
        for(MoreAntData moreData: myColony.moreAntData)
        {
            if(moreData.related(ant))
            {
                moreData.closestEnemy(ant, data.enemyAntSet);
                moreData.closestFood(ant, data.foodSet);
                return moreData;
            }
        }
        // birthed ants wont have a valid ID until the following turn,
        // this is best place to catch and add:
        MoreAntData birthedAnt = new MoreAntData(ant, null);
        myColony.moreAntData.add(birthedAnt);
        {
            birthedAnt.closestEnemy(ant, data.enemyAntSet);
            birthedAnt.closestFood(ant, data.foodSet);
            return birthedAnt;
        }
    }

    private boolean healAtHome(AntData ant, AntAction action)
    {
        if(ant.underground && ant.health < ant.antType.getMaxHealth())
        {
            action.type = AntActionType.HEAL;
            return true;
        }
        return false;
    }

    // New intent with exit nest: save a few/a small percent of ants back at base
    private boolean exitNest(AntData ant, AntAction action)
    {
        if (ant.underground)
        {
            action.type = AntActionType.EXIT_NEST;
            action.x = centerX - (Constants.NEST_RADIUS-1) + random.nextInt(2 * (Constants.NEST_RADIUS-1));
            action.y = centerY - (Constants.NEST_RADIUS-1) + random.nextInt(2 * (Constants.NEST_RADIUS-1));
            return true;
        }
        return false;
    }

    private boolean enterNest(AntData ant, AntAction action)
    {
        if(ant.health >= ant.antType.getMaxHealth()) return false;

        if(Util.manhattanDistance(ant.gridX, ant.gridY, centerX, centerY) < Constants.NEST_RADIUS - 1)
        {
            action.type = AntActionType.ENTER_NEST;
            return true;
        }
        return false;
    }

    private boolean jostle(AntData ant, AntAction action)
    {
        if(random.nextDouble() < JOSTLE_CHANCE)
        {
            return goRandom(ant, action);
        }
        return false;
    }


    private boolean dropOffFoodAtNest(AntData ant, AntAction action)
    {
        if(ant.carryUnits > 0 && Util.manhattanDistance(ant.gridX, ant.gridY, centerX, centerY) < Constants.NEST_RADIUS - 3)
        {
            action.type = AntActionType.DROP;
            action.quantity = ant.carryUnits;
            action.direction = Direction.getRandomDir();
            return true;
        }
        return false;
    }

    private boolean runFromDanger(AntData ant, AntAction action)
    {
//        if(ant.health < ant.antType.getMaxHealth() / 4)
//        {
//
//        }
        return false;
    }

    private boolean attackAdjacent(AntData ant, AntAction action, MoreAntData data)
    {
        if(ant.carryUnits > 0) return false;

        if(data.enemyProximity < 3)
        {
            //System.out.println("ATTACK!");
            action.type = AntActionType.ATTACK;
            action.direction = paths.toTarget(ant, data.closestEnemy.gridX, data.closestEnemy.gridY);
            return true;
        }
        return false;
    }

    private boolean pickUpFoodAdjacent(AntData ant, AntAction action, MoreAntData data)
    {
        if(ant.carryUnits > (ant.antType.getCarryCapacity() / 2) - 1) return false;

//        if(ant.carryType == null || ant.carryType == data.closestFood.foodType)

        if(data.foodProximity == 1)
        {
            if(ant.carryType != null && ant.carryType != data.closestFood.foodType) return false;

            action.direction = paths.toTarget(ant, data.closestFood.gridX, data.closestFood.gridY);
            action.type = AntActionType.PICKUP;
            if (data.closestFood.getCount() + ant.carryUnits > ((ant.antType.getCarryCapacity() / 2) - 1))
            {
                action.quantity = (ant.antType.getCarryCapacity() / 2) - 1 - ant.carryUnits;
            }
            else
            {
                action.quantity = data.closestFood.getCount();
            }
            return true;
        }

        if(data.foodProximity == 2)
        {
            if(ant.carryType != null && ant.carryType != data.closestFood.foodType) return false;

            action.direction = paths.toTarget(ant, data.closestFood.gridX, data.closestFood.gridY);

            if(action.direction == Direction.NORTH || action.direction == Direction.SOUTH
                    || action.direction == Direction.EAST || action.direction == Direction.WEST) return false;

            action.type = AntActionType.PICKUP;
            if (data.closestFood.getCount() + ant.carryUnits > ((ant.antType.getCarryCapacity() / 2) - 1))
            {
                action.quantity = (ant.antType.getCarryCapacity() / 2) - 1 - ant.carryUnits;
            }
            else
            {
                action.quantity = data.closestFood.getCount();
            }
            return true;

        }

        return false;
    }

    private boolean goHomeIfCarryingOrHurt(AntData ant, AntAction action)
    {
        if(ant.carryUnits > 0 || ant.health < (ant.antType.getMaxHealth() * dangerHealth))
        {
            action.direction = paths.toHome(ant);
            if(action.direction != null)
            {
                action.type = AntActionType.MOVE;
                return true;
            }
        }
        return false;
    }

    private boolean pickUpWater(AntData ant, AntAction action)
    {
//        if(ant.carryUnits > ant.antType.getCarryCapacity() * 0.5) return false;
        if(ant.carryType != null) return false;

        for(Direction dir : Direction.values())
        {
            if(LocalVars.waterMap[ant.gridX + dir.deltaX()][ant.gridY + dir.deltaY()])
            {
                action.type = AntActionType.PICKUP;
                action.direction = dir;
                action.quantity = (ant.antType.getCarryCapacity() / 2) - 1;
                return true;
            }
        }

        return false;
    }

    private boolean goToEnemyAnt(AntData ant, AntAction action, MoreAntData data)
    {
        if(data.enemyProximity < LocalVars.ATTACK_DIST)
        {
            //System.out.println("Ant in range");
            action.direction = paths.toTarget(ant, data.closestEnemy.gridX, data.closestEnemy.gridY);
            if(action.direction != null)
            {
                action.type = AntActionType.MOVE;
                return true;
            }
        }
        return false;
    }

    private boolean goToFood(AntData ant, AntAction action, MoreAntData data){
        if(data.foodProximity < LocalVars.FOOD_DIST)
        {
            //System.out.println("Food in range");
            action.direction = paths.toTarget(ant, data.closestFood.gridX, data.closestFood.gridY);
            if(action.direction != null)
            {
                action.type = AntActionType.MOVE;
                return true;
            }
        }
        return false;
    }

    private boolean goToGoodAnt(AntData ant, AntAction action)
    {
        return false;
    }

    private boolean squadTravel(AntData ant, AntAction action)
    {
        if(random.nextDouble() > 0.8) return false;  // to keep a bit of randomness with the squad movement

//        if (myColony.homeDefense.squadAntIDs.contains(ant.id))
//        {
//            action.type = AntActionType.STASIS;
//            return true;
//        }
        for (Squad s: myColony.factions)
        {
            if(s.squadAntIDs.contains(ant.id))
            {
                if(!(s.exploreDir == null))
                {
                    // include something to stop ants from hitting obstacles...
                    if(paths.nextStepToWater(ant, s.exploreDir)) return false;
                    action.type = AntActionType.MOVE;
                    action.direction = s.exploreDir;
                    return true;
                }
            }
        }
        return false;
    }

    private boolean goExplore(AntData ant, AntAction action, MoreAntData data)
    {
        if(!data.hasTarget)
        {
            paths.chooseRandomUnexploredArea(data);
        }
        if(data.hasTarget)
        {
            action.direction = paths.toTarget(ant, data.targetX, data.targetY);
            if(action.direction != null)
            {
                action.type = AntActionType.MOVE;
                return true;
            }
        }
        if(Util.manhattanDistance(ant.gridX, ant.gridY, data.targetX, data.targetY) < LocalVars.ACCEPT_TARGET_PROX)
        {
            System.out.println("reached target");
            data.hasTarget = false;
        }
        return false;
    }

    private boolean goRandom(AntData ant, AntAction action)
    {
        ArrayList<Direction> legalDirs = paths.leagalDirs(ant);
        action.direction = legalDirs.get(random.nextInt(legalDirs.size()));
        action.type = AntActionType.MOVE;
        return true;
//        Direction dir = Direction.getRandomDir();
//        action.type = AntActionType.MOVE;
//        action.direction = dir;
//        return true;
    }


    private AntAction chooseAction(CommData data, AntData ant)
    {
        AntAction action = new AntAction(AntActionType.STASIS);

        MoreAntData moreData = updateMoreData(ant, data);

        if (ant.ticksUntilNextAction > 0) return action;

        if (healAtHome(ant, action)) return action;

        if (exitNest(ant, action)) return action;

        if (dropOffFoodAtNest(ant, action)) return action;

        if (enterNest(ant, action)) return action;

        if (jostle(ant, action)) return action;

        if (runFromDanger(ant, action)) return action;

        if (attackAdjacent(ant, action, moreData)) return action;

        if (pickUpFoodAdjacent(ant, action, moreData)) return action;

        if (goHomeIfCarryingOrHurt(ant, action)) return action;

        if (goToEnemyAnt(ant, action, moreData)) return action;

        if (pickUpWater(ant, action)) return action;

        if (goToGoodAnt(ant, action)) return action;

        if (goToFood(ant, action, moreData)) return action;

        if (squadTravel(ant, action)) return action;

        if (goExplore(ant, action, moreData)) return action;

        if (goRandom(ant, action)) return action;

        return action;
    }


    /**
     * The last argument is taken as the host name.
     * The default host is localhost.
     * Also supports an optional option for the teamname.
     * The default teamname is TeamNameEnum.Connor_Rob.
     * @param args Array of command-line arguments.
     */
    public static void main(String[] args)
    {
        String serverHost = "localhost";
        if (args.length > 0) serverHost = args[args.length -1];

        TeamNameEnum team = TeamNameEnum.Connor_Rob;
        if (args.length > 1)
        { team = TeamNameEnum.getTeamByString(args[0]);
        }

        new ClientWizard(serverHost, Constants.PORT, team);
    }

}
