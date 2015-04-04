package delegateMAS;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.Interval;

import test.Plan;

public class FreeTimeIntervals {

  private List<Interval> intervals;
  private long travelTime;
  private long guardInterval;

  public FreeTimeIntervals(List<Interval> intervals,
      long travelTime, long guardInterval) {
    this.intervals = intervals;
    this.travelTime = travelTime;
    this.guardInterval = guardInterval;
  }

  public List<Interval> getIntervals() {
    return intervals;
  }

  public long getTravelTime() {
    return travelTime;
  }

  public long getGuardInterval() {
    return guardInterval;
  }
  
  /**
   * Gets the exit intervals given a start interval
   *
   * @param entryInterval the entry interval
   * @return the exit intervals
   */
  @SuppressWarnings("unchecked")
  public List<Plan> getExitPlans(Plan entryPlan) {
    List<Plan> exitPlans = new ArrayList<Plan>();
    
    for (Interval interval : intervals) {
      if (interval.toDurationMillis() >= travelTime + 2 * guardInterval) {
        final Interval avaiEntryInterval = new Interval(
            interval.getStartMillis() + guardInterval, interval.getEndMillis()
                - guardInterval - travelTime);

        final Interval overlap = avaiEntryInterval.overlap(entryPlan
            .getInterval());

        if (overlap != null) {
          final long startExitInterval = overlap.getStartMillis() + travelTime;
          final Interval exitInterval = new Interval(startExitInterval,
              interval.getEndMillis() - guardInterval);
          ArrayList<Long> startTimes = (ArrayList<Long>) entryPlan
              .getStartTime().clone();
          startTimes.add(overlap.getStartMillis());

          exitPlans.add(new Plan(exitInterval, startTimes));
        }
      }
    }

    return exitPlans;
  }
}
