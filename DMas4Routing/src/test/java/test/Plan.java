package test;

import java.util.ArrayList;

import org.joda.time.Interval;

public class Plan {

  private Interval interval;
  private ArrayList<Long> startTime;

  public Plan(Interval interval, ArrayList<Long> startTime) {
    this.interval = interval;
    this.startTime = startTime;
  }

  public Interval getInterval() {
    return interval;
  }

  public ArrayList<Long> getStartTime() {
    return startTime;
  }

}
