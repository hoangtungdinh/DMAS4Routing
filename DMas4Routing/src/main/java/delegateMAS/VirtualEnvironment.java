package delegateMAS;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.Stack;
import java.util.TreeMap;

import org.apache.commons.math3.random.RandomGenerator;

import com.github.rinde.rinsim.core.Simulator;
import com.github.rinde.rinsim.core.TickListener;
import com.github.rinde.rinsim.core.TimeLapse;
import com.github.rinde.rinsim.core.model.road.CollisionGraphRoadModel;
import com.github.rinde.rinsim.geom.Connection;
import com.github.rinde.rinsim.geom.ConnectionData;
import com.github.rinde.rinsim.geom.Point;
import com.google.common.base.Optional;

public class VirtualEnvironment implements TickListener {
  
  /** The graph road model. */
  Optional<CollisionGraphRoadModel> roadModel;
  private Map<Connection<? extends ConnectionData>, ResourceAgent> edgeAgents;
  private Map<Point, ResourceAgent> nodeAgents;
  private RandomGenerator r;
  private int dynamicRate;
  private int timeWindow;
  private int numberOfSucesses;
  
  private Point removedNode1 = null;
  private Point removedNode2 = null;
  
  /**
   * Instantiates a new virtual environment.
   *
   * @param roadModel the road model
   */
  public VirtualEnvironment(CollisionGraphRoadModel roadModel, Simulator sim,
      int dynamicRate, int timeWindow, int pheromoneLifeTime) {
    this.r = sim.getRandomGenerator();
    this.dynamicRate = dynamicRate;
    this.timeWindow = timeWindow;
    this.roadModel = Optional.of(roadModel);
    this.numberOfSucesses = 0;
    
    Set<Point> nodes = roadModel.getGraph().getNodes();
    
    nodeAgents = new HashMap<Point, ResourceAgent>();
    
    for (Point p : nodes) {
      nodeAgents.put(p, new ResourceAgent(pheromoneLifeTime));
    }
    
    edgeAgents = new HashMap<Connection<? extends ConnectionData>, ResourceAgent>();
    
    for (Point p : nodes) {
      final Collection<Point> outGoingPoints = roadModel.getGraph()
          .getOutgoingConnections(p);
      for (Point p1 : outGoingPoints) {
        final Connection<? extends ConnectionData> conn1 = roadModel.getGraph()
            .getConnection(p, p1);
        final Connection<? extends ConnectionData> conn2 = roadModel.getGraph()
            .getConnection(p1, p);
        final ResourceAgent resourceAgent = new ResourceAgent(pheromoneLifeTime);
        edgeAgents.put(conn1, resourceAgent);
        edgeAgents.put(conn2, resourceAgent);
      }
    }
  }
  
