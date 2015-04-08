package delegateMAS;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ResourceAgent {

  private List<Reservation> reservations;
  private DeadlockWarning deadlockWarning;
  
  public ResourceAgent() {
    reservations = new ArrayList<Reservation>();
    deadlockWarning = new DeadlockWarning(0, false, 0);
  }
  
  /**
   * Checks if a time slot is available.
   *
   * @param agentID the agent id
   * @param time the time
   * @return true, if is available
   */
  public boolean isAvailable(int agentID, long time) {
    if (deadlockWarning.isDeadlock() && deadlockWarning.getAgentID() != agentID) {
      return false;
    } else {
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
  }
  
  /**
   * Book resource.
   *
   * @param agentID the agent id
   * @param time the time
   * @return true, if book successfully
   */
  public boolean bookResource(int agentID, long time) {
    if (deadlockWarning.isDeadlock() && deadlockWarning.getAgentID() != agentID) {
      return false;
    } else {
      for (Reservation resv : reservations) {
        // if time slot is reserved
        if (resv.getReservedTime() == time) {
          if (resv.getAgentID() != agentID) {
            // if other agent booked, then false
            return false;
          } else {
            // if this agent booked, then true
            resv.setLifeTime(Setting.PHEROMONES_LIFE_TIME);
            return true;
          }
        }
      }
      // if time slot hasn't been reserved, then true
      reservations.add(new Reservation(time, agentID,
          Setting.PHEROMONES_LIFE_TIME));
      return true;
    }
  }
  
  public void evolve() {
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
    
    deadlockWarning.evolve();
  }
  
  public void setDeadlockWarning(int agentID) {
    deadlockWarning = new DeadlockWarning(agentID, true, 3);
  }
}
