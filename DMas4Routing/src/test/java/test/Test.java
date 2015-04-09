package test;

import java.util.Map;
import java.util.Set;

import com.github.rinde.rinsim.geom.Graph;
import com.github.rinde.rinsim.geom.Graphs;
import com.github.rinde.rinsim.geom.LengthData;
import com.github.rinde.rinsim.geom.Point;
import com.github.rinde.rinsim.geom.TableGraph;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;

public class Test {

  public static void main(String[] args) {
    Graph<LengthData> graph = createGraphStructure();
    Point point = new Point(0d, 0d);
    System.out.println("test");
    Set<Point> allPoints = graph.getNodes();
    long a = System.currentTimeMillis();
    int i = 0;
    for (Point p : allPoints) {
      i += Graphs.shortestPathEuclideanDistance(graph, p, point).size();
    }
    long b = System.currentTimeMillis();
    System.out.println(b-a);
    System.out.println(i);
  }
  
  public static Graph<LengthData> createGraphStructure() {
    final Graph<LengthData> g = new TableGraph<>();

    final Table<Integer, Integer, Point> matrix = createMatrix(
        50, 50, new Point(0, 0));

    for (final Map<Integer, Point> column : matrix.columnMap().values()) {
        Graphs.addBiPath(g, column.values());
    }

    for (final Map<Integer, Point> row : matrix.rowMap().values()) {
        Graphs.addBiPath(g, row.values());
    }
    return g;
  }
    
    public static ImmutableTable<Integer, Integer, Point> createMatrix(int cols,
        int rows, Point offset) {
      final ImmutableTable.Builder<Integer, Integer, Point> builder = ImmutableTable
          .builder();
      for (int c = 0; c < cols; c++) {
        for (int r = 0; r < rows; r++) {
          builder.put(r, c, new Point(offset.x + c * 2
              * 3, offset.y + r * 2 * 3));
        }
      }
      return builder.build();
    }
}
