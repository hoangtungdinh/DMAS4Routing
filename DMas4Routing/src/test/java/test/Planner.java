package test;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.Interval;

import delegateMAS.FreeTimeIntervals;


public class Planner {

  // What is the type of output
  // Input is list of possible free time interval
  // Output: list of resource entry time (equal to number of resource)
  
  public List<Plan> makeSchedule(List<FreeTimeIntervals> intervals, Interval startTime) {
    List<Plan> startList = new ArrayList<Plan>();
    ArrayList<Long> plan = new ArrayList<Long>();
    startList.add(new Plan(startTime, plan));
    
    List<Plan> exitList = new ArrayList<Plan>();
    
    for (FreeTimeIntervals freeTimeIntervals : intervals) {
      for (Plan start : startList) {
        final List<Plan> end = freeTimeIntervals.getExitPlans(start);
        exitList.addAll(end);
      }
      startList.clear();
      startList.addAll(exitList);
      exitList.clear();
    }
    
    return startList;
  }
}
