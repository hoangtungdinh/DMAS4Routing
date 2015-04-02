package delegateMAS;

/*
 * Copyright (C) 2011-2015 Rinde van Lon, iMinds-DistriNet, KU Leuven
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;

import com.github.rinde.rinsim.core.TickListener;
import com.github.rinde.rinsim.core.TimeLapse;
import com.github.rinde.rinsim.core.model.road.CollisionGraphRoadModel;
import com.github.rinde.rinsim.core.model.road.DeadlockException;
import com.github.rinde.rinsim.core.model.road.MovingRoadUser;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.geom.Connection;
import com.github.rinde.rinsim.geom.ConnectionData;
import com.github.rinde.rinsim.geom.Graph;
import com.github.rinde.rinsim.geom.Graphs;
import com.github.rinde.rinsim.geom.LengthData;
import com.github.rinde.rinsim.geom.Point;
import com.google.common.base.Optional;

class VehicleAgent implements TickListener, MovingRoadUser {
  private final RandomGenerator rng;
  private Optional<CollisionGraphRoadModel> roadModel;
  private Optional<Point> destination;
  private LinkedList<Point> path;
  private VirtualEnvironment virtualEnvironment;
  private Graph<LengthData> graph;
  private int count = 0;
  private boolean deadlock = false;
  private boolean stop = false;
  public final static double SPEED = 3.6d*4;
  public final static double LENGTH = 2d;
  public final static double MIN_DISTANCE = 0.25;

  VehicleAgent(RandomGenerator r, VirtualEnvironment virtualEnvironment,
      Graph<LengthData> graph) {
    rng = r;
    roadModel = Optional.absent();
    destination = Optional.absent();
    path = new LinkedList<>();
    this.virtualEnvironment = virtualEnvironment;
    this.graph = graph;
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
    // speed in km/h, unit of map is meter
    return SPEED;
//    return (2);
  }

  void nextDestination() {
    destination = Optional.of(roadModel.get().getRandomPosition(rng));
    
    Route bestRoute = explore(roadModel.get().getPosition(this));
    makeReservation(bestRoute, roadModel.get().getPosition(this));
  }
  
  public Route explore(Point start) {
    List<Route> routeList = getKRoutes(10, start, destination.get());
    
    Route bestRoute = null;
    double speedMs = this.getSpeed() / 3600;
    ExplorationInfo explorationInfo = null;
    double bestDistance = Point.distance(start, destination.get());
    
    for (Route route : routeList) {
      explorationInfo = virtualEnvironment.explore(this.hashCode(), route.getRoute(), speedMs);
      if (explorationInfo.getDistance() <= bestDistance) {
        // TODO if distances are equal, check time
        bestRoute = new Route(explorationInfo.getPath());
        bestDistance = explorationInfo.getDistance();
      }
    }
    
    return bestRoute;
  }
  
  public void makeReservation(Route bestRoute, Point start) {
    double speedMs = this.getSpeed() / 3600;
    
    if (bestRoute == null) {
      List<Point> tmpPath = new ArrayList<Point>();
      tmpPath.add(start);
      virtualEnvironment.makeReservation(this.hashCode(), tmpPath, speedMs);
      path = new LinkedList<>(tmpPath);
      stop = true;
    } else {
      if (virtualEnvironment.makeReservation(this.hashCode(),
          bestRoute.getRoute(), speedMs)) {
        path = new LinkedList<>(bestRoute.getRoute());
      } else {
        List<Point> tmpPath = new ArrayList<Point>();
        tmpPath.add(start);
        path = new LinkedList<>(tmpPath);
      }
    }
  }

  @Override
  public void tick(TimeLapse timeLapse) {
    if (!destination.isPresent()) {
      nextDestination();
    }

    Point currentPosition = roadModel.get().getPosition(this);
    if ((currentPosition.x % 4) == 0 && (currentPosition.y % 4) == 0) {
      if (!deadlock) {
        Route bestRoute = explore(currentPosition);
        makeReservation(bestRoute, roadModel.get().getPosition(this));
      }
    } 
//    else {
//      if (deadlock) {
//        List<Point> tmpPath = new ArrayList<Point>();
//        tmpPath.add(roadModel.get().getPosition(this));
//        path = new LinkedList<>(tmpPath);
//      }
//    }
    
//    virtualEnvironment.printCurrentTime();
//    
//    System.out
//    .println("1: " + count + " " + this.hashCode() + " -- "
//        + roadModel.get().getPosition(this) + " -- "
//        + this.destination.get() + " -- " + path);
//    System.out.println(stop);
    
    if (!stop && !path.isEmpty()) {
      try {
        roadModel.get().followPath(this, path, timeLapse);
        deadlock = false;
      } catch (DeadlockException e) {
        Point pos = roadModel.get().getPosition(this);
        if ((pos.x % 4) == 0 && (pos.y % 4) == 0) {
//          Collection<Point> outgoingNodes = roadModel.get().getGraph()
//              .getOutgoingConnections(pos);
          
//          for (Point p : outgoingNodes) {
//            System.out.println(roadModel.get().hasRoadUserOn(pos, p));
//          }
          
          Point p1 = pos;
          Point p2 = path.get(0);
          graph.removeConnection(p1, p2);
          graph.removeConnection(p2, p1);
          Route bestRoute = explore(pos);
          makeReservation(bestRoute, pos);
          graph.addConnection(p1, p2);
          graph.addConnection(p2, p1);
          deadlock = true;
        } else {
          deadlock = true;
        }
      }
    } else {
      stop = false;
    }
    
//    System.out
//    .println("2: " + count + " " + this.hashCode() + " -- "
//        + roadModel.get().getPosition(this) + " -- "
//        + this.destination.get() + " -- " + path);
    
    if (roadModel.get().getPosition(this).equals(destination.get())) {
      nextDestination();
      count++;
    }
  }

  @Override
  public void afterTick(TimeLapse timeLapse) {}
  
  /**
   * Gets maximum k routes from start to goal
   *
   * @param k the k
   * @param start the start
   * @param goal the goal
   * @return the k routes
   */
  public List<Route> getKRoutes(int k, Point start, Point goal) {
    List<Route> routeList = new ArrayList<Route>();
    Random random = new Random();
    
    List<DirectlyConnectedPoints> tmpPoints = new ArrayList<>();
    
    // find k routes by remove one random connection between founded route and execute A* again
    for (int i = 0; i < k; i++) {
      try {
        List<Point> route = Graphs.shortestPathEuclideanDistance(graph, start, goal);
        routeList.add(new Route(route));
        int index = random.nextInt(route.size() - 1);
        Point p1 = route.get(index);
        Point p2 = route.get(index + 1);
        tmpPoints.add(new DirectlyConnectedPoints(p1, p2));
        graph.removeConnection(p1, p2);
        graph.removeConnection(p2, p1);
      } catch (Exception e) {
        break;
      }
    }
    
    for (DirectlyConnectedPoints points : tmpPoints) {
      graph.addConnection(points.getPoint1(), points.getPoint2());
      graph.addConnection(points.getPoint2(), points.getPoint1());
    }

    return routeList;
  }

}
