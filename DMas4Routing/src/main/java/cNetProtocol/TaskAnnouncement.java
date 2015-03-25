package cNetProtocol;

import com.github.rinde.rinsim.core.model.comm.MessageContents;
import com.github.rinde.rinsim.geom.Point;

public class TaskAnnouncement implements MessageContents {
  
  private Point taskLocation;
  
  public TaskAnnouncement(Point location) {
    this.taskLocation = location;
  }
  
  public Point getTaskLocation() {
    return taskLocation;
  }
}
