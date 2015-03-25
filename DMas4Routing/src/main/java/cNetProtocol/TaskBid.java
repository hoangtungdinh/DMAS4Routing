package cNetProtocol;

import com.github.rinde.rinsim.core.model.comm.MessageContents;
import com.github.rinde.rinsim.geom.Point;

public class TaskBid implements MessageContents {
  
  private Point agvLocation;
  
  public TaskBid(Point agvLocation) {
    this.agvLocation = agvLocation;
  }
  
  public Point getAGVLocation() {
    return agvLocation;
  }
}
