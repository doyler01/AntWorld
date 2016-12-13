package antworld.wizard;

/**
 * Is a static class for holding variables needed by multiple parts of my client
 * the final variables are set here but the non-finals are all determined elsewhere
 */
public class LocalVars {

    static final int X_PIXELS = 5000;
    static final int Y_PIXELS = 2500;

    static boolean[][] waterMap = new boolean[X_PIXELS][Y_PIXELS];
    static boolean[][] exploredMap = new boolean[X_PIXELS][Y_PIXELS];
    static Tile[][] worldMap = new Tile[X_PIXELS][Y_PIXELS];

    static int nestCenterX;
    static int nestCenterY;

    static final int MAX_DIST_FROM_SQUAD = 50;
    static final int ATTACK_DIST = 50;
    static final int FOOD_DIST = 75;
    static final int ACCEPT_TARGET_PROX = 10;
}
