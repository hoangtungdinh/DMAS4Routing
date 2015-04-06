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
  private boolean hasReached = true;
  private boolean pathContainsGoal = false;

  AGV(RandomGenerator r, VirtualEnvironment virtualEnvironment) {
    rng = r;
    roadModel = Optional.absent();
    destination = Optional.absent();
    path = new LinkedList<>();
    this.virtualEnvironment = virtualEnvironment;
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
    return 1000;
  }

  void nextDestination(TimeLapse timeLapse) {
    destination = Optional.of(roadModel.get().getRandomPosition(rng));
    
    explore(timeLapse);
    
    final boolean bookingResponse = bookResource(timeLapse);
    
    if (!bookingResponse) {
      System.out.println("DO SOMETHING");
    }
  }

  @Override
  public void tick(TimeLapse timeLapse) {
    if ((timeLapse.getStartTime() % 1000) == 0) {
      if (!destination.isPresent()) {
        nextDestination(timeLapse);
      }

      if (getPosition().equals(destination.get())) {
        nextDestination(timeLapse);
      }
      System.out.println(this.hashCode() + " " + path + " " + destination.get());
      if ((timeLapse.getStartTime() % Setting.TIME_WINDOW) == 0) {
        explore(timeLapse);
        bookResource(timeLapse);
      } else {
        if (!path.isEmpty()) {
          if (pathContainsGoal) {
            boolean bookResponse = bookResource(timeLapse);
            if (!bookResponse) {
              explore(timeLapse);
              bookResource(timeLapse);
            }
          } else {
            explore(timeLapse);
            bookResource(timeLapse);
          }
        } else {
          explore(timeLapse);
          bookResource(timeLapse);
        }
      }
      
      hasReached = false;
      roadModel.get().moveTo(this, path.getFirst(), timeLapse);

      if (getPosition().equals(path.getFirst())) {
        path.removeFirst();
        hasReached = true;
      }
      
    } else {
      // this part is to solve the problem with priority in CollisionGraphRoadModel
      if (!path.isEmpty()) {
        if (!hasReached) {
          roadModel.get().moveTo(this, path.getFirst(), timeLapse);
          if (getPosition().equals(path.getFirst())) {
            path.removeFirst();
            hasReached = true;
          }
        }
      }
    }

  }

  @Override
  public void afterTick(TimeLapse timeLapse) {}
  
  public Point getPosition() {
    return roadModel.get().getPosition(this);
  }
  
  public void explore(TimeLapse timeLapse) {
    final Route exploredRoute = virtualEnvironment.explore(
        this.hashCode(), getPosition(), destination.get(),
        timeLapse.getStartTime());

    pathContainsGoal = exploredRoute.containsDestination();
    path = new LinkedList<>(exploredRoute.getRoute());
    if (path.size() > 1) {
      path.removeFirst();
    }
  }
  
  /**
   * Book resource.
   *
   * @param timeLapse the time lapse
   * @return true, if book successfully
   */
  public boolean bookResource(TimeLapse timeLapse) {
    final boolean bookingResponse = virtualEnvironment.bookResource(
        this.hashCode(), new ArrayList<Point>(path), getPosition(), timeLapse.getStartTime());
    
    return bookingResponse;
  }

}
