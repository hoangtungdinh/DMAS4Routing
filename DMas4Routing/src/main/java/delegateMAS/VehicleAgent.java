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

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.math3.random.RandomGenerator;

import com.github.rinde.rinsim.core.TickListener;
import com.github.rinde.rinsim.core.TimeLapse;
import com.github.rinde.rinsim.core.model.road.CollisionGraphRoadModel;
import com.github.rinde.rinsim.core.model.road.MovingRoadUser;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.geom.Point;
import com.google.common.base.Optional;

class VehicleAgent implements TickListener, MovingRoadUser {
  private final RandomGenerator rng;
  private Optional<CollisionGraphRoadModel> roadModel;
  private Optional<Point> destination;
  private LinkedList<Point> path;
  private VirtualEnvironment virtualEnvironment;

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

  }

  @Override
  public double getSpeed() {
    return 4.0;
  }

  void nextDestination() {
    destination = Optional.of(roadModel.get().getRandomPosition(rng));
    
//    destination = Optional.of(new Point(36.0, 36.0));
    
    List<Route> routes = virtualEnvironment.explore(roadModel.get()
        .getPosition(this), destination.get(), 160);
    
    Route bestRoute = null;
    int length = 999;
    for (Route route : routes) {
      if (route.getRoute().size() < length) {
        bestRoute = route;
        length = route.getRoute().size();
      }
    }

    path = new LinkedList<>(bestRoute.getRoute());
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

}
