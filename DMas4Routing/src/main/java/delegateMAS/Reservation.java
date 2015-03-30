package delegateMAS;

import org.joda.time.Interval;

public class Reservation {
  
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
}
