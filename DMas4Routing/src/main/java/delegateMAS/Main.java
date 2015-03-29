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

/**
 * Example showcasing the {@link CollisionGraphRoadModel} with an
 * {@link WarehouseRenderer} and {@link AGVRenderer}.
 * @author Rinde van Lon
 */
public final class Main {

  private static final double VEHICLE_LENGTH = 2d;

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

    for (int i = 0; i < 1; i++) {
      sim.register(new VehicleAgent(sim.getRandomGenerator(), virtualEnvironment));
    }

    View.create(sim)
        .with(WarehouseRenderer.builder()
            .setMargin(VEHICLE_LENGTH).showNodes().showNodeOccupancy()
        )
        .with(AGVRenderer.builder()
            .useDifferentColorsForVehicles().showVehicleOrigin()
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

  static ListenableGraph<LengthData> createGraph() {
    final Graph<LengthData> g = new TableGraph<>();
    
    final Table<Integer, Integer, Point> matrix = createMatrix(4, 4,
        new Point(0, 0));
    for (final Map<Integer, Point> column : matrix.columnMap().values()) {
      Graphs.addBiPath(g, column.values());
    }
    for (final Map<Integer, Point> row : matrix.rowMap().values()) {
      Graphs.addBiPath(g, row.values());
    }
    return new ListenableGraph<>(g);
  }
}