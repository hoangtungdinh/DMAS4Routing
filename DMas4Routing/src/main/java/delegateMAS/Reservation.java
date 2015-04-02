package delegateMAS;

import org.joda.time.Interval;

public class Reservation implements Comparable<Reservation> {
  
  private int agentID;
  private Interval interval;
  private int lifeTime;
  
  public Reservation(int agentID, Interval interval) {
    this.agentID = agentID;
    this.interval = interval;
    this.lifeTime = EdgeAgent.EVAPORATION_RATE;
  }
  
  public int getAgentID() {
    return agentID;
  }
  
  public Interval getInterval() {
    return interval;
  }
  
  public boolean overlap(int agtID, Interval intv) {
    return interval.overlaps(intv) && agtID != agentID;
  }
  
  public int getLifeTime() {
    return lifeTime;
  }
  
  public void refesh() {
    if (lifeTime > 0) {
      lifeTime--;
    }
  }

  @Override
  public int compareTo(Reservation o) {
    long comp = interval.getStartMillis() - o.getInterval().getStartMillis();
    if (comp > 0) {
      return 1;
    } else if (comp < 0) {
      return -1;
    } else {
      return 0;
    }
  }

}
