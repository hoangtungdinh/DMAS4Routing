package delegateMAS;

public class AGVStat {
  
  private int agentID;
  private int numberOfSuccesses;
  private int realLength;
  private int idealLength;
  
  public AGVStat(int agentID, int numberOfSuccesses, int realLength,
      int idealLength) {
    this.agentID = agentID;
    this.numberOfSuccesses = numberOfSuccesses;
    this.realLength = realLength;
    this.idealLength = idealLength;
  }

  public int getAgentID() {
    return agentID;
  }

  public int getNumberOfSuccesses() {
    return numberOfSuccesses;
  }

  public int getRealLength() {
    return realLength;
  }

  public int getIdealLength() {
    return idealLength;
  }
  
  
}
