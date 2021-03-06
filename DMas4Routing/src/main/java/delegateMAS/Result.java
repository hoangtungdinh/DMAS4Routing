package delegateMAS;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import com.github.rinde.rinsim.core.Simulator;
import com.github.rinde.rinsim.core.TickListener;
import com.github.rinde.rinsim.core.TimeLapse;
import com.github.rinde.rinsim.core.model.road.CollisionGraphRoadModel;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.road.RoadUser;

public class Result implements TickListener {
  
  private RoadModel roadModel;
  private Setting setting;
  private Simulator simulator;
  private String fileID;
  
  public Result(CollisionGraphRoadModel roadModel, Simulator simulator,
      Setting setting, String fileID) {
    this.roadModel = roadModel;
    this.simulator = simulator;
    this.setting = setting;
    this.fileID = fileID;
  }

  @Override
  public void tick(TimeLapse timeLapse) {}

  @Override
  public void afterTick(TimeLapse timeLapse) {
    if (timeLapse.getTime() >= setting.getStopTime()) {
      print();
      simulator.stop();
      System.out.println("COMPLETE!!!");
      simulator = null;
    }
  }
  
  public void print() {
    String date = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss")
        .format(new Date());
    String fileName = date + fileID;
    try {
      PrintWriter printWriter = new PrintWriter(new FileOutputStream(fileName,
          true));
      printWriter.println(setting);
      Set<RoadUser> roadUserSet = roadModel.getObjects();
      List<RoadUser> roadUserList = new ArrayList<RoadUser>(roadUserSet);
      printWriter.println("AgentID" + "\t" + "Successes" + "\t" + "Real Length"
          + "\t" + "Ideal Length" + "\t" + "Ratio");
      int best = Integer.MIN_VALUE;
      int worst = Integer.MAX_VALUE;
      int total = 0;
      int totalRealLength = 0;
      int totalIdealLength = 0;
      for (RoadUser roadUser : roadUserList) {
        AGV agv = (AGV) roadUser;
        final int success = agv.getNumberOfSuccesses();
        if (best < success) {
          best = success;
        }
        if (worst > success) {
          worst = success;
        }
        total += success;
        printWriter.println(agv.getAgentID()
            + "\t"
            + success
            + "\t"
            + agv.getRealLength()
            + "\t"
            + agv.getIdealLength()
            + "\t"
            + (new DecimalFormat("##.00").format(((double) agv.getRealLength())
                / agv.getIdealLength())));
        totalIdealLength += agv.getIdealLength();
        totalRealLength += agv.getRealLength();
      }
      printWriter.println("Best: " + best);
      printWriter.println("Worst: " + worst);
      printWriter.println("Total: " + total);
      printWriter.println("Average successes: "
          + (((double) total) / roadUserList.size()));
      printWriter.println("Average real length: "
          + (new DecimalFormat("##.00").format(((double) totalRealLength)
              / roadUserList.size())));
      printWriter.println("Average ideal length: "
          + (new DecimalFormat("##.00").format(((double) totalIdealLength)
              / roadUserList.size())));
      printWriter.println("Average Ratio: "
          + (new DecimalFormat("##.00").format(((double) totalRealLength)
              / totalIdealLength)));

      printWriter.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }
}
