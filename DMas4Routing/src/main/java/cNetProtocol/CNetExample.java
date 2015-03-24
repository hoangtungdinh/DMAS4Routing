package cNetProtocol;

import org.apache.commons.math3.random.RandomGenerator;

import com.github.rinde.rinsim.core.Simulator;
import com.github.rinde.rinsim.core.TickListener;
import com.github.rinde.rinsim.core.TimeLapse;
import com.github.rinde.rinsim.core.model.comm.CommModel;
import com.github.rinde.rinsim.core.model.pdp.DefaultPDPModel;
import com.github.rinde.rinsim.core.model.pdp.PDPModel;
import com.github.rinde.rinsim.core.model.road.MovingRoadUser;
import com.github.rinde.rinsim.core.model.road.PlaneRoadModel;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.geom.Point;
import com.github.rinde.rinsim.ui.View;
import com.github.rinde.rinsim.ui.renderers.PlaneRoadModelRenderer;
import com.github.rinde.rinsim.ui.renderers.RoadUserRenderer;

public final class CNetExample {

  static final double VEHICLE_SPEED_KMH = 50d;
  static final Point MIN_POINT = new Point(0, 0);
  static final Point MAX_POINT = new Point(10, 10);

  private CNetExample() {}

  public static void main(String[] args) {
    run(false);
  }

  public static void run(boolean testing) {

    final PlaneRoadModel planeModel = PlaneRoadModel.builder()
        .setMinPoint(MIN_POINT)
        .setMaxPoint(MAX_POINT)
        .setMaxSpeed(VEHICLE_SPEED_KMH)
        .build();
    
     final DefaultPDPModel pdpModel = DefaultPDPModel.create();
     
     final CommModel commModel = CommModel.builder().build();
  }
}
