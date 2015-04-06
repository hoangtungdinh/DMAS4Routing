package delegateMAS;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.Stack;
import java.util.TreeMap;

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
  public static final int HOPS_AHEAD = 5;
  public static final long TIME_WINDOW = 10000;
  
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
  
  public ArrayList<Point> explore(int agentID, Point start, Point goal,
      long currentTime) {
    // long time = currentTime;
    // long deadline = currentTime;
    //
    // do {
    // deadline += 1000;
    // } while ((deadline % TIME_WINDOW) != 0);
    
    // set of investigated node and time slot
    final Set<TimeNode> visitedNodes = new HashSet<>();
    
    // sorted queue of routes
    final SortedMap<Integer, Route> routeQueue = new TreeMap<>();
    
    // initialize the first route and add it to the queue
    ArrayList<Point> firstRoute = new ArrayList<Point>();
    firstRoute.add(start);
    final Route startRoute = new Route(firstRoute);
    routeQueue.put(getEstimatedCost(startRoute, goal), startRoute);
    
    while (!routeQueue.isEmpty()) {
      // select and remove the first route in the queue
      final Route route = routeQueue.remove(routeQueue.firstKey());
      final Point lastNode = route.getLastNode();
      // if the last node of the route is goal, then return
      if (lastNode.equals(goal) && route.getRoute().size() > 1) {
        return route.getRoute();
      }
      // list of all possible next nodes (outgoing nodes and this node)
      final List<Point> outgoingNodes = new ArrayList<>();
      outgoingNodes.addAll(graphRoadModel.get().getGraph()
          .getOutgoingConnections(lastNode));
      outgoingNodes.add(lastNode);
      // investigated time slot: current time + size of route * 1000
      final long time = currentTime + route.getRoute().size() * 1000;
      for (Point nextNode : outgoingNodes) {
        // pair of node and time slot
        final TimeNode timeNode = new TimeNode(nextNode, time);
        // if this pair hasn't been investigated
        if (!visitedNodes.contains(timeNode)) {
          // mark it as investigated
          visitedNodes.add(timeNode);
          // check if this pair (node and time slot) is available
          final ResourceAgent nodeAgent = nodeAgents.get(nextNode);
          if (nodeAgent.isAvailable(agentID, time)) {
            // if this pair is available
            if (nextNode.equals(lastNode)) {
              // if next node is similar to current node (AGV stays at the same
              // position), then create new route with this node and add to the
              // queue
              final ArrayList<Point> newPath = route.getRoute();
              newPath.add(nextNode);
              final Route newRoute = new Route(newPath);
              routeQueue.put(getEstimatedCost(newRoute, goal), newRoute);
            } else {
              // if next node is different from current node, check also the
              // edge between them
              final ResourceAgent edgeAgent = edgeAgents.get(graphRoadModel
                  .get().getGraph().getConnection(lastNode, nextNode));
              if (edgeAgent.isAvailable(agentID, time)) {
                // if the edge is also available, then create new route and add
                // it to the queue
                final ArrayList<Point> newPath = route.getRoute();
                newPath.add(nextNode);
                final Route newRoute = new Route(newPath);
                routeQueue.put(getEstimatedCost(newRoute, goal), newRoute);
              }
            }
          }
        }
      }
    }
    
    System.out.println("FAIL");
    return exploreHopsAhead(agentID, firstRoute, currentTime);
  }
  
  /**
   * Gets the estimated cost of a route.
   *
   * @param route the route
   * @param goal the goal
   * @return the estimated cost
   */
  public Integer getEstimatedCost(Route route, Point goal) {
    int gValue = route.getRoute().size();
    int hValue = getShortestPathDistance(route.getLastNode(), goal);
    return gValue + hValue - 1;
  }
  
  @SuppressWarnings("unchecked")
  public ArrayList<Point> exploreHopsAhead(int agentID, ArrayList<Point> path,
      long currentTime) {
    Stack<Route> routeStack = new Stack<Route>();
    routeStack.push(new Route(path));
    ArrayList<Point> longestRoute = (ArrayList<Point>) path.clone();
    
    int initialLength = path.size();
    
    while (!routeStack.isEmpty()) {
      final Route route = routeStack.pop();
      final Point lastNode = route.getLastNode();
      // list of all possible next nodes (outgoing nodes and this node)
      final List<Point> outgoingNodes = new ArrayList<>();
      outgoingNodes.addAll(graphRoadModel.get().getGraph()
          .getOutgoingConnections(lastNode));
      outgoingNodes.add(lastNode);
      for (Point nextNode : outgoingNodes) {
        final long time = currentTime
            + (route.getRoute().size() - initialLength + 1) * 1000;
        // check if node is available
        final ResourceAgent nodeAgent = nodeAgents.get(nextNode);
        if (nodeAgent.isAvailable(agentID, time)) {
          if (nextNode.equals(lastNode)) {
            // if next node is similar to last node (agv doesn't move)
            final ArrayList<Point> newRoute = route.getRoute();
            newRoute.add(nextNode);
            if (newRoute.size() - initialLength == HOPS_AHEAD) {
              // if reached required length then return
              return newRoute;
            } else {
              if (newRoute.size() > longestRoute.size()) {
                // else store the longest route
                longestRoute = (ArrayList<Point>) newRoute.clone();
              }
              // push new route into stack
              routeStack.push(new Route(newRoute));
            }
          } else {
            // if next node is different from current node, check also the
            // edge between them
            final ResourceAgent edgeAgent = edgeAgents.get(graphRoadModel.get()
                .getGraph().getConnection(lastNode, nextNode));
            if (edgeAgent.isAvailable(agentID, time)) {
              // if edge is also available
              final ArrayList<Point> newRoute = route.getRoute();
              newRoute.add(nextNode);
              if (newRoute.size() - initialLength == HOPS_AHEAD) {
                // if reached required length then return
                return newRoute;
              } else {
                if (newRoute.size() > longestRoute.size()) {
                  // else store the longest route
                  longestRoute = (ArrayList<Point>) newRoute.clone();
                }
                // push new route into stack
                routeStack.push(new Route(newRoute));
              }
            }
          }
        }
      }
    }

    return longestRoute;
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
   * Gets the shortest path distance between 2 nodes
   * 
   * Basically, euclidean distance and shortest path distance
   * both are good heuristic criteria (because they are both underestimate).
   * Shortest path distance is better because it is closer to the real value.
   * Euclidean distance is faster because it doesn't have to perform A*
   *
   * @param p1 the p1
   * @param p2 the p2
   * @return the shortest distance
   */
  public int getShortestPathDistance(Point p1, Point p2) {
    return graphRoadModel.get().getShortestPathTo(p1, p2).size();
  }
  
  /**
   * Gets the euclidean distance.
   * 
   * Basically, euclidean distance and shortest path distance
   * both are good heuristic criteria (because they are both underestimate).
   * Shortest path distance is better because it is closer to the real value.
   * Euclidean distance is faster because it doesn't have to perform A*
   *
   * @param p1 the p1
   * @param p2 the p2
   * @return the euclidean distance
   */
  public double getEuclideanDistance(Point p1, Point p2) {
    return Point.distance(p1, p2);
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
