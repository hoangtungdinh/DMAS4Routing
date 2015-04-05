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
  
  // TODO check correctness here
  public ArrayList<Route> explore(int agentID, Point start, Point goal, long currentTime,
      long deadline) {
    ArrayList<Route> routeList = new ArrayList<Route>();
    ArrayList<Point> firstRoute = new ArrayList<Point>();
    firstRoute.add(start);
    routeList.add(new Route(firstRoute));
    
    long time = currentTime;

    while (time < deadline) {
      time += 1000;
      final ArrayList<Route> tmpRouteList = new ArrayList<Route>();
      for (Route route : routeList) {
        final Point lastNode = route.getLastNode();
        // if lastNode is not goal node
        if (!lastNode.equals(goal)) {
          final int maxLength = (int) (deadline - time) / 1000;
          // check if staying at same node is possible
          if (getShortestDistance(lastNode, goal) < maxLength
              && nodeAgents.get(lastNode).isAvailable(agentID, time)) {
            final ArrayList<Point> newRoute = route.getRoute();
            newRoute.add(lastNode);
            tmpRouteList.add(new Route(newRoute));
          }

          // check for each outgoing node
          final Collection<Point> outgoingNodes = graphRoadModel.get()
              .getGraph().getOutgoingConnections(lastNode);
          for (Point nextNode : outgoingNodes) {
            if (getShortestDistance(lastNode, nextNode) < maxLength) {
              final ResourceAgent nodeAgent = nodeAgents.get(nextNode);
              final ResourceAgent edgeAgent = edgeAgents.get(graphRoadModel
                  .get().getGraph().getConnection(lastNode, nextNode));
              if (nodeAgent.isAvailable(agentID, time)
                  && edgeAgent.isAvailable(agentID, time)) {
                final ArrayList<Point> newRoute = route.getRoute();
                newRoute.add(nextNode);
                tmpRouteList.add(new Route(newRoute));
              }
            }
          }
        } else {
          tmpRouteList.add(route);
        }
      }
      routeList = tmpRouteList;
    }

    return routeList;
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
