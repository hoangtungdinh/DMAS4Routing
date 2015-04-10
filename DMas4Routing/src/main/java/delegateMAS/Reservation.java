package delegateMAS;

public class Reservation {

  private int agentID;
  private int priority;
  private int lifeTime;

  public Reservation(int agentID, int priority, int lifeTime) {
    this.agentID = agentID;
    this.priority = priority;
    this.lifeTime = lifeTime;
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
