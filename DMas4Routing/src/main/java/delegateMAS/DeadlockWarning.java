package delegateMAS;

public class DeadlockWarning {

  private boolean isDeadlock;
  private int agentID;
  private int lifeTime;

  public DeadlockWarning(boolean isDeadlock, int agentID, int lifeTime) {
    this.isDeadlock = isDeadlock;
    this.agentID = agentID;
    this.lifeTime = lifeTime;
  }

  public boolean isDeadlock() {
    return isDeadlock;
  }

  public int getAgentID() {
    return agentID;
  }

  public int getLifeTime() {
    return lifeTime;
  }

  public void setLifeTime(int lifeTime) {
    if (lifeTime > 0) {
      this.lifeTime = lifeTime;
    } else {
      throw new IllegalArgumentException("Life time must be greater than 0!");
    }
  }
  
  public void refesh() {
    if (lifeTime > 0) {
      lifeTime--;
    } else {
      isDeadlock = false;
    }
  }
  
}
