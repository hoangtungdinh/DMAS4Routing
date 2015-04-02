package delegateMAS;

import java.util.List;

import org.joda.time.Interval;

public class FreeTimeIntervals {

  private List<Interval> freeTimeIntervals;
  private long minimumTravelTime;
  private long estimatedDurationUntilDeparture;

  public FreeTimeIntervals(List<Interval> freeTimeIntervals,
      long minimumTravelTime, long estimatedDurationUntilDeparture) {
    this.freeTimeIntervals = freeTimeIntervals;
    this.minimumTravelTime = minimumTravelTime;
    this.estimatedDurationUntilDeparture = estimatedDurationUntilDeparture;
  }

  public List<Interval> getFreeTimeIntervals() {
    return freeTimeIntervals;
  }

  public long getMinimumTravelTime() {
    return minimumTravelTime;
  }

  public long getEstimatedDurationUntilDeparture() {
    return estimatedDurationUntilDeparture;
  }
}
