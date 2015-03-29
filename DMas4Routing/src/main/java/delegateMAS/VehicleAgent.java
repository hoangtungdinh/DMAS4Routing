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

import org.apache.commons.math3.random.RandomGenerator;

import com.github.rinde.rinsim.core.TickListener;
import com.github.rinde.rinsim.core.TimeLapse;
import com.github.rinde.rinsim.core.model.road.CollisionGraphRoadModel;
import com.github.rinde.rinsim.core.model.road.MovingRoadUser;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.geom.ConnectionData;
import com.github.rinde.rinsim.geom.Graph;
import com.github.rinde.rinsim.geom.Graphs;
import com.github.rinde.rinsim.geom.PathNotFoundException;
import com.github.rinde.rinsim.geom.Point;
import com.google.common.base.Optional;
import com.rits.cloning.Cloner;

class VehicleAgent implements TickListener, MovingRoadUser {
  private final RandomGenerator rng;
  private Optional<CollisionGraphRoadModel> roadModel;
  private Optional<Point> destination;
  private LinkedList<Point> path;
  private VirtualEnvironment virtualEnvironment;
  private Graph<? extends ConnectionData> graph;

  VehicleAgent(RandomGenerator r, VirtualEnvironment virtualEnvironment) {
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
    graph = (new Cloner()).deepClone(roadModel.get().getGraph());
  }

  @Override
  public double getSpeed() {
    return 1;
  }

  void nextDestination() {
    destination = Optional.of(roadModel.get().getRandomPosition(rng));

    Point currentPos = roadModel.get().getPosition(this);
    
    List<Route> routeList = getKRoutes(10, currentPos, destination.get());

    path = new LinkedList<>(routeList.get(0).getRoute());
    
//    virtualEnvironment.book(this.hashCode(), path);
  }

  @Override
  public void tick(TimeLapse timeLapse) {
    if (!destination.isPresent()) {
      nextDestination();
    }
    
    roadModel.get().followPath(this, path, timeLapse);
    
    if (roadModel.get().getPosition(this).equals(destination.get())) {
      nextDestination();
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