  public Route explore(int agentID, int priority, Point start, Point goal,
      long currentTime) {
    // required length
    int length = timeWindow;
    
    // set of investigated node and time slot
    final Set<TimeNode> visitedNodes = new HashSet<>();
    
    // sorted queue of routes
    final SortedMap<Double, Route> routeQueue = new TreeMap<>();
    
    // initialize the first route and add it to the queue
    ArrayList<Point> firstRoute = new ArrayList<Point>();
    firstRoute.add(start);
    final Route startRoute = new Route(firstRoute);
    routeQueue.put(getEstimatedCost(startRoute, goal), startRoute);

    Route longestRoute = new Route(firstRoute);
    
    while (!routeQueue.isEmpty()) {
      // select and remove the first route in the queue
      final Route route = routeQueue.remove(routeQueue.firstKey());

      if (getHammingDistance(route.getLastNode(), goal) < getHammingDistance(
          longestRoute.getLastNode(), goal)) {
        longestRoute = new Route(route.getRoute());
      } else if (getHammingDistance(route.getLastNode(), goal) == getHammingDistance(
          longestRoute.getLastNode(), goal)) {
        if (r.nextBoolean()) {
          longestRoute = new Route(route.getRoute());
        }
      }

      final Point lastNode = route.getLastNode();
      if (route.getRoute().size() > length) {
        // if reached required length, then return
        return new Route(route.getRoute());
      } else if (lastNode.equals(goal) && route.getRoute().size() > 1) {
        // if reached goal, extend to route to fit the time window, then returns
        ArrayList<Point> rawRoute = exploreHopsAhead(agentID, priority,
            route.getRoute(), currentTime, length);
        return new Route(rawRoute, route.getRoute().size());
      }
      // list of all possible next nodes (outgoing nodes and this node)
      final List<Point> outgoingNodes = new ArrayList<>();
      outgoingNodes.addAll(roadModel.get().getGraph()
          .getOutgoingConnections(lastNode));
      outgoingNodes.add(lastNode);
      // investigated time slot: current time + size of route * 1000
      final long time = currentTime + route.getRoute().size() * 1000;
      for (Point nextNode : outgoingNodes) {
        if (route.getRoute().size() == 1 && !nextNode.equals(lastNode)
            && roadModel.get().isOccupied(nextNode)) {
          continue;
        }
        // pair of node and time slot
        final TimeNode timeNode = new TimeNode(nextNode, time);
        // if this pair hasn't been investigated
        if (!visitedNodes.contains(timeNode)) {
          // check if this pair (node and time slot) is available
          final ResourceAgent nodeAgent = nodeAgents.get(nextNode);
          if (nodeAgent.isAvailable(agentID, getPriority(goal, nextNode), time)) {
            // if this pair is available
            if (nextNode.equals(lastNode)) {
              // if next node is similar to current node (AGV stays at the same
              // position), then create new route with this node and add to the
              // queue
              final ArrayList<Point> newPath = route.getRoute();
              newPath.add(nextNode);
              final Route newRoute = new Route(newPath);
              double estimatedCost = getEstimatedCost(newRoute, goal);
              while (routeQueue.containsKey(estimatedCost)) {
                estimatedCost = Double.longBitsToDouble(Double
                    .doubleToLongBits(estimatedCost) + 1);
              }
              routeQueue.put(estimatedCost, newRoute);
              // mark it as investigated
              visitedNodes.add(timeNode);
            } else {
              // if the previous and next time steps are available (to avoid
              // collision)
              if (nodeAgent.isAvailable(agentID, getPriority(goal, nextNode), time - 1000)
                  && nodeAgent.isAvailable(agentID, getPriority(goal, nextNode), time + 1000)) {
                // if next node is different from current node, check also the
                // edge between them
                final ResourceAgent edgeAgent = edgeAgents.get(roadModel.get()
                    .getGraph().getConnection(lastNode, nextNode));
                if (edgeAgent.isAvailable(agentID, getPriority(goal, nextNode), time)) {
                  // if the edge is also available, then create new route and
                  // add
                  // it to the queue
                  final ArrayList<Point> newPath = route.getRoute();
                  newPath.add(nextNode);
                  final Route newRoute = new Route(newPath);
                  double estimatedCost = getEstimatedCost(newRoute, goal);
                  while (routeQueue.containsKey(estimatedCost)) {
                    estimatedCost = Double.longBitsToDouble(Double
                        .doubleToLongBits(estimatedCost) + 1);
                  }
                  routeQueue.put(estimatedCost, newRoute);
                  // mark it as investigated
                  visitedNodes.add(timeNode);
                }
              } else {
                // mark it as investigated
                visitedNodes.add(timeNode);
              }
            }
          } else {
            // mark it as investigated
            visitedNodes.add(timeNode);
          }
        }
      }
    }
    
//    if (longestRoute.getRoute().size() == 1) {
//      ResourceAgent nodeAgent = nodeAgents.get(longestRoute.getRoute().get(0));
//      nodeAgent.setDeadlockWarning(agentID);
//    }
//    return new Route(longestRoute.getRoute());
    
    ResourceAgent nodeAgent = nodeAgents.get(startRoute.getRoute().get(0));
    nodeAgent.setDeadlockWarning(agentID);
    return startRoute;
  }
  
