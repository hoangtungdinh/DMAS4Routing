package delegateMAS;

import java.util.ArrayList;
import java.util.LinkedList;

import org.apache.commons.math3.random.RandomGenerator;

import com.github.rinde.rinsim.core.TickListener;
import com.github.rinde.rinsim.core.TimeLapse;
import com.github.rinde.rinsim.core.model.road.CollisionGraphRoadModel;
import com.github.rinde.rinsim.core.model.road.MovingRoadUser;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.geom.Point;
import com.google.common.base.Optional;

class AGV implements TickListener, MovingRoadUser {
  private final RandomGenerator rng;
  private Optional<CollisionGraphRoadModel> roadModel;
  private Optional<Point> destination;
  private LinkedList<Point> path;
  private VirtualEnvironment virtualEnvironment;
  private boolean pathContainsGoal = false;
  private int agentID;
  private int success = 0;

  AGV(RandomGenerator r, VirtualEnvironment virtualEnvironment, int agentID) {
    rng = r;
    roadModel = Optional.absent();
    destination = Optional.absent();
    path = new LinkedList<>();
    this.virtualEnvironment = virtualEnvironment;
    this.agentID = agentID;
  }

  @Override
  public void initRoadUser(RoadModel model) {
    roadModel = Optional.of((CollisionGraphRoadModel) model);
    Point p;
    do {
      p = model.getRandomPosition(rng);
    } while (roadModel.get().isOccupied(p));
    roadModel.get().addObjectAt(this, p);
  }

  @Override
  public double getSpeed() {
    return 10000d;
  }

  void nextDestination(long startTime) {
    do {
      destination = Optional.of(roadModel.get().getRandomPosition(rng));
    } while (destination.get().equals(getPosition()));
  }

  @Override
  public void tick(TimeLapse timeLapse) {
    long startTime = timeLapse.getStartTime();

    if (!destination.isPresent()) {
      nextDestination(startTime);
      explore(startTime);
      bookResource(startTime);
    }

    if (getPosition().equals(destination.get())) {
      nextDestination(startTime);
      explore(startTime);
      bookResource(startTime);
      System.out.println(agentID + ": " + ++success);
    }

    if ((timeLapse.getStartTime() % Setting.TIME_WINDOW) == 0) {
      explore(startTime);
      bookResource(startTime);
    } else {
      if (!path.isEmpty()) {
        if (pathContainsGoal) {
          boolean bookResponse = bookResource(startTime);
          if (!bookResponse) {
            explore(startTime);
            bookResource(startTime);
          }
        } else {
          explore(startTime);
          bookResource(startTime);
        }
      } else {
        explore(startTime);
        bookResource(startTime);
      }
    }

    roadModel.get().moveTo(this, path.getFirst(), timeLapse);
    path.removeFirst();

  }

  @Override
  public void afterTick(TimeLapse timeLapse) {}
  
  public Point getPosition() {
    return roadModel.get().getPosition(this);
  }
  
  /**
   * Explore.
   *
   * @param startTime the start time
   */
  public void explore(long startTime) {
    final Route exploredRoute = virtualEnvironment.explore(agentID,
        getPosition(), destination.get(), startTime);

    pathContainsGoal = exploredRoute.containsDestination();
    path = new LinkedList<>(exploredRoute.getRoute());
    if (path.size() > 1) {
      path.removeFirst();
    }
  }
  
  /**
   * Book resource.
   *
   * @param startTime the start time
   * @return true, if book successfully
   */
  public boolean bookResource(long startTime) {
    final boolean bookingResponse = virtualEnvironment.bookResource(
        agentID, new ArrayList<Point>(path), getPosition(), startTime);
    
    return bookingResponse;
  }
}
