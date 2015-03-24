package cNetProtocol;

import com.github.rinde.rinsim.core.TimeLapse;
import com.github.rinde.rinsim.core.model.comm.CommDevice;
import com.github.rinde.rinsim.core.model.comm.CommDeviceBuilder;
import com.github.rinde.rinsim.core.model.comm.CommUser;
import com.github.rinde.rinsim.core.model.pdp.PDPModel;
import com.github.rinde.rinsim.core.model.pdp.Vehicle;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.geom.Point;
import com.google.common.base.Optional;

public class AGVAgent extends Vehicle implements CommUser {
  
  private static final double SPEED = 1000d;
  private Optional<RoadModel> roadModel;
  private Optional<PDPModel> pdpModel;
  Optional<CommDevice> device;
  private double range = 2.0;
  private double reliability = 1.0;

  @Override
  public double getSpeed() {
    return SPEED;
  }

  @Override
  protected void tickImpl(TimeLapse time) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void initRoadPDP(RoadModel pRoadModel, PDPModel pPdpModel) {
    roadModel = Optional.of(pRoadModel);
    pdpModel = Optional.of(pPdpModel);
  }

  @Override
  public Point getPosition() {
    return roadModel.get().getPosition(this);
  }

  @Override
  public void setCommDevice(CommDeviceBuilder builder) {
    if (range >= 0) {
      builder.setMaxRange(range);
    }
    device = Optional.of(builder
        .setReliability(reliability)
        .build());
  }

}
