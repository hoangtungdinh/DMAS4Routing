package delegateMAS;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.rinde.rinsim.core.Simulator;
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
  private Simulator simulator;
  private Map<Connection<? extends ConnectionData>, EdgeAgent> edgeAgents;
  private Map<Point, NodeAgent> nodeAgents;
  
  /**
   * Instantiates a new virtual environment.
   *
   * @param graphRoadModel the road model
   */
  public VirtualEnvironment(GraphRoadModel graphRoadModel, Simulator simulator) {
    this.graphRoadModel = Optional.of(graphRoadModel);
    this.simulator = simulator;
    
    nodeAgents = new HashMap<Point, NodeAgent>();
    Set<Point> nodes = graphRoadModel.getGraph().getNodes();
    for (Point p : nodes) {
      nodeAgents.put(p, new NodeAgent());
    }
    
    edgeAgents = new HashMap<Connection<? extends ConnectionData>, EdgeAgent>();
    for (Connection<? extends ConnectionData> conn : graphRoadModel.getGraph()
        .getConnections()) {
       edgeAgents.put(conn, new EdgeAgent(conn.getLength()));
    }
  }

  /**
   * Explore.
   *
   * @param agentID the agent id
   * @param path the path
   * @param speed the speed
   * @return the estimated arrival time
   */
  public long explore(int agentID, List<Point> path, double speed) {
    long time = simulator.getCurrentTime();
    Point p1;
    Point p2;
    Connection<? extends ConnectionData> conn;
    
    for (int i = 0; i < path.size() - 1; i++) {
      p1 = path.get(i);
      p2 = path.get(i + 1);
      // TODO solve the problem of one path having 2 connections
      // only check opposite direction
      conn = graphRoadModel.get().getGraph().getConnection(p2, p1);
      time = edgeAgents.get(conn).checkAvailability(agentID, time, speed);
      if (time == -1 || !nodeAgents.get(p2).checkAvailability(agentID, time)) {
        return -1;
      }
    }
    
    return time;
  }
  
  /**
   * Make reservation.
   *
   * @param agentID the agent id
   * @param path the path
   * @param speed the speed
   * @return true, if make reservation successfully
   */
  public boolean makeReservation(int agentID, List<Point> path, double speed) {
    long time = simulator.getCurrentTime();
    Point p1;
    Point p2;
    Connection<? extends ConnectionData> conn;
    
    if (path.size() == 1) {
      p1 = path.get(0);
      return nodeAgents.get(p1).makeReservation(agentID, time);
    } else {
      for (int i = 0; i < path.size() - 1; i++) {
        p1 = path.get(i);
        p2 = path.get(i + 1);
        conn = graphRoadModel.get().getGraph().getConnection(p1, p2);
        time = edgeAgents.get(conn).makeReservation(agentID, time, speed);
        if (time == -1 || !nodeAgents.get(p2).makeReservation(agentID, time)) {
          return false;
        }
      }
    }
    
    return true;
  }

  @Override
  public void tick(TimeLapse timeLapse) {
  }

  @Override
  public void afterTick(TimeLapse timeLapse) {
    for (Map.Entry<Point, NodeAgent> entry : nodeAgents.entrySet()) {
      entry.getValue().refesh();
    }

    for (Map.Entry<Connection<? extends ConnectionData>, EdgeAgent> entry : edgeAgents
        .entrySet()) {
      entry.getValue().refesh();
    }
  }
  
  public void printCurrentTime() {
    System.out.println(simulator.getCurrentTime());
  }
}
