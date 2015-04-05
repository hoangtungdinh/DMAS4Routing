package delegateMAS;

import java.util.ArrayList;

import com.github.rinde.rinsim.geom.Point;


public class Route {
  
  private ArrayList<Point> route;
  
  public Route(ArrayList<Point> route) {
    this.route = route;
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
}
