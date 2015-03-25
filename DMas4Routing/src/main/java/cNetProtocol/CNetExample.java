package cNetProtocol;

import org.apache.commons.math3.random.RandomGenerator;

import com.github.rinde.rinsim.core.Simulator;
import com.github.rinde.rinsim.core.TickListener;
import com.github.rinde.rinsim.core.TimeLapse;
import com.github.rinde.rinsim.core.model.comm.CommModel;
import com.github.rinde.rinsim.core.model.pdp.DefaultPDPModel;
import com.github.rinde.rinsim.core.model.road.PlaneRoadModel;
import com.github.rinde.rinsim.geom.Point;
import com.github.rinde.rinsim.ui.View;
import com.github.rinde.rinsim.ui.renderers.PDPModelRenderer;
import com.github.rinde.rinsim.ui.renderers.PlaneRoadModelRenderer;
import com.github.rinde.rinsim.ui.renderers.RoadUserRenderer;

public final class CNetExample {

  static final double VEHICLE_SPEED_KMH = 50d;
  static final Point MIN_POINT = new Point(0, 0);
  static final Point MAX_POINT = new Point(10, 10);
  static final int NUM_AGV = 5;
  static final int NUM_PARCEL = 10;

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
     
     final Simulator simulator = Simulator.builder()
         .addModel(planeModel)
         .addModel(pdpModel)
         .addModel(commModel)
         .build();
     
     final RandomGenerator rng = simulator.getRandomGenerator();
     
     for (int i = 0; i < NUM_AGV; i++) {
       simulator.register(new AGVAgent(planeModel.getRandomPosition(rng)));
     }
    for (int i = 0; i < NUM_PARCEL; i++) {
      simulator.register(new Package(planeModel.getRandomPosition(rng),
          planeModel.getRandomPosition(rng), 1, 1, 1.0));
    }
    
    simulator.addTickListener(new TickListener() {
      @Override
      public void tick(TimeLapse time) {
        if (rng.nextDouble() < .007) {
          simulator.register(new Package(planeModel.getRandomPosition(rng),
              planeModel.getRandomPosition(rng), 1, 1, 1.0));
        }
      }

      @Override
      public void afterTick(TimeLapse timeLapse) {
      }
    });
    
    final View.Builder viewBuilder = View.create(simulator)
        .with(PlaneRoadModelRenderer.create())
        .with(RoadUserRenderer.builder())
        .with(new PDPModelRenderer());
    
    
    viewBuilder.show();
  }
}
