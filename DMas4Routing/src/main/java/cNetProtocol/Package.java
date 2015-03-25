package cNetProtocol;

import com.github.rinde.rinsim.core.TickListener;
import com.github.rinde.rinsim.core.TimeLapse;
import com.github.rinde.rinsim.core.model.comm.CommDevice;
import com.github.rinde.rinsim.core.model.comm.CommDeviceBuilder;
import com.github.rinde.rinsim.core.model.comm.CommUser;
import com.github.rinde.rinsim.core.model.comm.Message;
import com.github.rinde.rinsim.core.model.pdp.PDPModel;
import com.github.rinde.rinsim.core.model.pdp.Parcel;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.geom.Point;
import com.github.rinde.rinsim.util.TimeWindow;
import com.google.common.base.Optional;

import java.util.ArrayList;
import java.util.List;

public class Package extends Parcel implements TickListener, CommUser {

  private boolean hasContractor = false;
  private Point startPosition;
  private double range = 100.0;
  private double reliability = 1.0;
  Optional<CommDevice> device;
  private State state = State.BROADCASTING;
  private int counter = 0;
  
  public Package(Point startPosition, Point pDestination,
      long pLoadingDuration, long pUnloadingDuration, double pMagnitude) {
    super(pDestination, pLoadingDuration, TimeWindow.ALWAYS,
        pUnloadingDuration, TimeWindow.ALWAYS, pMagnitude);
    setStartPosition(startPosition);
    this.startPosition = startPosition;
  }

  @Override
  public void initRoadPDP(RoadModel pRoadModel, PDPModel pPdpModel) {}

  @Override
  public void tick(TimeLapse timeLapse) {
    if (!hasContractor) {
      if (state == State.BROADCASTING) {
        device.get().broadcast(new TaskAnnouncement(startPosition));
        state = State.WAITING_BIDS;
      } else {
        counter++;
        List<Message> msgs = device.get().getUnreadMessages();
        double bestDistance = 999;
        CommUser winner = null;
        List<CommUser> loosers = new ArrayList<CommUser>();

        for (Message message : msgs) {
          if (message.getContents() instanceof TaskBid) {
            double distance = getDistance(message.getSender().getPosition(),
                this.getPosition());

            if (distance < bestDistance) {
              if (winner != null) {
                loosers.add(winner);
              }
              winner = message.getSender();
              bestDistance = distance;
            } else {
              loosers.add(message.getSender());
            }
          }
        }
        
        if (winner != null) {
          device.get().send(new TaskAward(true), winner);
          for (CommUser commUser : loosers) {
            device.get().send(new TaskAward(false), commUser);
          }
          hasContractor = true;
        } else {
          if (counter == 3) {
            state = State.BROADCASTING;
            counter = 0;
          }
        }
      }
    }
  }

  @Override
  public void afterTick(TimeLapse timeLapse) {}

  @Override
  public Point getPosition() {
    return startPosition;
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
  
  public double getDistance(Point p1, Point p2) {
    return Math.sqrt((p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y)
        * (p1.y - p2.y));
  }

  enum State {
    BROADCASTING, WAITING_BIDS
  }
}
