package delegateMAS;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.github.rinde.rinsim.core.model.road.GraphRoadModel;
import com.github.rinde.rinsim.geom.Point;
import com.google.common.base.Optional;

public class VirtualEnvironment {
  
  /** The graph road model. */
  Optional<GraphRoadModel> graphRoadModel;
  
  /**
   * Instantiates a new virtual environment.
   *
   * @param graphRoadModel the road model
   */
  public VirtualEnvironment(GraphRoadModel graphRoadModel) {
    this.graphRoadModel = Optional.of(graphRoadModel);
  }
  
  /**
   * Explore routes between 2 nodes. Ants move and drop pheromone in each node
   * If ant move to a node that contain pheromone of its ancestor, it will be deleted.
   * 
   *
   * @param start the start
   * @param goal the goal
   * @param maxLength the max length
   * @return the list of all possible routes
   */
  public List<Route> explore(Point start, Point goal, int maxLength) {
    Route route = new Route();
    route.addNextNode(start);
    List<Route> listOfRoutes = new ArrayList<Route>();
    listOfRoutes.add(route);
    List<Point> listOfReachedPoints = new ArrayList<Point>();
    listOfReachedPoints.add(start);

    List<Route> listOfAllRoutes = explore(listOfRoutes, listOfReachedPoints,
        goal, maxLength);
    List<Route> listOfLegalRoutes = new ArrayList<Route>();

    for (Route oneRoute : listOfAllRoutes) {
      if (oneRoute.getLastNode().equals(goal)) {
        listOfLegalRoutes.add(oneRoute);
      }
    }

    return listOfLegalRoutes;
  }
  
  /**
   * Explore.
   *
   * @param listOfRoutes the list of current routes
   * @param goal the goal
   * @param length the length
   * @param maxLength the max length
   * @return the list of routes
   */
  public List<Route> explore(List<Route> listOfRoutes,
      List<Point> listOfReachedPoints, Point goal, double maxLength) {
    
    if (maxLength <= 0) {
      return listOfRoutes;
    }
    
    List<Route> newListOfRoutes = new ArrayList<>();

    for (Route route : listOfRoutes) {
      Point lastNode = route.getLastNode();
      
      if (!lastNode.equals(goal)) {
        Collection<Point> outGoingNodes = graphRoadModel.get().getGraph()
            .getOutgoingConnections(lastNode);

        for (Point node : outGoingNodes) {
          
          if (!listOfReachedPoints.contains(node)
              && getEuclideanDistance(node, goal) <= maxLength) {
            Route newRoute = (Route) route.clone();
            newRoute.addNextNode(node);
            newListOfRoutes.add(newRoute);
            if (!node.equals(goal)) {
              listOfReachedPoints.add(node);
            }
          }
        }
      } else {
        newListOfRoutes.add(route);
      }
    }
    
    return explore(newListOfRoutes, listOfReachedPoints, goal, maxLength - 4.0);
  }
  
  /**
   * Gets the euclidean distance.
   *
   * @param p1 the p1
   * @param p2 the p2
   * @return the euclidean distance
   */
  public double getEuclideanDistance(Point p1, Point p2) {
    return Math.sqrt((p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y)
        * (p1.y - p2.y));
  }
}
