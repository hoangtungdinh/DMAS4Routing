package delegateMAS;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.joda.time.Interval;

public class NodeAgent {
  public static final int EVAPORATION_RATE = 5;
  public static final long GUARD_INTERVAL = 500; // ms
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
      sort();
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

  public void sort() {
    Collections.sort(reservations, new ReservationStartComparator());
  }

  public FreeTimeIntervals getFreeTimeIntervals(int agentID,
      Interval searchInterval) {
    FreeIntervalFinder finder = new FreeIntervalFinder();
    List<Interval> existingIntervals = new ArrayList<Interval>();
    for (Reservation reservation : reservations) {
      if (reservation.getAgentID() != agentID) {
        existingIntervals.add(reservation.getInterval());
      }
    }

    final long travelTime = 0;

    FreeTimeIntervals freeTimeIntervals = new FreeTimeIntervals(
        finder.findGaps(existingIntervals, searchInterval), travelTime, GUARD_INTERVAL);
    return freeTimeIntervals;
  }
}
