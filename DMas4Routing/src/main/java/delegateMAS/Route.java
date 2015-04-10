package delegateMAS;

import java.util.ArrayList;

import com.github.rinde.rinsim.geom.Point;


public class Route {
  
  private ArrayList<Point> route;
  private int distanceToGoal;
  
  public Route(ArrayList<Point> route) {
    this(route, -1);
  }
  
  public Route(ArrayList<Point> route, int distanceToGoal) {
    this.route = route;
    this.distanceToGoal = distanceToGoal;
  }
  
  public ArrayList<Point> getRoute() {
    return new ArrayList<>(route);
  }
  
  public int getDistanceToGoal() {
    return distanceToGoal;
  }
  
  public void addNode(Point node) {
    route.add(node);
  }
  
  public Point getLastNode() {
    return route.get(route.size() - 1);
  }
  
  public boolean contains(Point point) {
    return route.contains(point);
  }
}
