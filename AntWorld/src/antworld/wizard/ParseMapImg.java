package antworld.wizard;

import antworld.common.LandType;

import java.io.*;
import java.awt.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

/**
 * Read in the AntWorld.png and create a 2d 'map' for the sake of pathing
 */
public class ParseMapImg {

    File file;
    BufferedImage image;
//    boolean[][] waterMap = new boolean[5000][2500];

    public ParseMapImg() throws IOException
    {
        file = new File(".\\resources\\AntWorld.png");
        image = ImageIO.read(file);
        readMap();
    }

    private void readMap()
    {
        for(int x = 0; x < 5000; x++)
        {
            for(int y = 0; y < 2500; y++)
            {
                readXYCoor(x,y);
            }
        }
    }

    private int readXYCoor(int x, int y)
    {
        // Getting pixel color by position x and y
        int clr =  image.getRGB(x,y);

        int  blue  =  clr & 0x000000ff;

        Tile curTile = new Tile();
        //LocalVars.worldMap[x][y] = curTile;

        if(blue == 0xFF)
        {
            //System.out.println("It's Water @ (" + x + ", " + y + ")");
            LocalVars.waterMap[x][y] = true;
            LocalVars.exploredMap[x][y] = true;
            //LocalVars.worldMap[x][y].setType(LandType.WATER);
        }
        else if(blue == 0x8C)
        {
            //System.out.println("It's a Nest");
//            LocalVars.waterMap[x][y] = false;
//            LocalVars.exploredMap[x][y] = false;
            //LocalVars.worldMap[x][y].setType(LandType.NEST);
        }
        else
        {
            //System.out.println("It's Grass at height: " + LandType.getMapHeight(clr));
//            LocalVars.waterMap[x][y] = false;
//            LocalVars.exploredMap[x][y] = false;
            //LocalVars.worldMap[x][y].setHeight(LandType.getMapHeight(clr));
        }

        return 0;
    }

    public static void main(String args[]) throws IOException
    {

        ParseMapImg test = new ParseMapImg();

    }
}
