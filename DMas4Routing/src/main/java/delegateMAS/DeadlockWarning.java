package delegateMAS;

public class DeadlockWarning {

  private boolean isDeadlock;
  private int lifeTime;
  private int agentID;

  public DeadlockWarning(int agentID, boolean isDeadlock, int lifeTime) {
    this.isDeadlock = isDeadlock;
    this.lifeTime = lifeTime;
    this.agentID = agentID;
  }

  public boolean isDeadlock() {
    return isDeadlock;
  }

  public int getLifeTime() {
    return lifeTime;
  }
  
  public int getAgentID() {
    return agentID;
  }

  public void setLifeTime(int lifeTime) {
    if (lifeTime > 0) {
      this.lifeTime = lifeTime;
    } else {
      throw new IllegalArgumentException("Life time must be greater than 0!");
    }
  }
  
  public void evolve() {
    if (lifeTime > 0) {
      lifeTime--;
    } else {
      isDeadlock = false;
    }
  }
  
}