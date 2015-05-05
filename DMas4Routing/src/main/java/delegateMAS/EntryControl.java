package delegateMAS;

import java.util.Iterator;
import java.util.List;

import com.github.rinde.rinsim.core.Simulator;
import com.github.rinde.rinsim.core.TickListener;
import com.github.rinde.rinsim.core.TimeLapse;

public class EntryControl implements TickListener {
  
  private List<AGV> agvList;
  private Simulator  simulator;
  
  public EntryControl(List<AGV> agvList, Simulator simulator) {
    this.agvList = agvList;
    this.simulator = simulator;
  }

  @Override
  public void tick(TimeLapse timeLapse) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void afterTick(TimeLapse timeLapse) {
    Iterator<AGV> i = agvList.iterator();
    while (i.hasNext()) {
      AGV agv = i.next();
      if (agv.getEntryTime() == timeLapse.getTime()) {
        simulator.register(agv);
        i.remove();
      }
    }
  }

}
