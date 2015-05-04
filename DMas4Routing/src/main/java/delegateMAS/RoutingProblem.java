package delegateMAS;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;

import com.github.rinde.rinsim.core.Simulator;
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

  private Setting setting;
  private String fileID;
  private boolean viewOn;

  public RoutingProblem(Setting setting, String fileID, boolean viewOn) {
    this.setting = setting;
    this.fileID = fileID;
    this.viewOn = viewOn;
  }

  public Result run() {

    CollisionGraphRoadModel collisionGraphRoadModel = CollisionGraphRoadModel
        .builder(loadRandomMap()).setVehicleLength(setting.getVehicleLength())
        .build();

    final Simulator sim = Simulator.builder().addModel(collisionGraphRoadModel)
        .build();

    VirtualEnvironment virtualEnvironment = new VirtualEnvironment(
        collisionGraphRoadModel, sim, setting.getDynamicRate(),
        setting.getTimeWindow(), setting.getPheromoneLifeTime());
    sim.addTickListener(virtualEnvironment);
    
    final List<Point> destinations = getDestinations(collisionGraphRoadModel,
        setting.getNumberOfAgents(), sim.getRandomGenerator());

    for (int i = 0; i < setting.getNumberOfAgents(); i++) {
      sim.register(new AGV(sim.getRandomGenerator(), virtualEnvironment, i,
          setting.getMinTimeSteps(), setting.getExplorationFreq(), setting
              .getIntentionFreq(), setting.getIntentionChangingThreshold(),
          setting.getPathLength(), setting.getFailureRate(), destinations.get(i)));
    }

    Result result = new Result(collisionGraphRoadModel, sim, setting, fileID,
        virtualEnvironment);
    
    sim.addTickListener(result);

    if (viewOn) {
      View.create(sim)
      .setFullScreen()
      .with(
          WarehouseRenderer.builder().setMargin(2).showNodes()
              .showNodeOccupancy())
      .with(
          AGVRenderer.builder().useDifferentColorsForVehicles()
              .showVehicleOrigin().showVehicleCreationNumber()).show();
    } else {
      sim.start();
    }
    
    return result;
  }

  public ImmutableTable<Integer, Integer, Point> createMatrix(int cols,
      int rows, Point offset) {
    final ImmutableTable.Builder<Integer, Integer, Point> builder = ImmutableTable
        .builder();
    for (int c = 0; c < cols; c++) {
      for (int r = 0; r < rows; r++) {
        builder.put(r, c, new Point(offset.x + c * setting.getVehicleLength()
            * 3, offset.y + r * setting.getVehicleLength() * 3));
      }
    }
    return builder.build();
  }

  public Graph<LengthData> createGraphStructure() {
    final Graph<LengthData> g = new TableGraph<>();

    final Table<Integer, Integer, Point> matrix = createMatrix(
        setting.getMapSizeX(), setting.getMapSizeY(), new Point(0, 0));

    int i = 0;
    for (final Map<Integer, Point> column : matrix.columnMap().values()) {
      if (i % setting.getBlockSize() == 0) {
        Graphs.addBiPath(g, column.values());
      }
      i++;
    }

    int j = 0;
    for (final Map<Integer, Point> row : matrix.rowMap().values()) {
      if (j % setting.getBlockSize() == 0) {
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

    final Table<Integer, Integer, Point> matrix = createMatrix(
        setting.getMapSizeX(), setting.getMapSizeY(), new Point(0, 0));

    for (final Map<Integer, Point> column : matrix.columnMap().values()) {
      Graphs.addBiPath(g, column.values());
    }

    Graphs.addBiPath(g, matrix.row(setting.getMapSizeY() / 2).values());
    Graphs.addBiPath(g, matrix.row((setting.getMapSizeY() / 2) - 1).values());

    return new ListenableGraph<>(g);
  }
  
  public ListenableGraph<LengthData> createGraph3() {
    final Graph<LengthData> g = new TableGraph<>();

    final Table<Integer, Integer, Point> matrix = createMatrix(
        setting.getMapSizeX(), setting.getMapSizeY(), new Point(0, 0));

    for (final Map<Integer, Point> column : matrix.columnMap().values()) {
      Graphs.addBiPath(g, column.values());
    }

    for (final Map<Integer, Point> row : matrix.rowMap().values()) {
      Graphs.addBiPath(g, row.values());
    }

    Set<Point> nodeSet = g.getNodes();
    List<Point> nodeList = new ArrayList<Point>(nodeSet);
    Collections.shuffle(nodeList);

    int numOfRemovedNodes = g.getNumberOfNodes() * 20 / 100;

    List<Point> removedNodes = new ArrayList<>(nodeList.subList(0, numOfRemovedNodes));

    for (Point node : removedNodes) {
      g.removeNode(node);
    }
    
    BenchmarkMap map = new BenchmarkMap(removedNodes);
    
    try {
      ObjectOutputStream objectOutputStream = new ObjectOutputStream(
          new FileOutputStream("Map5"));
      objectOutputStream.writeObject(map);
      objectOutputStream.close();
      
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }

    return new ListenableGraph<>(g);
  }
  
  public ListenableGraph<LengthData> loadRandomMap() {
    final Graph<LengthData> g = new TableGraph<>();

    final Table<Integer, Integer, Point> matrix = createMatrix(
        setting.getMapSizeX(), setting.getMapSizeY(), new Point(0, 0));

    for (final Map<Integer, Point> column : matrix.columnMap().values()) {
      Graphs.addBiPath(g, column.values());
    }

    for (final Map<Integer, Point> row : matrix.rowMap().values()) {
      Graphs.addBiPath(g, row.values());
    }

    BenchmarkMap map;
    
    FileInputStream fin;
    try {
      fin = new FileInputStream("Map2");
      ObjectInputStream ois = new ObjectInputStream(fin);
      map = (BenchmarkMap) ois.readObject();
      fin.close();
    } catch (Exception e) {
      e.printStackTrace();
      map = null;
    }

    List<Point> removedNodes = new ArrayList<>(map.getRemovedNodes());

    for (Point node : removedNodes) {
      g.removeNode(node);
    }

    return new ListenableGraph<>(g);
  }

  public List<Point> getDestinations(CollisionGraphRoadModel roadModel,
      int numOfAgents, RandomGenerator r) {
    List<Point> destinations = new ArrayList<>();
    while (destinations.size() < numOfAgents) {
      final Point point = roadModel.getGraph().getRandomNode(r);
      if (destinations.contains(point)) {
        continue;
      } else {
        destinations.add(point);
      }
    }
    
    return destinations;
  }
}