package test;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.Interval;

import delegateMAS.FreeIntervalFinder;


public class Test {

  public static void main(String[] args) {
    List<Interval> timeIntervals = new ArrayList<Interval>();
    timeIntervals.add(new Interval(0, 10));
    timeIntervals.add(new Interval(20, 30));
    timeIntervals.add(new Interval(40, 50));
    timeIntervals.add(new Interval(60, 70));
    timeIntervals.add(new Interval(80, 90));
    
    Interval searchInterval = new Interval(70, 100);
    
    FreeIntervalFinder dateTimeGapFinder = new FreeIntervalFinder();
    List<Interval> holeIntervals = dateTimeGapFinder.findGaps(timeIntervals, searchInterval);
    for (Interval interval : holeIntervals) {
      System.out.println(interval.getStartMillis() + " " + interval.getEndMillis());
    }
  }
}
