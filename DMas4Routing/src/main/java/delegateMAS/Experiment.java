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
    .setMapSizeX(16)
    .setMapSizeY(16)
    .setBlockSize(1)
    .setNumberOfAgents(16)
    .setDynamicRate(0)
    .setStopTime(1000 * 1000)
    .setFailureRate(0)
    .build();

    RoutingProblem routingProblem = new RoutingProblem(setting,
        "test2000agents", false);
    routingProblem.run();
  }
}
