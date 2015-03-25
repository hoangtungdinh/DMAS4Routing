package cNetProtocol;

import com.github.rinde.rinsim.core.model.comm.MessageContents;

public class TaskAward implements MessageContents {
   
  private boolean success;
  
  public TaskAward(boolean success) {
    this.success = success;
  }
  
  public boolean getAward() {
    return success;
  }
}
