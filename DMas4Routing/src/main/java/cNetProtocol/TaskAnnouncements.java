package cNetProtocol;

import com.github.rinde.rinsim.core.model.comm.MessageContents;
import com.github.rinde.rinsim.geom.Point;

public class TaskAnnouncements implements MessageContents {
  
  private Point taskLocation;
  
  public TaskAnnouncements(Point location) {
    this.taskLocation = location;
  }
  
  public Point getTaskLocation() {
    return taskLocation;
  }
}
