package delegateMAS;

import com.github.rinde.rinsim.geom.Point;

public class Reservation {
  
  private Point nextNode;
  private int duration;
  private int agentID;
  
  public Reservation(int agentID, Point nextNode, int duration) {
    this.nextNode = nextNode;
    this.duration = duration;
    this.agentID = agentID;
  }
  
  public Point getNextNode() {
    return nextNode;
  }
  
  public void refresh() {
    if (duration > 0) {
      duration--;
    }
  }
  
  public int getDuration() {
    return duration;
  }
  
  public int getAgentID() {
    return agentID;
  }
}
