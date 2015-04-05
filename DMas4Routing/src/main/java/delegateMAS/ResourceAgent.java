package delegateMAS;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ResourceAgent {

  public static final int LIFE_TIME = 8*5;
  private List<Reservation> reservations;
  
  public ResourceAgent() {
    reservations = new ArrayList<Reservation>();
  }
  
  /**
   * Checks if a time slot is available.
   *
   * @param agentID the agent id
   * @param time the time
   * @return true, if is available
   */
  public boolean isAvailable(int agentID, long time) {
    for (Reservation resv : reservations) {
      // if time slot is reserved
      if (resv.getReservedTime() == time) {
        if (resv.getAgentID() != agentID) {
          // if other agent booked, then false
          return false;
        } else {
          // if this agent booked, then true
          return true;
        }
      }
    }
    // if time slot hasn't been reserved, then true
    return true;
  }
  
  /**
   * Book resource.
   *
   * @param agentID the agent id
   * @param time the time
   * @return true, if book successfully
   */
  public boolean bookResource(int agentID, long time) {
    for (Reservation resv : reservations) {
      // if time slot is reserved
      if (resv.getReservedTime() == time) {
        if (resv.getAgentID() != agentID) {
          // if other agent booked, then false
          return false;
        } else {
          // if this agent booked, then true
          resv.setLifeTime(LIFE_TIME);
          return true;
        }
      }
    }
    // if time slot hasn't been reserved, then true
    reservations.add(new Reservation(time, agentID, LIFE_TIME));
    return true;
  }
  
  public void refesh() {
    for (Reservation resv : reservations) {
      resv.refesh();
    }
    
    Iterator<Reservation> iterator = reservations.iterator();
    while (iterator.hasNext()) {
      final Reservation resv = iterator.next();
      if (resv.getLifeTime() == 0) {
        iterator.remove();
      }
    }
  }
  
  public List<Reservation> getReservations() {
    return reservations;
  }
}
