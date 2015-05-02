package delegateMAS;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class Experiment {

  public static void main(String[] args) {
    final int intention = 1;
    final int numOfRuns = 10;
    final String fileName = "-ActFailNumAgentsLattice-";
    
    List<Result> resultList = new ArrayList<>();
    List<Double> successRatio = new ArrayList<>();
    List<Double> avgPathLength = new ArrayList<>();
    
    for (int numOfAgents = 1; numOfAgents <= 20; numOfAgents++) {
      for (int i = 0; i < numOfRuns; i++) {
        System.out.println("Number of Agents: " + (numOfAgents * 100) + "\t"
            + "Run: " + (i + 1));
        
        Setting setting = new Setting.SettingBuilder().setTimeWindow(30)
            .setMinTimeSteps(10)
            .setExplorationFreq(5)
            .setIntentionFreq(intention)
            .setIntentionChangingThreshold(70)
            .setPheromoneLifeTime(intention + 1)
            .setMapSizeX(100)
            .setMapSizeY(100)
            .setBlockSize(1)
            .setNumberOfAgents(numOfAgents*100)
            .setDynamicRate(0)
            .setStopTime(2000 * 1000)
            .setFailureRate(0) // TODO change failure rate
            .build();

        RoutingProblem routingProblem = new RoutingProblem(setting,
            fileName + numOfAgents + "_" + (i + 1), false);
        final Result result = routingProblem.run();
        resultList.add(result);
      }
      
      double totalSuccessRatio = 0;
      double totalAvgPathLength = 0;
      for (Result result : resultList) {
        totalSuccessRatio += result.getSuccessRatio();
        totalAvgPathLength += result.getAvgPathLength();
      }
      resultList.clear();
      successRatio.add(totalSuccessRatio / numOfRuns);
      avgPathLength.add(totalAvgPathLength / numOfRuns);
    }
    
    print(successRatio, avgPathLength, fileName);
    
  }

  public static void print(List<Double> successRatio,
      List<Double> avgPathLength, String fileID) {
    String date = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss")
        .format(new Date());
    String fileName = "Result/" + date + fileID + "-summary";
    try {
      PrintWriter printWriter = new PrintWriter(new FileOutputStream(fileName,
          true));
      
      printWriter.print("Number of Agents\t");
      for (int i = 1; i <= 20; i++) {
        printWriter.print((i*100) + "\t");
      }
      printWriter.println();
      
      printWriter.print("Success Ratio\t");
      for (Double success : successRatio) {
        printWriter.print((new DecimalFormat("###.000").format(success*100)) + "\t");
      }
      printWriter.println();
      
      printWriter.print("Average Path Length\t");
      for (Double avgLength : avgPathLength) {
        printWriter.print((new DecimalFormat("###.000").format(avgLength)) + "\t");
      }
      printWriter.println();
      
      printWriter.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }
}