  /**
   * Gets the estimated cost of a route.
   *
   * @param route the route
   * @param goal the goal
   * @return the estimated cost
   */
  public Double getEstimatedCost(Route route, Point goal) {
    double gValue = route.getRoute().size();
    double hValue = getHammingDistance(route.getLastNode(), goal);
    return (gValue + hValue - 1);
  }
  
  @SuppressWarnings("unchecked")
  public ArrayList<Point> exploreHopsAhead(int agentID, int priority,
      ArrayList<Point> path, long currentTime, int length) {
    // set of investigated node and time slot
    final Set<TimeNode> visitedNodes = new HashSet<>();
    
    final Point goal = path.get(path.size() - 1);
    
    Stack<Route> routeStack = new Stack<Route>();
    routeStack.push(new Route((ArrayList<Point>) path.clone()));
    ArrayList<Point> longestRoute = (ArrayList<Point>) path.clone();
    
    while (!routeStack.isEmpty()) {
      final Route route = routeStack.pop();
      if (route.getRoute().size() > length) {
        return route.getRoute();
      }
      final Point lastNode = route.getLastNode();
      // list of all possible next nodes (outgoing nodes and this node)
      final List<Point> outgoingNodes = new ArrayList<>();
      outgoingNodes.addAll(roadModel.get().getGraph()
          .getOutgoingConnections(lastNode));
      // shuffle whole list
      Collections.shuffle(outgoingNodes);
      // prefer lastnode first
      outgoingNodes.add(lastNode);
      for (Point nextNode : outgoingNodes) {
        if (route.getRoute().size() == 1 && !nextNode.equals(lastNode)
            && roadModel.get().isOccupied(nextNode)) {
          continue;
        }
        final long time = currentTime + route.getRoute().size() * 1000;
        final TimeNode timeNode = new TimeNode(nextNode, time);
        if (!visitedNodes.contains(timeNode)) {
          // check if node is available
          final ResourceAgent nodeAgent = nodeAgents.get(nextNode);
          if (nodeAgent.isAvailable(agentID, getPriority(goal, nextNode), time)) {
            if (nextNode.equals(lastNode)) {
              // if next node is similar to last node (agv doesn't move)
              final ArrayList<Point> newRoute = route.getRoute();
              newRoute.add(nextNode);
              if (newRoute.size() > longestRoute.size()) {
                // store the longest route
                longestRoute = (ArrayList<Point>) newRoute.clone();
              }
              // push new route into stack
              routeStack.push(new Route(newRoute));
              visitedNodes.add(timeNode);
            } else {
              // check previous and next time steps to avoid collision
              if (nodeAgent.isAvailable(agentID, getPriority(goal, nextNode), time - 1000)
                  && nodeAgent.isAvailable(agentID, getPriority(goal, nextNode), time + 1000)) {
                // if next node is different from current node, check also the
                // edge between them
                final ResourceAgent edgeAgent = edgeAgents.get(roadModel.get()
                    .getGraph().getConnection(lastNode, nextNode));
                if (edgeAgent.isAvailable(agentID, getPriority(goal, nextNode), time)) {
                  // if edge is also available
                  final ArrayList<Point> newRoute = route.getRoute();
                  newRoute.add(nextNode);
                  if (newRoute.size() > longestRoute.size()) {
                    // store the longest route
                    longestRoute = (ArrayList<Point>) newRoute.clone();
                  }
                  // push new route into stack
                  routeStack.push(new Route(newRoute));
                  visitedNodes.add(timeNode);
                }
              } else {
                visitedNodes.add(timeNode);
              }
            }
          } else {
            visitedNodes.add(timeNode);
          }
        }
      }
    }
    
    // if is at goal
    if (path.get(0).equals(path.get(path.size() - 1))) {
      ResourceAgent nodeAgent = nodeAgents.get(path.get(0));
      nodeAgent.setDeadlockWarning(agentID);
      final ArrayList<Point> startRoute = new ArrayList<>();
      startRoute.add(path.get(0));
      return startRoute;
    } else {
      return path;
    }
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
  public boolean bookResource(int agentID, int priority, ArrayList<Point> path, Point start,
      long currentTime, Point goal) {
    if (path.size() < 1) {
      return false;
    } else if (!start.equals(path.get(0))
        && roadModel.get().isOccupied(path.get(0))) {
      return false;
    }
    
    long time = currentTime;
    Point currentNode = start;
    
    for (Point nextNode : path) {
      time += 1000;
      if (nextNode.equals(currentNode)) {
        // stay at the same position, only need to book node
        final ResourceAgent nodeAgent = nodeAgents.get(nextNode);
        final boolean bookingResponse = nodeAgent.bookResource(agentID,
            getPriority(goal, nextNode), time);
        // if can't book, return false
        if (!bookingResponse) {
          return false;
        }
      } else {
        if (!roadModel.get().getGraph().hasConnection(currentNode, nextNode)) {
          return false;
        }
        // move to next node, book both edge and the next node
        final ResourceAgent nodeAgent = nodeAgents.get(nextNode);
        final ResourceAgent edgeAgent = edgeAgents.get(roadModel.get()
            .getGraph().getConnection(currentNode, nextNode));
        final boolean nodeResponse = nodeAgent.bookResource(agentID, getPriority(goal, nextNode),
            time);
        final boolean edgeResponse = edgeAgent.bookResource(agentID, getPriority(goal, nextNode),
            time);
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
  public static int getShortestPathDistance(CollisionGraphRoadModel roadModel,
      Point p1, Point p2) {
    return roadModel.getShortestPathTo(p1, p2).size();
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
  public static double getEuclideanDistance(Point p1, Point p2) {
    return Point.distance(p1, p2) / 6;
  }
  
  public static double getHammingDistance(Point p1, Point p2) {
    return (Math.abs(p1.x - p2.x) + Math.abs(p1.y - p2.y)) / 6;
  }

  @Override
  public void tick(TimeLapse timeLapse) {
  }

  @Override
  public void afterTick(TimeLapse timeLapse) {
    for (Map.Entry<Point, ResourceAgent> entry : nodeAgents.entrySet()) {
      entry.getValue().evolve();
    }

    for (Map.Entry<Connection<? extends ConnectionData>, ResourceAgent> entry : edgeAgents
        .entrySet()) {
      entry.getValue().evolve();
    }
    
    changeGraphStructure();
  }
  
  public void changeGraphStructure() {
    if (r.nextInt(100) + 1 <= dynamicRate) {
      if (removedNode1 != null && removedNode2 != null) {
        roadModel.get().getGraph().addConnection(removedNode1, removedNode2);
        roadModel.get().getGraph().addConnection(removedNode2, removedNode1);
      }
      
      Point randNode = null;
      Point nextNode = null;

      do {
        randNode = roadModel.get().getGraph().getRandomNode(r);
        final Collection<Point> nextNodes = roadModel.get().getGraph()
            .getOutgoingConnections(randNode);
        final List<Point> listNextNodes = new ArrayList<>(nextNodes);
        nextNode = listNextNodes.get(r.nextInt(listNextNodes.size()));
      } while (randNode == removedNode1 || randNode == removedNode2
          || nextNode == removedNode1 || nextNode == removedNode2);

      roadModel.get().getGraph().removeConnection(randNode, nextNode);
      roadModel.get().getGraph().removeConnection(nextNode, randNode);
      removedNode1 = randNode;
      removedNode2 = nextNode;
    }
  }
  
  public void increaseSuccesses() {
    numberOfSucesses++;
  }
  
  public void decreaseSuccesses() {
    numberOfSucesses--;
  }
  
  public int getNumberOfSuccesses() {
    return numberOfSucesses;
  }
  
  public int getPriority(Point goal, Point nextNode) {
    if (goal.equals(nextNode)) {
      return 1;
    } else {
      return 0;
    }
  }
}
