package delegateMAS;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.github.rinde.rinsim.core.TickListener;
import com.github.rinde.rinsim.core.TimeLapse;
import com.github.rinde.rinsim.core.model.road.GraphRoadModel;
import com.github.rinde.rinsim.geom.Point;
import com.google.common.base.Optional;

public class VirtualEnvironment implements TickListener {
  
  /** The graph road model. */
  Optional<GraphRoadModel> graphRoadModel;
  
  private Infrastructure infrastructure;
  
  /**
   * Instantiates a new virtual environment.
   *
   * @param graphRoadModel the road model
   */
  public VirtualEnvironment(GraphRoadModel graphRoadModel) {
    this.graphRoadModel = Optional.of(graphRoadModel);
    this.infrastructure = new Infrastructure(graphRoadModel);
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
  public List<Route> explore(int agentID, Point start, Point goal, int maxLength) {
    Route route = new Route();
    route.addNextNode(start);
    
    List<Route> listOfRoutes = new ArrayList<Route>();
    listOfRoutes.add(route);
    List<Point> listOfReachedPoints = new ArrayList<Point>();
    listOfReachedPoints.add(start);

    List<Route> listOfAllRoutes = explore(agentID, listOfRoutes, listOfReachedPoints,
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
  public List<Route> explore(int agentID, List<Route> listOfRoutes,
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
              && Point.distance(node, goal) <= maxLength
              && infrastructure.isAvailable(agentID, lastNode, node, route
                  .getRoute().size())) {
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
    
    return explore(agentID, newListOfRoutes, listOfReachedPoints, goal, maxLength - 4.0);
  }
  
  /**
   * Intention ant books a route.
   *
   * @param agentID the agent id
   * @param path the path
   * @return true, if successful
   */
  public boolean book(int agentID, LinkedList<Point> path) {
    return infrastructure.book(agentID, path);
  }
  
  /**
   * Refresh.
   */
  public void refresh() {
    infrastructure.refresh();
  }

  @Override
  public void tick(TimeLapse timeLapse) {
    refresh();
  }

  @Override
  public void afterTick(TimeLapse timeLapse) {}
}
