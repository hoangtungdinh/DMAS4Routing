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

  private Setting setting;

  RoutingProblem(Setting setting) {
    this.setting = setting;
  }

  public void run() {

    CollisionGraphRoadModel collisionGraphRoadModel = CollisionGraphRoadModel
        .builder(createGraph2()).setVehicleLength(setting.getVehicleLength())
        .build();

    final Simulator sim = Simulator.builder().addModel(collisionGraphRoadModel)
        .build();

    VirtualEnvironment virtualEnvironment = new VirtualEnvironment(
        collisionGraphRoadModel, sim, setting.getDynamicRate(),
        setting.getTimeWindow(), setting.getPheromoneLifeTime());
    sim.addTickListener(virtualEnvironment);

    for (int i = 0; i < setting.getNumberOfAgents(); i++) {
      sim.register(new AGV(sim.getRandomGenerator(), virtualEnvironment, i,
          setting.getMinTimeSteps(), setting.getExplorationFreq(), setting
              .getIntentionFreq(), setting.getIntentionChangingThreshold(),
          setting.getPathLength()));
    }
    
    sim.addTickListener(new TickListener() {
      @Override
      public void tick(TimeLapse timeLapse) {
      }

      @Override
      public void afterTick(TimeLapse timeLapse) {
        if (timeLapse.getTime() >= setting.getStopTime()) {
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

    final Table<Integer, Integer, Point> matrix = createMatrix(10, 10,
        new Point(0, 0));

    for (final Map<Integer, Point> column : matrix.columnMap().values()) {
      Graphs.addBiPath(g, column.values());
    }

    Graphs.addBiPath(g, matrix.row(4).values());
    Graphs.addBiPath(g, matrix.row(5).values());

    return new ListenableGraph<>(g);
  }
}