package delegateMAS;

import java.util.ArrayList;
import java.util.List;

import com.github.rinde.rinsim.geom.Point;

public class ResourceAgent {
  
  /** The Constant EVAPORATION_RATE. */
  public static final int EVAPORATION_RATE = 50;
  
  /**  The Constant CAPACITY, the number of reservations ahead. */
  public static final int CAPACITY = 100;
  
  /** The reservations. */
  private List<Reservation> reservations;
  
  /**
   * Instantiates a new resource agent.
   */
  public ResourceAgent() {
    reservations = new ArrayList<>(CAPACITY);
    for (int i = 0; i < CAPACITY; i++) {
      reservations.add(i, null);
    }
  }
  
  /**
   * Gets the booking information at a time step.
   *
   * @param timeStep the time step
   * @return the booking info
   */
  public Reservation getBookingInfo(int timeStep) {
    return reservations.get(timeStep);
  }
  
  /**
   * Make a reservation. Has to provide the next node and the time step ahead.
   *
   * @param nextNode the next node
   * @param timestep the time step ahead
   * @return true, if successful
   */
  public boolean book(int agentID, Point startNode, Point nextNode, int timestep) {
    if (timestep > 0) {
      boolean condition1 = reservations.get(timestep) == null
          || reservations.get(timestep).getAgentID() == agentID;
      boolean condition2 = reservations.get(timestep - 1) == null
          || reservations.get(timestep - 1).getAgentID() == agentID
          || !reservations.get(timestep - 1).getNextNode().equals(startNode);

      if (condition1 && condition2) {
        reservations.set(timestep, new Reservation(agentID, nextNode,
            EVAPORATION_RATE));
        return true;
      } else {
        return false;
      }
    } else {
//      boolean condition1 = reservations.get(timestep) == null
//          || reservations.get(timestep).getAgentID() == agentID;
      
      // TODO think about strategy for booking for the first step
      boolean condition1 = true;

      if (condition1) {
        reservations.set(timestep, new Reservation(agentID, nextNode,
            EVAPORATION_RATE));
        return true;
      } else {
        return false;
      }
    }
  }
  
  /**
   * Refresh.
   */
  public void refresh() {
    reservations.remove(0);
    reservations.add(null);
    
    for (Reservation reservation : reservations) {
      if (reservation != null) {
        reservation.refresh();
        if (reservation.getDuration() == 0) {
          reservation = null;
        }
      }
    }
  }
}
