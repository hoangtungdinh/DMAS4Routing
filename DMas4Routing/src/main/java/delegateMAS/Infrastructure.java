package delegateMAS;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import com.github.rinde.rinsim.core.model.road.GraphRoadModel;
import com.github.rinde.rinsim.geom.Point;

public class Infrastructure {
  
  private Map<Point, ResourceAgent> resourceAgents;
  
  public Infrastructure(GraphRoadModel graphRoadModel) {
    int numberOfNodes = graphRoadModel.getGraph().getNumberOfNodes();
    Set<Point> nodes = graphRoadModel.getGraph().getNodes();
    
    resourceAgents = new HashMap<Point, ResourceAgent>(numberOfNodes);
    for (Point node : nodes) {
      resourceAgents.put(node, new ResourceAgent());
    }
  }
  
  public void refresh() {
    for (Map.Entry<Point, ResourceAgent> entry : resourceAgents.entrySet()) {
      entry.getValue().refresh();
    }
  }
  
  public boolean isAvailable(int agentID, Point startNode, Point node, int timeStep) {
    Reservation reservation = resourceAgents.get(node).getBookingInfo(timeStep);
    Reservation previousReservation = resourceAgents.get(node).getBookingInfo(timeStep - 1);
    
    if ((reservation == null || reservation.getAgentID() == agentID)
        && (previousReservation == null
            || previousReservation.getAgentID() == agentID || !previousReservation
            .getNextNode().equals(startNode))) {
      return true;
    } else {
      return false;
    }
  }
  
  public boolean book(int agentID, LinkedList<Point> path) {
    boolean successful = true;
    
    if (path.size() > 1) {
      successful = resourceAgents.get(path.get(0)).book(agentID, path.get(0),
          path.get(1), 0);
    } else {
      successful = resourceAgents.get(path.get(0)).book(agentID, path.get(0),
          path.get(0), 0);
    }
    
    if (!successful) {
      return false;
    }

    // book other positions
    for (int i = 1; i < path.size() - 1; i++) {
      // TODO put constrain on the length of route here
      successful = resourceAgents.get(path.get(i)).book(agentID,
          path.get(i - 1), path.get(i + 1), i);

      if (!successful) {
        return false;
      }
    }

    // book last position
    Point goal = path.get(path.size() - 1);
    successful = resourceAgents.get(goal).book(
        agentID,
        path.get((path.size() - 2) > 0 ? (path
            .size() - 2) : 0), goal, path.size() - 1);

    return successful;
  }
}
