package delegateMAS;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ResourceAgent {

  public static final int LIFE_TIME = 5;
  private List<Reservation> reservations;
  
  public ResourceAgent() {
    reservations = new ArrayList<Reservation>();
  }
  
  /**
   * Checks if a timeslot is available.
   *
   * @param agentID the agent id
   * @param time the time
   * @return true, if is available
   */
  public boolean isAvailable(int agentID, long time) {
    for (Reservation resv : reservations) {
      if (resv.getReservedTime() == time && resv.getAgentID() != agentID) {
        return false;
      }
    }
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
    if (isAvailable(agentID, time)) {
      reservations.add(new Reservation(time, agentID, LIFE_TIME));
      return true;
    } else {
      return false;
    }
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
}
