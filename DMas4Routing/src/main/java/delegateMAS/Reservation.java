package delegateMAS;

public class Reservation {

  private long reservedTime;
  private int agentID;
  private int priority;
  private int lifeTime;

  public Reservation(long reservedTime, int agentID, int priority, int lifeTime) {
    this.reservedTime = reservedTime;
    this.agentID = agentID;
    this.priority = priority;
    this.lifeTime = lifeTime;
  }

  public long getReservedTime() {
    return reservedTime;
  }

  public int getAgentID() {
    return agentID;
  }
  
  public int getPriority() {
    return priority;
  }
  
  public int getLifeTime() {
    return lifeTime;
  }
  
  public void updateReservation(int priority, int lifeTime) {
    if (lifeTime > 0) {
      this.lifeTime = lifeTime;
      this.priority = priority;
    } else {
      throw new IllegalArgumentException("Life time must be greater than 0!");
    }
  }
  
  public void refesh() {
    if (lifeTime > 0) {
      lifeTime--;
    }
  }
}
