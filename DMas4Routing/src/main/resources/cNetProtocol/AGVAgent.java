package cNetProtocol;

import java.util.List;

import com.github.rinde.rinsim.core.TimeLapse;
import com.github.rinde.rinsim.core.model.comm.CommDevice;
import com.github.rinde.rinsim.core.model.comm.CommDeviceBuilder;
import com.github.rinde.rinsim.core.model.comm.CommUser;
import com.github.rinde.rinsim.core.model.comm.Message;
import com.github.rinde.rinsim.core.model.pdp.PDPModel;
import com.github.rinde.rinsim.core.model.pdp.Vehicle;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.geom.Point;
import com.google.common.base.Optional;

public class AGVAgent extends Vehicle implements CommUser {

  private static final double SPEED = 1000d; 
  Optional<RoadModel> roadModel;
  Optional<PDPModel> pdpModel;
  Optional<CommDevice> device;
  private double range = 100.0;
  private double reliability = 1.0;
  private State state = State.IDLE;
  private Package parcel = null;

  public AGVAgent(Point startPosition) {
    setStartPosition(startPosition);
    setCapacity(3.0);
    roadModel = Optional.absent();
    pdpModel = Optional.absent();
  }

  @Override
  public double getSpeed() {
    return SPEED;
  }

  @Override
  protected void tickImpl(TimeLapse time) {
    if (state == State.IDLE) {
      if (device.get().getUnreadCount() != 0) {
        List<Message> messageList = device.get().getUnreadMessages();
        double bestDistance = 999;
        CommUser sender = null;
        for (Message message : messageList) {
          if (message.getContents() instanceof TaskAnnouncement) {
            double distance = getDistance(this.getPosition().get(), message
                .getSender().getPosition().get());

            if (distance < bestDistance) {
              bestDistance = distance;
              sender = message.getSender();
            }
          }
        }

        if (sender != null) {
          device.get().send(new TaskBid(this.getPosition().get()), sender);
          state = State.WAITING;
        }
      }
    } else if (state == State.WAITING) {
      List<Message> messageList = device.get().getUnreadMessages();
      for (Message message : messageList) {
        if (message.getContents() instanceof TaskAward) {
          TaskAward taskAward = (TaskAward) message.getContents();
          if (taskAward.getAward()) {
            state = State.WORKING;
            parcel = (Package) message.getSender();
          } else {
            state = State.IDLE;
          }
        }
      }
    } else {
      if (parcel != null) {
        final boolean inCargo = pdpModel.get().containerContains(this, parcel);

        if (!inCargo) {
          roadModel.get().moveTo(this, parcel, time);
          if (roadModel.get().equalPosition(this, parcel)) {
            pdpModel.get().pickup(this, parcel, time);
          }
        } else {
          roadModel.get().moveTo(this, parcel.getDestination(), time);
          if (roadModel.get().getPosition(this).equals(parcel.getDestination())) {
            pdpModel.get().deliver(this, parcel, time);
            device.get().getUnreadMessages();
            state = State.IDLE;
          }
        }
      }
    }
  }

  @Override
  public void initRoadPDP(RoadModel pRoadModel, PDPModel pPdpModel) {
    roadModel = Optional.of(pRoadModel);
    pdpModel = Optional.of(pPdpModel);
  }

  @Override
  public Optional<Point> getPosition() {
    return Optional.of(roadModel.get().getPosition(this));
  }

  @Override
  public void setCommDevice(CommDeviceBuilder builder) {
    if (range >= 0) {
      builder.setMaxRange(range);
    }
    device = Optional.of(builder.setReliability(reliability).build());
  }

  enum State {
    IDLE, WAITING, WORKING
  }

  public double getDistance(Point p1, Point p2) {
    return Math.sqrt((p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y)
        * (p1.y - p2.y));
  }
}
