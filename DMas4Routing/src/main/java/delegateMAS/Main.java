package delegateMAS;

import java.util.Map;

import com.github.rinde.rinsim.core.Simulator;
import com.github.rinde.rinsim.core.model.road.CollisionGraphRoadModel;
import com.github.rinde.rinsim.core.model.road.GraphRoadModel;
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

public final class Main {

  private static final double VEHICLE_LENGTH = 2d;
  private static final int MAP_SIZE_X = 4;
  private static final int MAP_SIZE_Y = 4;
  private static final int NUM_AGENTS = 3;

  private Main() {}

  /**
   * @param args - No args.
   */
  public static void main(String[] args) {

    CollisionGraphRoadModel collisionGraphRoadModel = CollisionGraphRoadModel
        .builder(createGraph()).setVehicleLength(VEHICLE_LENGTH).build();
    
    final Simulator sim = Simulator.builder()
        .addModel(collisionGraphRoadModel)
        .build();
    
    VirtualEnvironment virtualEnvironment = new VirtualEnvironment(
        (GraphRoadModel) collisionGraphRoadModel);
    sim.addTickListener(virtualEnvironment);

    for (int i = 0; i < NUM_AGENTS; i++) {
      sim.register(new AGV(sim.getRandomGenerator(), virtualEnvironment));
    }

    View.create(sim)
        .with(WarehouseRenderer.builder()
            .setMargin(2).showNodes().showNodeOccupancy()
        )
        .with(AGVRenderer.builder()
            .useDifferentColorsForVehicles().showVehicleOrigin().showVehicleCreationNumber()
        )
        .show();
  }

  static ImmutableTable<Integer, Integer, Point> createMatrix(int cols,
      int rows, Point offset) {
    final ImmutableTable.Builder<Integer, Integer, Point> builder = ImmutableTable
        .builder();
    for (int c = 0; c < cols; c++) {
      for (int r = 0; r < rows; r++) {
        builder.put(r, c, new Point(
            offset.x + c * VEHICLE_LENGTH * 2,
            offset.y + r * VEHICLE_LENGTH * 2));
      }
    }
    return builder.build();
  }
  
  static Graph<LengthData> createGraphStructure() {
    final Graph<LengthData> g = new TableGraph<>();
    
    final Table<Integer, Integer, Point> matrix = createMatrix(MAP_SIZE_X, MAP_SIZE_Y,
        new Point(0, 0));
    
    int i = 0;
    for (final Map<Integer, Point> column : matrix.columnMap().values()) {
      if (i % 1 == 0) {
        Graphs.addBiPath(g, column.values());
      }
      i++;
    }
    
    int j = 0;
    for (final Map<Integer, Point> row : matrix.rowMap().values()) {
      if (j % 1 == 0) {
        Graphs.addBiPath(g, row.values());
      }
      j++;
    }
    return g;
  }

  static ListenableGraph<LengthData> createGraph() {
    return new ListenableGraph<>(createGraphStructure());
  }
}