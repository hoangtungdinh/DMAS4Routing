package delegateMAS;

public class Setting {
  private double vehicleLength;
  private int timeWindow;
  private int minTimeSteps;
  private int explorationFreq;
  private int intentionFreq;
  private int intentionChangingThreshold;
  private int pheromoneLifeTime;
  private int mapSizeX;
  private int mapSizeY;
  private int blockSize;
  private int numberOfAgents;
  private int dynamicRate;
  private int pathLength;
  private long stopTime;

  public Setting(double vehicleLength, int timeWindow, int minTimeSteps,
      int explorationFreq, int intentionFreq, int intentionChangingThreshold,
      int pheromoneLifeTime, int mapSizeX, int mapSizeY, int blockSize,
      int numberOfAgents, int dynamicRate, int pathLength, long stopTime) {
    this.vehicleLength = vehicleLength;
    this.timeWindow = timeWindow;
    this.minTimeSteps = minTimeSteps;
    this.explorationFreq = explorationFreq;
    this.intentionFreq = intentionFreq;
    this.intentionChangingThreshold = intentionChangingThreshold;
    this.pheromoneLifeTime = pheromoneLifeTime;
    this.mapSizeX = mapSizeX;
    this.mapSizeY = mapSizeY;
    this.blockSize = blockSize;
    this.numberOfAgents = numberOfAgents;
    this.dynamicRate = dynamicRate;
    this.pathLength = pathLength;
    this.stopTime = stopTime;
  }

  public double getVehicleLength() {
    return vehicleLength;
  }

  public int getTimeWindow() {
    return timeWindow;
  }

  public int getMinTimeSteps() {
    return minTimeSteps;
  }

  public int getExplorationFreq() {
    return explorationFreq;
  }

  public int getIntentionFreq() {
    return intentionFreq;
  }

  public int getIntentionChangingThreshold() {
    return intentionChangingThreshold;
  }

  public int getPheromoneLifeTime() {
    return pheromoneLifeTime;
  }

  public int getMapSizeX() {
    return mapSizeX;
  }

  public int getMapSizeY() {
    return mapSizeY;
  }

  public int getBlockSize() {
    return blockSize;
  }

  public int getNumberOfAgents() {
    return numberOfAgents;
  }

  public int getDynamicRate() {
    return dynamicRate;
  }

  public int getPathLength() {
    return pathLength;
  }

  public long getStopTime() {
    return stopTime;
  }

  @Override
  public String toString() {
    return new StringBuilder()
        .append("vehicleLength: " + vehicleLength + "\n")
        .append("timeWindow: " + timeWindow + "\n")
        .append("minTimeSteps: " + minTimeSteps + "\n")
        .append("explorationFreq: " + explorationFreq + "\n")
        .append("intentionFreq: " + intentionFreq + "\n")
        .append(
            "intentionChangingThreshold: " + intentionChangingThreshold + "\n")
        .append("pheromoneLifeTime: " + pheromoneLifeTime + "\n")
        .append("mapSizeX: " + mapSizeX + "\n")
        .append("mapSizeY: " + mapSizeY + "\n")
        .append("blockSize: " + blockSize + "\n")
        .append("numberOfAgents: " + numberOfAgents + "\n")
        .append("dynamicRate: " + dynamicRate + "\n")
        .append("pathLength: " + pathLength + "\n")
        .append("stopTime: " + stopTime + "\n")
        .toString();
  }

  public static class SettingBuilder {
    private double vehicleLength = 2d;
    private int timeWindow = 32;
    private int minTimeSteps = 16;
    private int explorationFreq = 5;
    private int intentionFreq = 1;
    private int intentionChangingThreshold = 70;
    private int pheromoneLifeTime = 10;
    private int mapSizeX = 16;
    private int mapSizeY = 16;
    private int blockSize = 1;
    private int numberOfAgents = 10;
    private int dynamicRate = 0;
    private int pathLength = 8;
    private long stopTime = 1000000;

    public SettingBuilder() {

    }

    public SettingBuilder setVehicleLength(double vehicleLength) {
      this.vehicleLength = vehicleLength;
      return this;
    }

    public SettingBuilder setTimeWindow(int timeWindow) {
      this.timeWindow = timeWindow;
      return this;
    }

    public SettingBuilder setMinTimeSteps(int minTimeSteps) {
      this.minTimeSteps = minTimeSteps;
      return this;
    }

    public SettingBuilder setExplorationFreq(int explorationFreq) {
      this.explorationFreq = explorationFreq;
      return this;
    }

    public SettingBuilder setIntentionFreq(int intentionFreq) {
      this.intentionFreq = intentionFreq;
      return this;
    }

    public SettingBuilder setIntentionChangingThreshold(int intentionChangingThreshold) {
      this.intentionChangingThreshold = intentionChangingThreshold;
      return this;
    }

    public SettingBuilder setPheromoneLifeTime(int pheromoneLifeTime) {
      this.pheromoneLifeTime = pheromoneLifeTime;
      return this;
    }

    public SettingBuilder setMapSizeX(int mapSizeX) {
      this.mapSizeX = mapSizeX;
      return this;
    }

    public SettingBuilder setMapSizeY(int mapSizeY) {
      this.mapSizeY = mapSizeY;
      return this;
    }

    public SettingBuilder setBlockSize(int blockSize) {
      this.blockSize = blockSize;
      return this;
    }

    public SettingBuilder setNumberOfAgents(int numberOfAgents) {
      this.numberOfAgents = numberOfAgents;
      return this;
    }

    public SettingBuilder setDynamicRate(int dynamicRate) {
      this.dynamicRate = dynamicRate;
      return this;
    }

    public SettingBuilder setPathLength(int pathLength) {
      this.pathLength = pathLength;
      return this;
    }

    public SettingBuilder setStopTime(long stopTime) {
      this.stopTime = stopTime;
      return this;
    }

    public Setting build() {
      return new Setting(vehicleLength, timeWindow, minTimeSteps,
          explorationFreq, intentionFreq, intentionChangingThreshold,
          pheromoneLifeTime, mapSizeX, mapSizeY, blockSize, numberOfAgents,
          dynamicRate, pathLength, stopTime);
    }
  }
}
