package delegateMAS;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.Interval;

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
  public List<Interval> getExitIntervals(Interval entryInterval) {
    List<Interval> exitIntervals = new ArrayList<Interval>();
    
    for (Interval interval : intervals) {
      if (interval.toDurationMillis() >= travelTime + 2 * guardInterval) {
        final Interval avaiEntryInterval = new Interval(interval.getStartMillis()
            + guardInterval, interval.getEndMillis() - guardInterval - travelTime);
        
        final Interval overlap = avaiEntryInterval.overlap(entryInterval);
        
        if (overlap != null) {
          final long startExitInterval = overlap.getStartMillis() + travelTime;
          final Interval exitInterval = new Interval(startExitInterval, interval.getEndMillis() - guardInterval);
          exitIntervals.add(exitInterval);
        }
      }
    }
    
    return exitIntervals;
  }
}
