package test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.joda.time.Interval;

import com.github.rinde.rinsim.geom.Point;
import com.github.rinde.rinsim.util.TimeWindow;


public class Test {

  public static void main(String[] args) {
    List<Point> points = new ArrayList<Point>();
    points.add(new Point(0,0));
    points.add(new Point(0,1));
    points.add(new Point(0,2));
    points.add(new Point(0,3));
    points.add(new Point(0,4));
    points.add(new Point(0,3));
    Iterator<Point> i = points.iterator();
    
    System.out.println(points);
    while (i.hasNext()) {
      Point p = i.next();
      if (p.x == 0 && p.y == 3) {
        i.remove();
      }
    }
    System.out.println(points);
  }

}
