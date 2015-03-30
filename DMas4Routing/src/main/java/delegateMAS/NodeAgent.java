package delegateMAS;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.joda.time.Interval;

public class NodeAgent {
  public static final int EVAPORATION_RATE = 8;
  public static final long GUARD_INTERVAL = 200; // ms
  private List<Reservation> reservations;
  
  public NodeAgent() {
    reservations = new ArrayList<Reservation>();
  }
  
  /**
   * Check availability.
   *
   * @param agentID the agent id
   * @param startTime the start time
   * @return true, if is available
   */
  public boolean checkAvailability(int agentID, long startTime) {
    Interval interval = new Interval(startTime, startTime + GUARD_INTERVAL);
    
    return !overlap(agentID, interval);
  }
  
  public boolean overlap(int agentID, Interval interval) {
    for (Reservation res : reservations) {
      if (res.overlap(agentID, interval)) {
        return true;
      }
    }
    return false;
  }
  
  /**
   * Make reservation.
   *
   * @param agentID the agent id
   * @param startTime the start time
   * @return true, if make reservation successfully
   */
  public boolean makeReservation(int agentID, long startTime) {
    Interval interval = new Interval(startTime, startTime + GUARD_INTERVAL);
    
    if (overlap(agentID, interval)) {
      return false;
    } else {
      reservations.add(new Reservation(agentID, interval));
      return true;
    }
  }
  
  public void refesh() {
    
    for (Reservation res : reservations) {
      res.refesh();
    }
    
    Iterator<Reservation> iterator = reservations.iterator();
    while (iterator.hasNext()) {
      Reservation res = iterator.next();
      if (res.getLifeTime() == 0) {
        iterator.remove();
      }
    }
  }
}
