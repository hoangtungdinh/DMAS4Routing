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
  private int minTimeSteps;
  private int explorationFreq;
  private int intentionFreq;
  private int intentionChangingThreshold;
  @SuppressWarnings("unused")
  private int pathLength;
  private int idealLength = 0;
  private int realLength = 0;
//  private int distance = 0;
  private int failureRate = 0;

  AGV(RandomGenerator r, VirtualEnvironment virtualEnvironment, int agentID,
      int minTimeSteps, int explorationFreq, int intentionFreq,
      int intentionChangingThreshold, int pathLength, int failureRate, Point destination) {
    rng = r;
    roadModel = Optional.absent();
    this.destination = Optional.of(destination);
    path = new LinkedList<>();
    this.virtualEnvironment = virtualEnvironment;
    this.agentID = agentID;
    this.minTimeSteps = minTimeSteps;
    this.explorationFreq = explorationFreq;
    this.intentionFreq = intentionFreq;
    this.intentionChangingThreshold = intentionChangingThreshold;
    this.pathLength = pathLength;
    this.failureRate = failureRate;
  }

  @Override
  public void initRoadUser(RoadModel model) {
    roadModel = Optional.of((CollisionGraphRoadModel) model);
    Point p;
    do {
      p = model.getRandomPosition(rng);
    } while (roadModel.get().isOccupied(p));
    roadModel.get().addObjectAt(this, p);
    
    idealLength = VirtualEnvironment.getShortestPathDistance(roadModel.get(),
        getPosition(), this.destination.get()) - 1;
  }

  @Override
  public double getSpeed() {
    return 10000d;
  }

//  void nextDestination() {
//    do {
//      destination = Optional.of(roadModel.get().getRandomPosition(rng));
//    } while (destination.get().equals(getPosition()));
//
//    distance = VirtualEnvironment.getShortestPathDistance(roadModel.get(),
//        getPosition(), destination.get()) - 1;
//  }

  @Override
  public void tick(TimeLapse timeLapse) {
    long startTime = timeLapse.getStartTime();

    if (path.isEmpty()) {
      exploreAndBook(startTime);
    } else if (path.size() < minTimeSteps) {
      // if path size is smaller than time window, then explore
      exploreAndBook(startTime);
    } else {
      if (expCounter == explorationFreq) {
        final Route route = explore(startTime);
        if (pathQuality < 0) {
          if (route.getDistanceToGoal() > 0) {
            setPath(route);
            bookResource(startTime);
          } else if (route.getRoute().size() > path.size()
              && VirtualEnvironment.getHammingDistance(
                  route.getRoute().get(path.size() - 1), destination.get()) < VirtualEnvironment
                  .getHammingDistance(path.getLast(), destination.get())
                  * intentionChangingThreshold / 100) {
            setPath(route);
            bookResource(startTime);
          }
        } else if (pathQuality > 0
            && ((route.getDistanceToGoal() * 100) / pathQuality) < intentionChangingThreshold) {
          setPath(route);
          bookResource(startTime);
        }
      }
      if (intCounter == intentionFreq) {
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
      if (getPosition().equals(path.getFirst())
          || (rng.nextInt(100) + 1) > failureRate) {
        roadModel.get().moveTo(this, path.getFirst(), timeLapse);
        path.removeFirst();
      }
    } else {
      exploreAndBook(startTime);
      if (getPosition().equals(path.getFirst())
          || (rng.nextInt(100) + 1) > failureRate) {
        roadModel.get().moveTo(this, path.getFirst(), timeLapse);
        path.removeFirst();
      }
    }
    
    pathQuality--;

    if (getPosition().equals(destination.get()) && success == 0) {
      success = 1;
      realLength = (int) timeLapse.getStartTime() / 1000 + 1;
    } else if (!getPosition().equals(destination.get())) {
      success = 0;
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
    final Route exploredRoute = virtualEnvironment.explore(agentID, success,
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
    final boolean bookingResponse = virtualEnvironment.bookResource(agentID,
        success, new ArrayList<Point>(path), getPosition(), startTime);

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
  
  public int getNumberOfSuccesses() {
    return success;
  }
  
  public int getAgentID() {
    return agentID;
  }
  
  public int getIdealLength() {
    return idealLength;
  }
  
  public int getRealLength() {
    return realLength;
  }
}
