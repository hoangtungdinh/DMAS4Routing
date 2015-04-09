package delegateMAS;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ResourceAgent {

  private List<Reservation> reservations;
  private DeadlockWarning deadlockWarning;
  private int pheromoneLifeTime;
  
  public ResourceAgent(int pheromoneLifeTime) {
    reservations = new ArrayList<Reservation>();
    deadlockWarning = new DeadlockWarning(0, false, 0);
    this.pheromoneLifeTime = pheromoneLifeTime;
  }
  
  /**
   * Checks if a time slot is available.
   *
   * @param agentID the agent id
   * @param priority the priority
   * @param time the time
   * @return true, if is available
   */
  public boolean isAvailable(int agentID, int priority, long time) {
    if (deadlockWarning.isDeadlock() && deadlockWarning.getAgentID() != agentID) {
      return false;
    } else {
      for (Reservation resv : reservations) {
        // if time slot is reserved by another agent with lower priority, then
        // false
        if (resv.getReservedTime() == time && resv.getAgentID() != agentID
            && resv.getPriority() <= priority) {
          return false;
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
   * @param priority the priority
   * @param time the time
   * @return true, if book successfully
   */
  public boolean bookResource(int agentID, int priority, long time) {
    if (deadlockWarning.isDeadlock() && deadlockWarning.getAgentID() != agentID) {
      return false;
    } else {
      Reservation backupResv = null;
      for (Reservation resv : reservations) {
        // if time slot is reserved
        if (resv.getReservedTime() == time) {
          if (resv.getAgentID() != agentID && resv.getPriority() <= priority) {
            // if other agent booked, then false
            return false;
          } else if (resv.getAgentID() == agentID) {
            backupResv = resv;
          }
        }
      }

      if (backupResv == null) {
        // if time slot hasn't been reserved
        reservations.add(new Reservation(time, agentID, priority,
            pheromoneLifeTime));
      } else {
        // if this agent booked at same priority, then true
        backupResv.updateReservation(priority, pheromoneLifeTime);
      }
      
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
