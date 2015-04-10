package delegateMAS;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ResourceAgent {

  private HashMap<Long, Reservation> reservations;
  private DeadlockWarning deadlockWarning;
  private int pheromoneLifeTime;
  
  public ResourceAgent(int pheromoneLifeTime) {
    reservations = new HashMap<>();
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
    }

    Reservation resv = reservations.get(time);

    if (resv == null) {
      // no reservation at the time slot yet
      return true;
    } else {
      if (resv.getAgentID() != agentID && resv.getPriority() <= priority) {
        return false;
      } else {
        return true;
      }
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
    }

    final Reservation resv = reservations.get(time);

    if (resv == null) {
      // no reservation at the time slot yet
      reservations.put(time, new Reservation(agentID, priority,
          pheromoneLifeTime));
      return true;
    } else {
      if (resv.getAgentID() != agentID && resv.getPriority() <= priority) {
        // if already booked by another agent with lower priority then false
        return false;
      } else {
        reservations.put(time, new Reservation(agentID, priority,
            pheromoneLifeTime));
        return true;
      }
    }
  }
  
  public void evolve() {
    Iterator<Map.Entry<Long, Reservation>> iterator = reservations.entrySet().iterator();
    
    while (iterator.hasNext()) {
      Map.Entry<Long, Reservation> entry = iterator.next();
      final Reservation resv = entry.getValue();
      resv.refesh();
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
