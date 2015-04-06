package delegateMAS;

import java.util.ArrayList;

import com.github.rinde.rinsim.geom.Point;


public class Route {
  
  private ArrayList<Point> route;
  private boolean containsDestination;
  
  public Route(ArrayList<Point> route) {
    this(route, false);
  }
  
  public Route(ArrayList<Point> route, boolean containsDestination) {
    this.route = route;
    this.containsDestination = containsDestination;
  }
  
  @SuppressWarnings("unchecked")
  public ArrayList<Point> getRoute() {
    return (ArrayList<Point>) route.clone();
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
  
  public boolean containsDestination() {
    return containsDestination;
  }
}
