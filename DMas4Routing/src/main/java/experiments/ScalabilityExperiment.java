package experiments;

import delegateMAS.RoutingProblem;
import delegateMAS.Setting;

public class ScalabilityExperiment {

  public static void main(String[] args) {
    final int numberOfAgents = 1500;
    Setting setting = new Setting.SettingBuilder()
    .setTimeWindow(30)
    .setMinTimeSteps(10)
    .setExplorationFreq(1)
    .setIntentionFreq(1)
    .setIntentionChangingThreshold(70)
    .setPheromoneLifeTime(10)
    .setMapSizeX(100)
    .setMapSizeY(100)
    .setBlockSize(1)
    .setNumberOfAgents(numberOfAgents)
    .setDynamicRate(0)
    .setStopTime(1000 * 1000)
    .build();

    RoutingProblem routingProblem = new RoutingProblem(setting,
        "-numAgent-" + numberOfAgents, false);
    routingProblem.run();
  }
}
