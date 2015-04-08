package delegateMAS;

public class Setting {
  
  public static final double VEHICLE_LENGTH = 2d;
  
  public static final int TIME_WINDOW = 32;
  public static final int MIN_LENGTH = 16;
  
  public static final int EXP_FREQ = 5;
  public static final int INT_FREQ = 1;
  public static final int INT_CHANGING_THRESHOLD = 70; // %
  public static final int PHEROMONES_LIFE_TIME = 10;
  
  public static final int MAP_SIZE_X = 16;
  public static final int MAP_SIZE_Y = 16;
  public static final int BLOCK_SIZE = 1;
  public static final int NUM_AGENTS = 10;
  
  public static final int DYNAMIC_RATE = 0;
  
  public static final int PATH_LENGTH = MAP_SIZE_X / 2;
  
  public static final long STOP_TIME = 100000;

  private Setting() {}
  
}
