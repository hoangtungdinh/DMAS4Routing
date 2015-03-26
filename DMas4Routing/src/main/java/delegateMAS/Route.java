package delegateMAS;

import java.util.ArrayList;
import java.util.List;

import com.github.rinde.rinsim.geom.Point;

public class Route implements Cloneable {
  
  /** The route, which is list of Point instances. */
  private List<Point> route;
  
  /**
   * Instantiates a new route.
   */
  public Route() {
    route = new ArrayList<Point>();
  }
  
  public Route(List<Point> route) {
    this.route = route;
  }
  
  /**
   * Adds the next node.
   *
   * @param nextNode the next node
   */
  public void addNextNode(Point nextNode) {
    route.add(nextNode);
  }
  
  /**
   * Gets the route.
   *
   * @return the route
   */
  public List<Point> getRoute() {
    return route;
  }
  
  /**
   * Gets the last node.
   *
   * @return the last node
   */
  public Point getLastNode() {
    return route.get(route.size() - 1);
  }
  
  /**
   * Checks if a node is in route.
   *
   * @param node the node
   * @return true, if is the node is in route
   */
  public boolean isInRoute(Point node) {
    for (Point point : route) {
      if (point.equals(node)) {
        return true;
      }
    }
    
    return false;
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#clone()
   */
  public Route clone() {
    List<Point> clone = new ArrayList<Point>();
    
    for (Point point : route) {
      clone.add(point);
    }
    
    return new Route(clone);
  }

}
