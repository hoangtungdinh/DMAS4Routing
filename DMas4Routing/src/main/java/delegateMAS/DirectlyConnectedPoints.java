package delegateMAS;

import com.github.rinde.rinsim.geom.Point;

public class DirectlyConnectedPoints {
  
  private Point p1;
  private Point p2;
  
  public DirectlyConnectedPoints(Point p1, Point p2) {
    this.p1 = p1;
    this.p2 = p2;
  }
  
  public Point getPoint1() {
    return p1;
  }
  
  public Point getPoint2() {
    return p2;
  }
}
