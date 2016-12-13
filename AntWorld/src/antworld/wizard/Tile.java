package antworld.wizard;

import antworld.common.LandType;
import com.sun.org.apache.bcel.internal.generic.LAND;

/**
 * Local Cell definition, with some added utility
 */
public class Tile {

    LandType myLandType;
    int landHeight;

    boolean hasBeenExplored = false;
    boolean foodSpotedHereInPast = false;

    public Tile()
    {
        myLandType = LandType.GRASS;    // default to grass (as most common)
        landHeight = -1;                // default to water height
    }

    void setType(LandType type)
    {
        myLandType = type;
    }

    void setHeight(int height)
    {
        landHeight = height;
    }
}
