package delegateMAS;

import static com.google.common.collect.Maps.newHashMap;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

import com.github.rinde.rinsim.core.Simulator;
import com.github.rinde.rinsim.core.model.road.CollisionGraphRoadModel;
import com.github.rinde.rinsim.geom.Graph;
import com.github.rinde.rinsim.geom.Graphs;
import com.github.rinde.rinsim.geom.LengthData;
import com.github.rinde.rinsim.geom.ListenableGraph;
import com.github.rinde.rinsim.geom.MultiAttributeData;
import com.github.rinde.rinsim.geom.Point;
import com.github.rinde.rinsim.geom.TableGraph;
import com.github.rinde.rinsim.geom.io.DotGraphIO;
import com.github.rinde.rinsim.geom.io.Filters;
import com.github.rinde.rinsim.ui.View;
import com.github.rinde.rinsim.ui.renderers.AGVRenderer;
import com.github.rinde.rinsim.ui.renderers.WarehouseRenderer;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;

public final class RoutingInRealMap {
  
  private static final String MAP_FILE = "leuven-simple.dot";
  private static final Map<String, Graph<MultiAttributeData>> GRAPH_CACHE = newHashMap();

  private RoutingInRealMap() {}

  /**
   * @param args - No args.
   */
  public static void main(String[] args) {

    final String graphFile = args != null && args.length >= 2 ? args[1]
        : MAP_FILE;

    CollisionGraphRoadModel collisionGraphRoadModel = CollisionGraphRoadModel
        .builder(new ListenableGraph<>(loadGraph(graphFile))).setVehicleLength(Setting.VEHICLE_LENGTH).build();
    
    final Simulator sim = Simulator.builder()
        .addModel(collisionGraphRoadModel)
        .build();
    
    VirtualEnvironment virtualEnvironment = new VirtualEnvironment(
        collisionGraphRoadModel, sim);
    sim.addTickListener(virtualEnvironment);

    for (int i = 0; i < Setting.NUM_AGENTS; i++) {
      sim.register(new AGV(sim.getRandomGenerator(), virtualEnvironment, i));
    }
    
//    sim.addTickListener(new TickListener() {
//      @Override
//      public void tick(TimeLapse timeLapse) {}
//
//      @Override
//      public void afterTick(TimeLapse timeLapse) {
//        if (timeLapse.getTime() >= Setting.STOP_TIME) {
//          sim.stop();
//        }
//      }
//    });
    
    View.create(sim)
        .with(WarehouseRenderer.builder()
            .setMargin(2).showNodes().showNodeOccupancy()
        )
        .with(AGVRenderer.builder()
            .useDifferentColorsForVehicles().showVehicleOrigin().showVehicleCreationNumber()
        )
        .show();
  }
  
//load the graph file
 static Graph<MultiAttributeData> loadGraph(String name) {
   try {
     if (GRAPH_CACHE.containsKey(name)) {
       return GRAPH_CACHE.get(name);
     }
     final Graph<MultiAttributeData> g = DotGraphIO
         .getMultiAttributeGraphIO(
             Filters.selfCycleFilter()).read(
             RoutingInRealMap.class.getResourceAsStream(name));

     GRAPH_CACHE.put(name, g);
     return g;
   } catch (final FileNotFoundException e) {
     throw new IllegalStateException(e);
   } catch (final IOException e) {
     throw new IllegalStateException(e);
   }
 }
  
  
  
  
  
  
  
  
  
  

  static ImmutableTable<Integer, Integer, Point> createMatrix(int cols,
      int rows, Point offset) {
    final ImmutableTable.Builder<Integer, Integer, Point> builder = ImmutableTable
        .builder();
    for (int c = 0; c < cols; c++) {
      for (int r = 0; r < rows; r++) {
        builder.put(r, c, new Point(
            offset.x + c * Setting.VEHICLE_LENGTH * 3,
            offset.y + r * Setting.VEHICLE_LENGTH * 3));
      }
    }
    return builder.build();
  }
  
  static Graph<LengthData> createGraphStructure() {
    final Graph<LengthData> g = new TableGraph<>();

    final Table<Integer, Integer, Point> matrix = createMatrix(
        Setting.MAP_SIZE_X, Setting.MAP_SIZE_Y, new Point(0, 0));
    
    int i = 0;
    for (final Map<Integer, Point> column : matrix.columnMap().values()) {
      if (i % Setting.BLOCK_SIZE == 0) {
        Graphs.addBiPath(g, column.values());
      }
      i++;
    }
    
    int j = 0;
    for (final Map<Integer, Point> row : matrix.rowMap().values()) {
      if (j % Setting.BLOCK_SIZE == 0) {
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