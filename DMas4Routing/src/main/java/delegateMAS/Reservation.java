package delegateMAS;

public class Reservation {

  private long reservedTime;
  private int agentID;
  private int lifeTime;

  public Reservation(long reservedTime, int agentID, int lifeTime) {
    this.reservedTime = reservedTime;
    this.agentID = agentID;
    this.lifeTime = lifeTime;
  }

  public long getReservedTime() {
    return reservedTime;
  }

  public int getAgentID() {
    return agentID;
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
