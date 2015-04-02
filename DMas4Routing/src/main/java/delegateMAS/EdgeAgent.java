package delegateMAS;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.joda.time.Interval;


public class EdgeAgent {

  public static final int EVAPORATION_RATE = 5;
  public static final long GUARD_INTERVAL = 200; // ms
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
      sort();
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
  
  public void sort() {
    Collections.sort(reservations, new ReservationStartComparator());
  }
  
  /**
   * Gets the free time intervals from an edge agent
   * Not take into account the reservation booked by the current vehicle agent
   *
   * @param agentID the agent id
   * @param searchInterval the search interval
   * @return the free time intervals
   */
  public FreeTimeIntervals getFreeTimeIntervals(int agentID,
      Interval searchInterval) {
    FreeIntervalFinder finder = new FreeIntervalFinder();
    List<Interval> existingIntervals = new ArrayList<Interval>();
    for (Reservation reservation : reservations) {
      if (reservation.getAgentID() != agentID) {
        existingIntervals.add(reservation.getInterval());
      }
    }

    long minimumTravelTime = (long) ((edgeLength + VehicleAgent.LENGTH + VehicleAgent.MIN_DISTANCE * 2) * 3600 / VehicleAgent.SPEED);
    minimumTravelTime += GUARD_INTERVAL;

    long estimatedStartingDepartureTime = (long) (edgeLength * 3600 / VehicleAgent.SPEED);

    FreeTimeIntervals freeTimeIntervals = new FreeTimeIntervals(
        finder.findGaps(existingIntervals, searchInterval), minimumTravelTime,
        estimatedStartingDepartureTime);
    return freeTimeIntervals;
  }
}
