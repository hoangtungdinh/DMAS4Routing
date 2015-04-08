package delegateMAS;

import java.util.Map;

import com.github.rinde.rinsim.core.Simulator;
import com.github.rinde.rinsim.core.TickListener;
import com.github.rinde.rinsim.core.TimeLapse;
import com.github.rinde.rinsim.core.model.road.CollisionGraphRoadModel;
import com.github.rinde.rinsim.geom.Graph;
import com.github.rinde.rinsim.geom.Graphs;
import com.github.rinde.rinsim.geom.LengthData;
import com.github.rinde.rinsim.geom.ListenableGraph;
import com.github.rinde.rinsim.geom.Point;
import com.github.rinde.rinsim.geom.TableGraph;
import com.github.rinde.rinsim.ui.View;
import com.github.rinde.rinsim.ui.renderers.AGVRenderer;
import com.github.rinde.rinsim.ui.renderers.WarehouseRenderer;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;

public class RoutingProblem {

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

  RoutingProblem(double vehicleLength, int timeWindow, int minTimeSteps,
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

  public void run() {

    CollisionGraphRoadModel collisionGraphRoadModel = CollisionGraphRoadModel
        .builder(createGraph2()).setVehicleLength(vehicleLength)
        .build();

    final Simulator sim = Simulator.builder().addModel(collisionGraphRoadModel)
        .build();

    VirtualEnvironment virtualEnvironment = new VirtualEnvironment(
        collisionGraphRoadModel, sim, dynamicRate, timeWindow,
        pheromoneLifeTime);
    sim.addTickListener(virtualEnvironment);

    for (int i = 0; i < numberOfAgents; i++) {
      sim.register(new AGV(sim.getRandomGenerator(), virtualEnvironment, i,
          minTimeSteps, explorationFreq, intentionFreq,
          intentionChangingThreshold, pathLength));
    }
    
    sim.addTickListener(new TickListener() {
      @Override
      public void tick(TimeLapse timeLapse) {
      }

      @Override
      public void afterTick(TimeLapse timeLapse) {
        if (timeLapse.getTime() >= stopTime) {
          sim.stop();
        }
      }
    });

    View.create(sim)
        .with(
            WarehouseRenderer.builder().setMargin(2).showNodes()
                .showNodeOccupancy())
        .with(
            AGVRenderer.builder().useDifferentColorsForVehicles()
                .showVehicleOrigin().showVehicleCreationNumber()).show();
  }

  public ImmutableTable<Integer, Integer, Point> createMatrix(int cols,
      int rows, Point offset) {
    final ImmutableTable.Builder<Integer, Integer, Point> builder = ImmutableTable
        .builder();
    for (int c = 0; c < cols; c++) {
      for (int r = 0; r < rows; r++) {
        builder.put(r, c, new Point(offset.x + c * vehicleLength * 3,
            offset.y + r * vehicleLength * 3));
      }
    }
    return builder.build();
  }

  public Graph<LengthData> createGraphStructure() {
    final Graph<LengthData> g = new TableGraph<>();

    final Table<Integer, Integer, Point> matrix = createMatrix(
        mapSizeX, mapSizeY, new Point(0, 0));

    int i = 0;
    for (final Map<Integer, Point> column : matrix.columnMap().values()) {
      if (i % blockSize == 0) {
        Graphs.addBiPath(g, column.values());
      }
      i++;
    }

    int j = 0;
    for (final Map<Integer, Point> row : matrix.rowMap().values()) {
      if (j % blockSize == 0) {
        Graphs.addBiPath(g, row.values());
      }
      j++;
    }
    return g;
  }

  public ListenableGraph<LengthData> createGraph() {
    return new ListenableGraph<>(createGraphStructure());
  }

  public ListenableGraph<LengthData> createGraph1() {
    final Graph<LengthData> g = new TableGraph<>();

    final Table<Integer, Integer, Point> matrix = createMatrix(5, 10,
        new Point(0, 0));
    for (final Map<Integer, Point> column : matrix.columnMap().values()) {
      Graphs.addBiPath(g, column.values());
    }
    Graphs.addBiPath(g, matrix.row(4).values());
    Graphs.addBiPath(g, matrix.row(5).values());

    final Table<Integer, Integer, Point> matrix2 = createMatrix(10, 7,
        new Point(30, 6));
    for (final Map<Integer, Point> row : matrix2.rowMap().values()) {
      Graphs.addBiPath(g, row.values());
    }
    Graphs.addBiPath(g, matrix2.column(0).values());
    Graphs.addBiPath(g, matrix2.column(matrix2.columnKeySet().size() - 1)
        .values());

    Graphs.addBiPath(g, matrix2.get(2, 0), matrix.get(4, 4));
    Graphs.addBiPath(g, matrix.get(5, 4), matrix2.get(4, 0));

    return new ListenableGraph<>(g);
  }

  public ListenableGraph<LengthData> createGraph2() {
    final Graph<LengthData> g = new TableGraph<>();

    final Table<Integer, Integer, Point> matrix = createMatrix(10, 10,
        new Point(0, 0));

    for (final Map<Integer, Point> column : matrix.columnMap().values()) {
      Graphs.addBiPath(g, column.values());
    }

    Graphs.addBiPath(g, matrix.row(4).values());
    Graphs.addBiPath(g, matrix.row(5).values());

    return new ListenableGraph<>(g);
  }

  public static class Builder {
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
    
    public Builder() {
      
    }

    public Builder setVehicleLength(double vehicleLength) {
      this.vehicleLength = vehicleLength;
      return this;
    }

    public Builder setTimeWindow(int timeWindow) {
      this.timeWindow = timeWindow;
      return this;
    }

    public Builder setMinTimeSteps(int minTimeSteps) {
      this.minTimeSteps = minTimeSteps;
      return this;
    }

    public Builder setExplorationFreq(int explorationFreq) {
      this.explorationFreq = explorationFreq;
      return this;
    }

    public Builder setIntentionFreq(int intentionFreq) {
      this.intentionFreq = intentionFreq;
      return this;
    }

    public Builder setIntentionChangingThreshold(int intentionChangingThreshold) {
      this.intentionChangingThreshold = intentionChangingThreshold;
      return this;
    }

    public Builder setPheromoneLifeTime(int pheromoneLifeTime) {
      this.pheromoneLifeTime = pheromoneLifeTime;
      return this;
    }

    public Builder setMapSizeX(int mapSizeX) {
      this.mapSizeX = mapSizeX;
      return this;
    }

    public Builder setMapSizeY(int mapSizeY) {
      this.mapSizeY = mapSizeY;
      return this;
    }

    public Builder setBlockSize(int blockSize) {
      this.blockSize = blockSize;
      return this;
    }

    public Builder setNumberOfAgents(int numberOfAgents) {
      this.numberOfAgents = numberOfAgents;
      return this;
    }

    public Builder setDynamicRate(int dynamicRate) {
      this.dynamicRate = dynamicRate;
      return this;
    }

    public Builder setPathLength(int pathLength) {
      this.pathLength = pathLength;
      return this;
    }

    public Builder setStopTime(long stopTime) {
      this.stopTime = stopTime;
      return this;
    }

    public RoutingProblem build() {
      return new RoutingProblem(vehicleLength, timeWindow, minTimeSteps,
          explorationFreq, intentionFreq, intentionChangingThreshold,
          pheromoneLifeTime, mapSizeX, mapSizeY, blockSize, numberOfAgents,
          dynamicRate, pathLength, stopTime);
    }
  }
}