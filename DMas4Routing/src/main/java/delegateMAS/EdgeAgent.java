package delegateMAS;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.joda.time.Interval;


public class EdgeAgent {

  public static final int EVAPORATION_RATE = 10;
  public static final long GUARD_INTERVAL = 2000; // ms
  private List<Reservation> reservations;
  private double edgeLength;
  
  public EdgeAgent(double edgeLength) {
    reservations = new ArrayList<Reservation>();
    this.edgeLength = edgeLength;
  }
  
  /**
   * Check availability.
   *
   * @param startTime the start time
   * @param speed the speed
   * @return -1 if not available, departure time if available
   */
  public long checkAvailability(int agentID, long startTime, double speed) {
    long stayingTime = (long) (edgeLength / speed);
    Interval interval = new Interval(startTime, startTime + stayingTime
        + GUARD_INTERVAL);
    
    if (overlap(agentID, interval)) {
      return -1;
    } else {
      return (startTime + stayingTime);
    }
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
   * @param speed the speed
   * @return the estimated departure time if succeed, -1 if fail
   */
  public long makeReservation(int agentID, long startTime, double speed) {
    long stayingTime = (long) (edgeLength / speed);
    Interval interval = new Interval(startTime, startTime + stayingTime
        + GUARD_INTERVAL);
    
    if (overlap(agentID, interval)) {
      return -1;
    } else {
      reservations.add(new Reservation(agentID, interval));
      return startTime + stayingTime;
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
