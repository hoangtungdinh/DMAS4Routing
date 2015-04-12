package delegateMAS;


public class Experiment {

  public static void main(String[] args) {
    final int intention = 1;
    Setting setting = new Setting.SettingBuilder()
    .setTimeWindow(30)
    .setMinTimeSteps(10)
    .setExplorationFreq(5)
    .setIntentionFreq(intention)
    .setIntentionChangingThreshold(70)
    .setPheromoneLifeTime(intention + 1)
    .setMapSizeX(100)
    .setMapSizeY(100)
    .setBlockSize(1)
    .setNumberOfAgents(0)
    .setDynamicRate(0)
    .setStopTime(100000000 * 1000)
    .setFailureRate(0)
    .build();

    RoutingProblem routingProblem = new RoutingProblem(setting,
        "-NumOfAgentVaried-map3", false);
    routingProblem.run();
  }
}
