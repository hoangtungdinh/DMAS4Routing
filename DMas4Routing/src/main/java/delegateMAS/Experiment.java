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
    final int timeWindow = 30;
    final int numberOfAgents = 250;
    Setting setting = new Setting.SettingBuilder()
    .setTimeWindow(timeWindow)
    .setMinTimeSteps(10)
    .setExplorationFreq(5)
    .setIntentionFreq(intention)
    .setIntentionChangingThreshold(70)
    .setPheromoneLifeTime(intention + 1)
    .setMapSizeX(32)
    .setMapSizeY(32)
    .setBlockSize(1)
    .setNumberOfAgents(numberOfAgents)
    .setDynamicRate(0)
    .setStopTime(100 * 1000)
    .build();
    
    List<Result> resultList = new ArrayList<Result>();

    for (int i = 0; i < 10; i++) {
      RoutingProblem routingProblem = new RoutingProblem(setting, "-NumAgents-"
          + numberOfAgents, false);
      final Result result = routingProblem.run();
      resultList.add(result);
    }
    
    String date = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss")
        .format(new Date());
    String fileName = date + "-summary";
    
    try {
      PrintWriter printWriter = new PrintWriter(new FileOutputStream(fileName,
          true));
      
      printWriter.println("number of failures");
      for (Result result : resultList) {
        printWriter.print(result.getNumOfFailures() + "\t");
      }
      printWriter.println();
      
      printWriter.println("average length");
      for (Result result : resultList) {
        printWriter.print((new DecimalFormat("##.00").format(result.getAverageLength())) + "\t");
      }
      printWriter.println();
      
      printWriter.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }
}
