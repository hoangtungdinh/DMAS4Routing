package delegateMAS;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.rinde.rinsim.core.TickListener;
import com.github.rinde.rinsim.core.TimeLapse;
import com.github.rinde.rinsim.core.model.road.GraphRoadModel;
import com.github.rinde.rinsim.geom.Connection;
import com.github.rinde.rinsim.geom.ConnectionData;
import com.github.rinde.rinsim.geom.Point;
import com.google.common.base.Optional;

public class VirtualEnvironment implements TickListener {
  
  /** The graph road model. */
  Optional<GraphRoadModel> graphRoadModel;
  private Map<Connection<? extends ConnectionData>, ResourceAgent> edgeAgents;
  private Map<Point, ResourceAgent> nodeAgents;
  
  /**
   * Instantiates a new virtual environment.
   *
   * @param graphRoadModel the road model
   */
  public VirtualEnvironment(GraphRoadModel graphRoadModel) {
    this.graphRoadModel = Optional.of(graphRoadModel);
    
    Set<Point> nodes = graphRoadModel.getGraph().getNodes();
    
    nodeAgents = new HashMap<Point, ResourceAgent>();
    
    for (Point p : nodes) {
      nodeAgents.put(p, new ResourceAgent());
    }
    
    edgeAgents = new HashMap<Connection<? extends ConnectionData>, ResourceAgent>();
    
    for (Point p : nodes) {
      final Collection<Point> outGoingPoints = graphRoadModel.getGraph()
          .getOutgoingConnections(p);
      for (Point p1 : outGoingPoints) {
        final Connection<? extends ConnectionData> conn1 = graphRoadModel
            .getGraph().getConnection(p, p1);
        final Connection<? extends ConnectionData> conn2 = graphRoadModel
            .getGraph().getConnection(p1, p);
        final ResourceAgent resourceAgent = new ResourceAgent();
        edgeAgents.put(conn1, resourceAgent);
        edgeAgents.put(conn2, resourceAgent);
      }
    }    
  }
  
  /**
   * Explore and find the fastest way from start to goal
   *
   * @param agentID the agent id
   * @param start the start
   * @param goal the goal
   * @param currentTime the current time
   * @param deadline the deadline
   * @return the array list
   */
  public ArrayList<Point> explore(int agentID, Point start, Point goal, long currentTime,
      long deadline) {
    List<Route> routeList = new ArrayList<Route>();
    // first route contains only start point
    ArrayList<Point> firstRoute = new ArrayList<Point>();
    firstRoute.add(start);
    routeList.add(new Route(firstRoute));
    
    long time = currentTime;

    while (time < deadline) {
      // explore until reaching deadline
      time += 1000;
      final int maxLength = (int) (deadline - time) / 1000;
      // list of visited nodes in the current time step
      final List<Point> visitedNodes = new ArrayList<Point>();
      final ArrayList<Route> tmpRouteList = new ArrayList<Route>();
      for (Route route : routeList) {
        final Point lastNode = route.getLastNode();
        // check if staying at same node is possible
        // first check if investigated node is visited in this time step
        if (!visitedNodes.contains(lastNode)) {
          visitedNodes.add(lastNode);
          // check if it is enough time to go from this node to goal
          // and if it is available in this time step (no reservation yet)
          if (getShortestDistance(lastNode, goal) < maxLength
              && nodeAgents.get(lastNode).isAvailable(agentID, time)) {
            final ArrayList<Point> newRoute = route.getRoute();
            newRoute.add(lastNode);
            if (lastNode.equals(goal)) {
              // if reached goal then return
              return newRoute;
            } else {
              tmpRouteList.add(new Route(newRoute));
            }
          }
        }

        // check for each outgoing node
        final Collection<Point> outgoingNodes = graphRoadModel.get().getGraph()
            .getOutgoingConnections(lastNode);
        for (Point nextNode : outgoingNodes) {
          // check if node is already visited in this step 
          // and do not allow cycle
          if (!visitedNodes.contains(nextNode) && !route.contains(nextNode)) {
            // check if it is still possible to go from this node to goal
            if (getShortestDistance(nextNode, goal) < maxLength) {
              final ResourceAgent nodeAgent = nodeAgents.get(nextNode);
              // check if next node is available in this time step
              if (nodeAgent.isAvailable(agentID, time)) {
                // if next node is available, check if edge from current node to
                // next node is available
                final ResourceAgent edgeAgent = edgeAgents.get(graphRoadModel
                    .get().getGraph().getConnection(lastNode, nextNode));
                if (edgeAgent.isAvailable(agentID, time)) {
                  // if next node can be reached, add to visited nodes
                  visitedNodes.add(nextNode);
                  final ArrayList<Point> newRoute = route.getRoute();
                  newRoute.add(nextNode);
                  if (nextNode.equals(goal)) {
                    // if reached goal then return
                    return newRoute;
                  } else {
                    tmpRouteList.add(new Route(newRoute));
                  }
                }
              } else {
                // if next node is not available in this time step, mark it as
                // visited node
                visitedNodes.add(nextNode);
              }
            } else {
              // if not enough time to go from this node to goal, mark it as
              // visited node
              visitedNodes.add(nextNode);
            }
          }
        }
      }
      if (tmpRouteList.isEmpty()) {
        break;
      } else {
        routeList = tmpRouteList;
      }
    }

    System.out.println(currentTime + "  " + time);
    // if can't reach goal, then return arbitrary route
    return routeList.get(0).getRoute();
  }
  
  /**
   * Book resource.
   *
   * @param agentID the agent id
   * @param path the path
   * @param start the start point
   * @param currentTime the current time
   * @return true, if book successfully
   */
  public boolean bookResource(int agentID, ArrayList<Point> path, Point start,
      long currentTime) {
    if (path.size() < 1) {
      return false;
    }
    
    long time = currentTime;
    Point currentNode = start;
    
    for (Point nextNode : path) {
      time += 1000;
      if (nextNode.equals(currentNode)) {
        // stay at the same position, only need to book node
        final ResourceAgent nodeAgent = nodeAgents.get(nextNode);
        final boolean bookingResponse = nodeAgent.bookResource(agentID, time);
        // if can't book, return false
        if (!bookingResponse) {
          return false;
        }
      } else {
        // move to next node, book both edge and the next node
        final ResourceAgent nodeAgent = nodeAgents.get(nextNode);
        final ResourceAgent edgeAgent = edgeAgents.get(graphRoadModel.get()
            .getGraph().getConnection(currentNode, nextNode));
        final boolean nodeResponse = nodeAgent.bookResource(agentID, time);
        final boolean edgeResponse = edgeAgent.bookResource(agentID, time);
        // if can't book node or edge, return false
        if (!nodeResponse || !edgeResponse) {
          return false;
        }
      }
      currentNode = nextNode;
    }
    
    return true;
  }
  
  /**
   * Gets the shortest distance between 2 nodes
   *
   * @param p1 the p1
   * @param p2 the p2
   * @return the shortest distance
   */
  public int getShortestDistance(Point p1, Point p2) {
    return graphRoadModel.get().getShortestPathTo(p1, p2).size();
  }

  @Override
  public void tick(TimeLapse timeLapse) {
  }

  @Override
  public void afterTick(TimeLapse timeLapse) {
    for (Map.Entry<Point, ResourceAgent> entry : nodeAgents.entrySet()) {
      entry.getValue().refesh();
    }

    for (Map.Entry<Connection<? extends ConnectionData>, ResourceAgent> entry : edgeAgents
        .entrySet()) {
      entry.getValue().refesh();
    }
  }
}
