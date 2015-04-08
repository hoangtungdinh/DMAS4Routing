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
  private int agentID;
  private int success = 0;
  private int expCounter = 0;
  private int intCounter = 0;
  private int pathQuality = -1;

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

  void nextDestination() {
    do {
      destination = Optional.of(roadModel.get().getRandomPosition(rng));
    } while (destination.get().equals(getPosition())
        || VirtualEnvironment.getHammingDistance(getPosition(),
            destination.get()) == Setting.PATH_LENGTH);
  }

  @Override
  public void tick(TimeLapse timeLapse) {
    long startTime = timeLapse.getStartTime();

    if (!destination.isPresent()) {
      nextDestination();
      exploreAndBook(startTime);
    } else if (getPosition().equals(destination.get())) {
      nextDestination();
      exploreAndBook(startTime);
      System.out.println(agentID + ": " + ++success);
    } else if (path.size() < Setting.MIN_LENGTH) {
      // if path size is smaller than time window, then explore
      exploreAndBook(startTime);
    } else {
      if (expCounter == Setting.EXP_FREQ) {
        final Route route = explore(startTime);
        if (((route.getDistanceToGoal() * 100) / pathQuality) < Setting.INT_CHANGING_THRESHOLD) {
          setPath(route);
          bookResource(startTime);
        }
      }
      if (intCounter == Setting.INT_FREQ) {
        boolean bookResponse = bookResource(startTime);
        if (!bookResponse) {
          exploreAndBook(startTime);
        }
      }
    }
    
    // agv only moves to another node when there is a connection and no other
    // agv is occupying that node
    if (getPosition().equals(path.getFirst())
        || (roadModel.get().getGraph()
            .hasConnection(getPosition(), path.getFirst()) && !roadModel.get()
            .isOccupied(path.getFirst()))) {
      roadModel.get().moveTo(this, path.getFirst(), timeLapse);
      path.removeFirst();
    } else {
      exploreAndBook(startTime);
      roadModel.get().moveTo(this, path.getFirst(), timeLapse);
      path.removeFirst();
    }

  }

  @Override
  public void afterTick(TimeLapse timeLapse) {
    expCounter++;
    intCounter++;
  }
  
  public void resetExpCounter() {
    expCounter = 0;
  }
  
  public void resetIntCounter() {
    intCounter = 0;
  }
  
  public Point getPosition() {
    return roadModel.get().getPosition(this);
  }
  
  /**
   * Explore.
   *
   * @param startTime the start time
   * @return true, if explore successfully
   */
  public Route explore(long startTime) {
    resetExpCounter();
    final Route exploredRoute = virtualEnvironment.explore(agentID,
        getPosition(), destination.get(), startTime);
    return exploredRoute;
  }
  
  /**
   * Book resource.
   *
   * @param startTime the start time
   * @return true, if book successfully
   */
  public boolean bookResource(long startTime) {
    resetIntCounter();
    final boolean bookingResponse = virtualEnvironment.bookResource(
        agentID, new ArrayList<Point>(path), getPosition(), startTime);
    
    return bookingResponse;
  }
  
  public void exploreAndBook(long startTime) {
    Route route = explore(startTime);
    setPath(route);    
    bookResource(startTime);
  }
  
  /**
   * Sets the new path and update path quality
   *
   * @param route the new path
   */
  public void setPath(Route route) {
    pathQuality = route.getDistanceToGoal();
    path = new LinkedList<>(route.getRoute());
    if (path.size() > 1) {
      path.removeFirst();
    }
  }
}
