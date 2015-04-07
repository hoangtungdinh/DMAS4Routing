package delegateMAS;

public class DeadlockWarning {

  private boolean isDeadlock;
  private int lifeTime;

  public DeadlockWarning(boolean isDeadlock, int lifeTime) {
    this.isDeadlock = isDeadlock;
    this.lifeTime = lifeTime;
  }

  public boolean isDeadlock() {
    return isDeadlock;
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
