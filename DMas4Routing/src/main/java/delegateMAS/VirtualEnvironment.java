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
   * Explore all possible routes between 2 nodes in which the length
   * is smaller than maxLength
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
    
    List<Route> listOfAllRoutes = explore(listOfRoutes, goal, 1, maxLength);
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
  public List<Route> explore(List<Route> listOfRoutes, Point goal, int length,
      int maxLength) {
    
    if (length == maxLength) {
      return listOfRoutes;
    }
    
    List<Route> newListOfRoutes = new ArrayList<>();

    for (Route route : listOfRoutes) {
      Point lastNode = route.getLastNode();
      
      if (!lastNode.equals(goal)) {
        Collection<Point> outGoingNodes = graphRoadModel.get().getGraph()
            .getOutgoingConnections(lastNode);

        for (Point node : outGoingNodes) {
          if (!route.getRoute().contains(node)) {
            Route newRoute = (Route) route.clone();
            newRoute.addNextNode(node);
            newListOfRoutes.add(newRoute);
          }
        }
      } else {
        newListOfRoutes.add(route);
      }
    }
    
    return explore(newListOfRoutes, goal, length + 1, maxLength);
  }
}
