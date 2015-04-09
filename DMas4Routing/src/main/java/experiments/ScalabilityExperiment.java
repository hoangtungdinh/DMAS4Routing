package experiments;

import delegateMAS.RoutingProblem;
import delegateMAS.Setting;

public class ScalabilityExperiment {

  public static void main(String[] args) {
    final int numberOfAgents = 2000;
    Setting setting = new Setting.SettingBuilder()
    .setTimeWindow(30)
    .setMinTimeSteps(10)
    .setExplorationFreq(5)
    .setIntentionFreq(15)
    .setIntentionChangingThreshold(70)
    .setPheromoneLifeTime(20)
    .setMapSizeX(120)
    .setMapSizeY(120)
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
