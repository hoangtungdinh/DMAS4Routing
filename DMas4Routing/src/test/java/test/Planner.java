package test;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.Interval;

import delegateMAS.FreeTimeIntervals;


public class Planner {

  // What is the type of output
  // Input is list of possible free time interval
  // Output: list of resource entry time (equal to number of resource)
  
  public List<Interval> makeSchedule(List<FreeTimeIntervals> intervals, Interval startTime) {
    List<Interval> startList = new ArrayList<Interval>();
    startList.add(startTime);
    
    List<Interval> exitList = new ArrayList<Interval>();
    
    for (FreeTimeIntervals freeTimeIntervals : intervals) {
      for (Interval start : startList) {
        final List<Interval> end = freeTimeIntervals.getExitIntervals(start);
        exitList.addAll(end);
      }
      startList.clear();
      startList.addAll(exitList);
      exitList.clear();
    }
    
    return startList;
  }
}
