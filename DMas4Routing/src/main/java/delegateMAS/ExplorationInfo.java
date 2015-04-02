package delegateMAS;

import java.util.List;

import com.github.rinde.rinsim.geom.Point;

public class ExplorationInfo {

  private List<Point> path;
  private double distance;
  
  public ExplorationInfo(List<Point> path, double distance) {
    this.path = path;
    this.distance = distance;
  }
  
  public List<Point> getPath() {
    return path;
  }

  public double getDistance() {
    return distance;
  }
  
  
}
