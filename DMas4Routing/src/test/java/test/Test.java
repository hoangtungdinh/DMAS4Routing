package test;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.Interval;

import delegateMAS.FreeTimeIntervals;


public class Test {

  public static void main(String[] args) {
    Planner planner = new Planner();
    
    Interval i1 = new Interval(10, 190);
    Interval i2 = new Interval(100, 290);
    Interval i3 = new Interval(200, 390);
    Interval i4 = new Interval(300, 490);
    
    List<Interval> intervals1 = new ArrayList<Interval>();
    intervals1.add(i1);
    List<Interval> intervals2 = new ArrayList<Interval>();
    intervals2.add(i2);
    List<Interval> intervals3 = new ArrayList<Interval>();
    intervals3.add(i3);
    List<Interval> intervals4 = new ArrayList<Interval>();
    intervals4.add(i4);
    
    FreeTimeIntervals f1 = new FreeTimeIntervals(intervals1, 50, 10);
    FreeTimeIntervals f2 = new FreeTimeIntervals(intervals2, 50, 10);
    FreeTimeIntervals f3 = new FreeTimeIntervals(intervals3, 50, 10);
    FreeTimeIntervals f4 = new FreeTimeIntervals(intervals4, 50, 10);
    
    List<FreeTimeIntervals> freeTimeIntervals = new ArrayList<FreeTimeIntervals>();
    freeTimeIntervals.add(f1);
    freeTimeIntervals.add(f2);
    freeTimeIntervals.add(f3);
    freeTimeIntervals.add(f4);
    
    List<Interval> result = planner.makeSchedule(freeTimeIntervals, new Interval(10, 50));
    
    for (Interval i : result) {
      System.out.println(i.getStartMillis() + "  " + i.getEndMillis());
    }
    
    
//    Interval i1 = new Interval(10, 100);
//    Interval i2 = new Interval(110, 190);
//    Interval i3 = new Interval(210, 290);
//    Interval i4 = new Interval(310, 390);
//
//    List<Interval> intervals1 = new ArrayList<Interval>();
//    intervals1.add(i1);
//    intervals1.add(i2);
//    intervals1.add(i3);
//    intervals1.add(i4);
//    FreeTimeIntervals f1 = new FreeTimeIntervals(intervals1, 50, 10);
//    
//    Interval startInterval = new Interval(10, 200);
//    List<Interval> exitIntervals = f1.getExitIntervals(startInterval);
//    
//    for (Interval i : exitIntervals) {
//      System.out.println(i.getStartMillis() + " " + i.getEndMillis());
//    }
  }
}
