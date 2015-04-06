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
      
      if (!path.isEmpty()) {
        bookResource(timeLapse);
      }

      if (getPosition().equals(destination.get())) {
        nextDestination(timeLapse);
      }
      
      if (path.isEmpty()) {
        explore(timeLapse);
        bookResource(timeLapse);
      }
      
      hasReached = false;
      
//      System.out.println(this.hashCode() + " " + path + " " + destination.get());
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
    final ArrayList<Point> exploredPath = virtualEnvironment.explore(
        this.hashCode(), getPosition(), destination.get(),
        timeLapse.getStartTime());

    if (exploredPath.get(exploredPath.size() - 1).equals(destination.get())) {
      // if a path is found
      path = new LinkedList<>(exploredPath);
      path.removeFirst();
    } else {
      // if can't found path to destination, just move ahead one step
      path = new LinkedList<>();
      path.add(exploredPath.get(1));
    }
  }
  
  public boolean bookResource(TimeLapse timeLapse) {
    final boolean bookingResponse = virtualEnvironment.bookResource(
        this.hashCode(), new ArrayList<Point>(path), getPosition(), timeLapse.getStartTime());
    
    return bookingResponse;
  }

}
